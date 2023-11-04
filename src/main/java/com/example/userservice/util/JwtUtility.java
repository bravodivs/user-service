package com.example.userservice.util;

import com.example.userservice.exception.CustomException;
import com.example.userservice.service.TokenStore;
import com.example.userservice.service.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtility {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtility.class);

    @Value("${spring.app.jwtExpirationMs}")
    private int tokenExpiration;
    @Value("${spring.app.jwtSecret}")
    private String secretKey;

    public String generateToken(UserDetailsImpl userDetailsImpl) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", userDetailsImpl.getUsername());
        claims.put("roles", userDetailsImpl.getRoles());
        claims.put("email", userDetailsImpl.getEmail());
        return doGenerateToken(claims, userDetailsImpl.getUsername());
    }

    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private Claims getAllClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new CustomException(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return claims;
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public void revokeToken(String token) {
        TokenStore.revokeToken(token);
    }

    public Boolean checkTokenRevoked(String token) {
        return TokenStore.isTokenRevoked(token);
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token) && !checkTokenRevoked(token));
    }

    private Key getSigningKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (Exception e) {
            logger.info(e.getMessage());
            throw new CustomException("Signing key error. Check logs for details", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
