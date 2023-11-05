package com.example.userservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class TokenStore {
    private static final Logger logger = LoggerFactory.getLogger(TokenStore.class);

    private static Set<String> revokedTokens = new HashSet<>();

    private TokenStore(){}

    public static void revokeToken(String token) {
        revokedTokens.add(token);
        logger.info("Token revoked!");
    }

    public static boolean isTokenRevoked(String token) {
        logger.info("Revoked tokens for the session: {}",revokedTokens.toString());
        return revokedTokens.contains(token);
    }
}
