package com.example.service.impl;

import com.example.exception.UserException;
import com.example.model.User;
import com.example.payload.dto.KeycloakUserDTO;
import com.example.repository.UserRepository;
import com.example.service.Keycloakservice;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final Keycloakservice keycloakservice;
    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) throws UserException{
        Optional<User> otp=userRepository.findById(id);
        if(otp.isPresent()){
            return otp.get();
        }
        throw new UserException("User not found");
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(Long id) throws UserException {
        Optional<User> otp=userRepository.findById(id);
        if(otp.isEmpty()){
            throw new UserException("User not found with id"+id);
        }
        userRepository.deleteById(otp.get().getId());
    }

    @Override
    public User updateUser(Long id, User user) throws UserException {
        Optional<User> otp=userRepository.findById(id);
        if(otp.isEmpty()){
            throw new UserException("User not found with id"+id);
        }
        User existingUser=otp.get();

        existingUser.setFullName(user.getFullName());
        existingUser.setEmail(user.getEmail());
        existingUser.setRole(user.getRole());
        existingUser.setUsername(user.getUsername());
        return userRepository.save(existingUser);
    }

    @Override
    public User getUserFromEmail(String email) throws Exception {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        return user;
    }
}
