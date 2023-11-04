package com.example.userservice.service;

import com.example.userservice.exception.CustomException;
import com.example.userservice.model.*;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.util.UserUtils;
import java.util.Arrays;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    /*primarily used to find the person who is logged in.*/
    @Override
    public UserDetailsImpl loadUserByUsername(String username) {
        try {
            User user = userRepository.findTopByUsername(username);

            if (Objects.isNull(user)) {
                logger.error("User - {} not found", username);
                throw new CustomException("Invalid credentials", HttpStatus.UNAUTHORIZED);
            }

            logger.info("Entry for authenticator/principal found: {}", String.valueOf(user));
            return new UserDetailsImpl(UserUtils.userDaoToDto(user));

        } catch (CustomException cx) {
            logger.error(Arrays.toString(cx.getStackTrace()));
            throw new CustomException(cx.getMessage(), cx.getStatus());

        } catch (Exception e) {
            logger.error("Error while loading user- {}", e.getMessage());
            throw new CustomException(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
