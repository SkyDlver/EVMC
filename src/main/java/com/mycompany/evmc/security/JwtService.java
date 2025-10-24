package com.mycompany.evmc.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 256 bits (32 bytes)");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String userEmail) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + (1000 * 60 * 60 * 24));  // e.g. 24h

        return Jwts.builder()
                .setSubject(userEmail)                     // use setSubject, not subject()
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSignKey())                     // OK: use signWith(Key)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build();

        return parser
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().after(new Date()) && claims.getSubject() != null;
        } catch (ExpiredJwtException e) {
            // token expired
            return false;
        } catch (Exception e) {
            // invalid token
            return false;
        }
    }

    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

}
