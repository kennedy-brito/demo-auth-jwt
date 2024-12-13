package com.kennedy.demo_auth_jwt.web.controllers;

import com.kennedy.demo_auth_jwt.entities.User;
import com.kennedy.demo_auth_jwt.servicies.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserResource {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user){
        user = userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                user
        );
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<User> findById(@PathVariable Long id){
        return ResponseEntity.ok(
                userService.findById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<User>> listAll(){
        return ResponseEntity.ok(
                userService.findAll()
        );
    }


}
