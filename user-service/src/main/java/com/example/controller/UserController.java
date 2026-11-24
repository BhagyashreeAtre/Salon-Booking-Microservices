package com.example.controller;

import com.example.exception.UserException;
import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/users")
    public ResponseEntity<User> createUser(@RequestBody @Valid User user){
        User createdUser=userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }
    @GetMapping("/api/users/profile")
    public ResponseEntity<User> getUserProfile(
            @AuthenticationPrincipal Jwt jwt) throws Exception {

        String email = jwt.getClaimAsString("email");

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email claim not found in JWT");
        }

        User user = userService.getUserFromEmail(email);

        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/users")
    public ResponseEntity<List<User>> getUsers(){
        List<User> users=userService.getAllUsers();
        return new ResponseEntity<>(users,HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) throws Exception{
        User user=userService.getUserById(id);
        return new ResponseEntity<>(user,HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/users/{id}")
    public ResponseEntity<User> updateUser(@RequestBody User user,
                           @PathVariable Long id) throws Exception{
        User updatedUser=userService.updateUser(id,user);
        return new ResponseEntity<>(updatedUser,HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/users/{id}")
    public ResponseEntity<String> deleteUserById(@PathVariable Long id) throws Exception{
       userService.deleteUser(id);
       return new ResponseEntity<>("User deleted",HttpStatus.ACCEPTED);
    }
/*@GetMapping("/api/users")
    public User getUser(){
        User user=new User();
        user.setEmail("abc@gmail.com");
        user.setFullName("Shree Limaye");
        user.setPhone("+91 888889090");
        user.setRole("Customer");
        return user;
    }*/
}
