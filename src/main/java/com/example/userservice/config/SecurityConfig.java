package com.example.userservice.config;

import com.example.userservice.exception.CustomException;
import com.example.userservice.jwt.JwtAuthenticationFilter;
import com.example.userservice.jwt.JwtUtility;
import com.example.userservice.model.UserRole;
import com.example.userservice.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Arrays;

@Configuration
@EnableWebMvc
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] allowed_urls = {
            "/auth/login",
            "/auth/register",
            "/v3/api-docs",
            "/v2/api-docs",
            "/api-docs/**",
            "/swagger-resources/**",
            "/swagger-ui/**",
            "/webjars/**"
    };

    private static final String[] admin_access_urls = {
            "/view_users",
            "/auth/register_admin",
            "/disable/**",
            "/enable/**",
            "/delete/**"
    };
    private final UserDetailsService userDetailsService;
    private final HandlerExceptionResolver handlerExceptionResolver;
    @Autowired
    private JwtUtility jwtUtility;
    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    public SecurityConfig(UserDetailsService userDetailsService, HandlerExceptionResolver handlerExceptionResolver) {
        this.userDetailsService = userDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req
                        .requestMatchers(getAntPathRequestMatchers(allowed_urls)).permitAll()
                        .requestMatchers(getAntPathRequestMatchers(admin_access_urls)).hasAuthority(UserRole.ADMIN.toString())
                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(new JwtAuthenticationFilter(userDetailsServiceImpl,
                        jwtUtility,
                        handlerExceptionResolver), UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling(exception->exception.authenticationEntryPoint(new Http403ForbiddenEntryPoint()));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setUserDetailsService(userDetailsService);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> {
            throw new CustomException("Access Denied: You don't have permission to access this resource.",
                    HttpStatus.FORBIDDEN);
        };
    }

    private AntPathRequestMatcher[] getAntPathRequestMatchers(String[] urlList) {
        return Arrays.stream(urlList)
                .map(AntPathRequestMatcher::new)
                .toArray(AntPathRequestMatcher[]::new);
    }
}
