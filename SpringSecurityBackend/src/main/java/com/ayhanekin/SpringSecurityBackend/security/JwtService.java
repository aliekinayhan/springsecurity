package com.ayhanekin.SpringSecurityBackend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    // the secret key to sign the token we shouldn't leak this
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.access-token-expiration}")
    private long ACCESS_TOKEN_EXPIRATION;

    public String generateToken(String username) {
        return Jwts.builder() // start to create token and put those info into a token
                .subject(username) // for whom token is produced
                .issuedAt(new Date()) // when token is produced
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(getSigningKey(),Jwts.SIG.HS256) // sign with secret key
                .compact(); // merge everything and produce a string with xxxxx.yyyyy.zzzzz this format
    }

    // we are going to take the username from token to ask this token belongs to whom everytime we have request
    public String extractUsername(String token) {
        return Jwts.parser() // start reading the token
                .verifyWith(getSigningKey()) // validate is it signed with secret key
                .build() // prepare the parser
                .parseSignedClaims(token) // parse the token and check the token
                .getPayload() // take the info in payload
                .getSubject(); // get the subject which is username
    }

    // is the username in token and the username in request is matches or not
    public boolean isTokenValid(String token, String username) {
        String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username) && !isTokenExpired(token);
    }

    // it returns true if the token is expired
    private boolean isTokenExpired(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }

    // we keep SECRET_KEY as a string but JWT library only can work with the
    // SecretKey object and that's why we have this method
    // Takes the secret key string and decodes with base 64 and according to
    // HMAC-SHA algorithm  produces a secret key object
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }


}
