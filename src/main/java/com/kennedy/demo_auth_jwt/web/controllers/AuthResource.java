package com.kennedy.demo_auth_jwt.web.controllers;

import com.kennedy.demo_auth_jwt.entities.User;
import com.kennedy.demo_auth_jwt.jwt.JwtToken;
import com.kennedy.demo_auth_jwt.jwt.JwtUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthResource {

    private static final Logger log = LoggerFactory.getLogger(AuthResource.class);
    private final JwtUserDetailsService userDetailsService;

    private final AuthenticationManager authenticationManager;

    @PostMapping
    public ResponseEntity<?> authenticate(@RequestBody User user, HttpServletRequest request){
        try{
            UsernamePasswordAuthenticationToken authenticationToken
                    = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());

            authenticationManager.authenticate(authenticationToken);

            JwtToken token =userDetailsService.getTokenAuthenticated(user.getUsername());
            return ResponseEntity.ok(token);
        }catch (AuthenticationException e){
            log.error("Authentication error for user: {}", user.getUsername());
        }

        return ResponseEntity.badRequest().build();
    }
}
