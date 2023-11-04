package com.example.userservice.util;

import com.example.userservice.exception.CustomException;
import com.example.userservice.model.User;
import com.example.userservice.model.UserDto;
import com.example.userservice.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Set;

public class UserUtils {
    private static final Logger logger = LoggerFactory.getLogger(UserUtils.class);

    @Autowired
    private static UserRepository userRepository;

    private UserUtils() {
    }

    public static UserDto userDaoToDto(User user) {
        UserDto userDto = new UserDto();

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        if (violations.isEmpty()) {
            BeanUtils.copyProperties(user, userDto);
        } else {
            logger.error("Violations while dao to dto");
            violations.forEach(v -> logger.error(v.getMessage()));
            throw new CustomException("Violation of constraints. Check log for details", HttpStatus.BAD_REQUEST);
        }
        logger.info("User {} dao converted to dto after successful validation.", userDto.getUsername());
        logger.info("Formed dto - {}", userDto.toString());
        return userDto;
    }

    public static User userDtoToDao(UserDto userDto) {
        User user = new User();

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        if (violations.isEmpty()) {
            BeanUtils.copyProperties(userDto, user);
        } else {
            logger.error("Violations while user dto to dao");
            violations.forEach(v -> logger.error(v.getMessage()));
            throw new CustomException("Violation of constraints. Check log for details", HttpStatus.BAD_REQUEST);
        }

        logger.info("User {} dto converted to dao after successful validation.", user.getUsername());
        logger.info("formed dao - {}", user.toString());
        return user;
    }
/*
    public static <T> String toJsonString(T object) {
        String value;

        try {
            value = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException jx) {
            logger.error(jx.getMessage());
            throw new CustomException("Invalid format", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        logger.info("converted json string - {}", value);
        return value;
    }

    public static <T> List<T> fromJsonString(String jsonString, Class<T> clazz) {
        List<T> value;

        logger.info("received json string - {}", jsonString);

        try {
            value = objectMapper.readValue(jsonString, new TypeReference<>() {
            });
        } catch (JsonProcessingException jx) {
            logger.error(jx.getMessage());
            throw new CustomException("Invalid Format", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return value;
    }*/

}
