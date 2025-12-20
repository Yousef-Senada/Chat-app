package com.example.chat_app.controller;


import com.example.chat_app.model.dto.user.UserResponse;
import com.example.chat_app.model.entity.User;
import com.example.chat_app.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private UserRepository userRepo;

    public UserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getCurrentUserProfile(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated()){
            return ResponseEntity.status(401).build();
        }

        User currentUser = (User) authentication.getPrincipal();

        User userEntity = userRepo.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found in DB"));

        UserResponse response = new UserResponse(
                userEntity.getId(),
                userEntity.getName(),
                userEntity.getUsername(),
                userEntity.getPhoneNumber()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userRepo.findAll();

        List<UserResponse> response = users.stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getUsername(),
                        user.getPhoneNumber()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

}
