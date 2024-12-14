package com.kennedy.demo_auth_jwt.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class JwtUtils {

    public static final String JWT_BEARER = "Bearer ";
    public static final String JWT_AUTHORIZATION = "Authorization";
    public static final String SECRET_KEY = "0123456789-0123456789-0123456789";
    public static final Long EXPIRE_DAYS = 0L;
    public static final Long EXPIRE_HOURS = 0L;
    public static final Long EXPIRE_MINUTES = 2L;

    public static SecretKey generateKey(){
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    private static Date toExpireDate(Date start){
        LocalDateTime dateTime = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        LocalDateTime end = dateTime
                .plusDays(EXPIRE_DAYS)
                .plusHours(EXPIRE_HOURS)
                .plusMinutes(EXPIRE_MINUTES);

        return Date.from(end.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static JwtToken createToken(String username, String role){
        Date issuedAt = new Date();

        Date limit = toExpireDate(issuedAt);

        String token = Jwts.builder()
                .header().add("typ", "jwt") //Defining header using header builder
                .and() //returning to Jwts builder
                .subject(username) // who owns the token
                .issuedAt(issuedAt)
                .expiration(limit)
                .signWith(generateKey()) //The key used to sign, the encrypt algorithm is decided automatically by the lib
                .claims().add("role", role) //additional information in the payload
                .and()
                .compact(); //generate the token

        return new JwtToken(token);
    }
}
