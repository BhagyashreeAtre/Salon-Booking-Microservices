
package com.example.service;

import com.example.payload.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Keycloakservice {

    @Value("${KEYCLOAK_BASE_URL:http://localhost:8080}")
    private String baseUrl;

    @Value("${KEYCLOAK_REALM:salon-booking}")
    private String realm;

    @Value("${KEYCLOAK_ADMIN_REALM:master}")
    private String adminRealm;

    @Value("${KEYCLOAK_ADMIN_USERNAME:}")
    private String adminUsername;

    @Value("${KEYCLOAK_ADMIN_PASSWORD:}")
    private String adminPassword;

    @Value("${KEYCLOAK_ADMIN_CLIENT_ID:admin-cli}")
    private String adminClientId;

    @Value("${KEYCLOAK_CLIENT_ID:salon-booking-client}")
    private String clientId;

    @Value("${KEYCLOAK_CLIENT_SECRET:}")
    private String clientSecret;

    private final RestTemplate restTemplate;

    private String tokenUrl(String targetRealm) {
        return baseUrl + "/realms/" + targetRealm
                + "/protocol/openid-connect/token";
    }

    private String adminApiUrl() {
        return baseUrl + "/admin/realms/" + realm;
    }

    public void createUser(SignupDTO signupDTO) throws Exception {

        if (adminUsername.isBlank() || adminPassword.isBlank()) {
            throw new IllegalStateException(
                    "Keycloak admin credentials are not configured"
            );
        }

        String adminToken = getAdminAccessToken().getAccessToken();

        Credential credential = new Credential();
        credential.setTemporary(false);
        credential.setType("password");
        credential.setValue(signupDTO.getPassword());

        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(signupDTO.getUsername());
        userRequest.setEmail(signupDTO.getEmail());
        userRequest.setEnabled(true);
        userRequest.setLastName(signupDTO.getFullName());
        userRequest.getCredentials().add(credential);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        HttpEntity<UserRequest> request =
                new HttpEntity<>(userRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                adminApiUrl() + "/users",
                HttpMethod.POST,
                request,
                String.class
        );

        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new Exception("Keycloak user creation failed: "
                    + response.getBody());
        }

        KeycloakUserDTO user =
                fetchFirstUserByUsername(signupDTO.getUsername(), adminToken);

        KeycloakRole role = getRoleByName(
                adminToken,
                signupDTO.getRole().toString()
        );

        assignRoleToUser(user.getId(), role, adminToken);
    }

    public KeycloakUserDTO fetchFirstUserByUsername(
            String username, String accessToken) throws Exception {

        String url = UriComponentsBuilder
                .fromUriString(adminApiUrl() + "/users")
                .queryParam("username", username)
                .queryParam("exact", true)
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<KeycloakUserDTO[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                KeycloakUserDTO[].class
        );

        KeycloakUserDTO[] users = response.getBody();

        if (users != null && users.length > 0) {
            return users[0];
        }

        throw new Exception("Keycloak user not found: " + username);
    }

    public TokenResponse getAdminAccessToken() throws Exception {
        return requestToken(
                tokenUrl(adminRealm),
                adminClientId,
                null,
                adminUsername,
                adminPassword,
                "password",
                null,
                null
        );
    }

    public TokenResponse getUserAccessToken(
            String username,
            String password,
            String grantType,
            String refreshToken) throws Exception {

        return requestToken(
                tokenUrl(realm),
                clientId,
                clientSecret,
                username,
                password,
                grantType,
                refreshToken,
                "openid profile email"
        );
    }

    private TokenResponse requestToken(
            String url,
            String requestedClientId,
            String requestedClientSecret,
            String username,
            String password,
            String grantType,
            String refreshToken,
            String scope) throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.APPLICATION_FORM_URLENCODED
        );

        MultiValueMap<String, String> body =
                new LinkedMultiValueMap<>();

        body.add("grant_type", grantType);
        body.add("client_id", requestedClientId);

        if (requestedClientSecret != null
                && !requestedClientSecret.isBlank()) {
            body.add("client_secret", requestedClientSecret);
        }

        if (username != null) {
            body.add("username", username);
        }

        if (password != null) {
            body.add("password", password);
        }

        if (refreshToken != null) {
            body.add("refresh_token", refreshToken);
        }

        if (scope != null) {
            body.add("scope", scope);
        }

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<TokenResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                TokenResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful()
                || response.getBody() == null) {
            throw new Exception("Failed to obtain Keycloak token");
        }

        return response.getBody();
    }

    public KeycloakRole getRoleByName(
            String token, String roleName) throws Exception {

        String url = adminApiUrl() + "/roles/" + roleName;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<KeycloakRole> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                KeycloakRole.class
        );

        if (response.getBody() == null) {
            throw new Exception("Keycloak role not found: " + roleName);
        }

        return response.getBody();
    }

    public void assignRoleToUser(
            String userId,
            KeycloakRole role,
            String token) throws Exception {

        String url = adminApiUrl()
                + "/users/" + userId + "/role-mappings/realm";

        List<KeycloakRole> roles = new ArrayList<>();
        roles.add(role);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Void> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(roles, headers),
                Void.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new Exception("Failed to assign Keycloak realm role");
        }
    }
}
