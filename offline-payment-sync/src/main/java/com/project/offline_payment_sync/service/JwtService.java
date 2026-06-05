


package com.project.offline_payment_sync.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
   

    @Value("${JWT_SECRET}")
private String secretKey;

    private static final long EXPIRATION_TIME = 1000L * 60 * 60 * 24;

    
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignKey())
                .compact();
    }

    // Backward compatibility method
    public String generateToken(String email) {
        return generateToken(email, "ROLE_USER");
    }

    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    
    public String extractRole(String token) {
        try {
            String role = extractClaim(token, claims -> claims.get("role", String.class));
            
            //  FIX: If role is null or empty (old tokens), default to ROLE_USER
            if (role == null || role.trim().isEmpty()) {
                System.out.println("  [JWT] Role claim missing - old token detected. Defaulting to ROLE_USER");
                return "ROLE_USER";
            }
            
            return role.trim();
        } catch (Exception e) {
            System.out.println("  [JWT] Error extracting role: " + e.getMessage() + ". Defaulting to ROLE_USER");
            return "ROLE_USER";
        }
    }

    
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT Token: " + e.getMessage());
        }
    }

    
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (ExpiredJwtException e) {
            System.err.println("Token is expired: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    
    public boolean isTokenExpired(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token);

            return false;
        } catch (ExpiredJwtException e) {
            System.err.println("Token confirmed expired by library");
            return true;
        } catch (Exception e) {
            System.err.println("Token validation error (not expiration): " + e.getMessage());
            return false;
        }
    }

        
        private Key getSignKey() {
    return Keys.hmacShaKeyFor(secretKey.getBytes());

    }
}