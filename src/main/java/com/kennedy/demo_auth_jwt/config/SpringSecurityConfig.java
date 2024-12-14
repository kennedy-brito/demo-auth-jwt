package com.kennedy.demo_auth_jwt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
public class SpringSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf( crfs -> crfs.disable())
                .formLogin( form -> form.disable())
                .httpBasic( basic -> basic.disable())
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers(
                                        antMatcher(HttpMethod.POST, "/users"),
                                        antMatcher(HttpMethod.GET, "/users/{id}"),
                                        antMatcher(HttpMethod.GET, "/users")
                                ).permitAll()
                                .anyRequest().authenticated()
                ).build();
    }

}
