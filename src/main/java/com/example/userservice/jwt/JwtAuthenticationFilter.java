package com.example.userservice.jwt;

import com.example.userservice.exception.CustomException;
import com.example.userservice.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger eventLogger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final UserDetailsServiceImpl userDetailsServiceImpl;

    private final HandlerExceptionResolver handlerExceptionResolver;

    private final JwtUtility jwtUtility;

    @Autowired
    public JwtAuthenticationFilter(UserDetailsServiceImpl userDetailsServiceImpl,
                                   JwtUtility jwtUtility,
                                   HandlerExceptionResolver handlerExceptionResolver) {
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.jwtUtility = jwtUtility;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        eventLogger.info("Request path received - {}", request.getServletPath());

        if (request.getServletPath().contains("/auth") ||
                request.getServletPath().contains("/swagger-ui") ||
                request.getServletPath().contains("/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userName;

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                eventLogger.error("Bearer token not present in auth header - {}", authHeader);
                throw new CustomException("Bearer token not present", HttpStatus.UNAUTHORIZED);
            }

            if (Boolean.TRUE.equals(jwtUtility.checkTokenRevoked(authHeader))) {
                logger.error("user is logged out");
                throw new CustomException("User logged out. Login again to continue", HttpStatus.UNAUTHORIZED);
            }
            jwt = authHeader.substring(7);

            try {
                userName = jwtUtility.getUsernameFromToken(jwt);
            } catch (CustomException cx) {
                eventLogger.error("Token expired/wrong token provided. Expiration time: {}",
                        jwtUtility.getExpirationDateFromToken(jwt));
                throw new CustomException(String.format("Token expired/wrong token provided. Expiration time: %s",
                        jwtUtility.getExpirationDateFromToken(jwt)), HttpStatus.UNAUTHORIZED);
            }
            if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails;
                userDetails = userDetailsServiceImpl.loadUserByUsername(userName);
                if (Boolean.TRUE.equals(jwtUtility.validateToken(jwt, userDetails))) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    eventLogger.info("authenticated user {}, setting security context", userName);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (CustomException cx) {
            handlerExceptionResolver.resolveException(request, response, null, cx);
        }
    }
}
