package com.kennedy.demo_auth_jwt.jwt;


import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

public class JwtUserDetails extends User {

    private com.kennedy.demo_auth_jwt.entities.User user;

    public JwtUserDetails(com.kennedy.demo_auth_jwt.entities.User user) {
        super(
                user.getUsername(),
                user.getPassword(),
                AuthorityUtils.createAuthorityList(user.getRole().name())
        );

        this.user = user;
    }

    public Long getId(){
        return user.getId();
    }

    public String role(){
        return user.getRole().name();
    }
}
