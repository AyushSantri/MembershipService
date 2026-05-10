package com.firstclub.membership.controller;

import com.firstclub.membership.dto.CreateUserDto;
import com.firstclub.membership.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "Create a new user in the system. This is a prerequisite for any membership operations.")
public class UserController {

    private final UserService userService;

     @PostMapping("/create")
     public ResponseEntity<String> createUser(@RequestBody CreateUserDto createUserDto) {
         try {
             return ResponseEntity.ok(userService.createUser(createUserDto));
         } catch (Exception e) {
             return ResponseEntity.status(500).body("Error creating user: " + e.getMessage());
         }
     }
}
