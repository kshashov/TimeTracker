package com.github.kshashov.timetracker.web.security;

import com.github.kshashov.timetracker.data.entity.user.User;

public class JwtTokenUtil {
    public static final long ACCESS_TOKEN_VALIDITY_SECONDS = 5 * 60 * 60;
    public static final String SIGNING_KEY = "devglan123r";

    public static String generateToken(User user) {
        return "token";
        /*Claims claims = Jwts.claims().setSubject(user.getEmail());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer("https://devglan.com")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_SECONDS*1000))
                .signWith(SignatureAlgorithm.HS256, SIGNING_KEY)
                .compact();*/
    }
}
