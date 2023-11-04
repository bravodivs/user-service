package com.example.userservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotNull(message = "username must be given")
    @NotBlank(message = "username must be valid")
    private String username;

    @NotNull(message = "password must be given")
    @NotBlank(message = "password should not be an empty string")
    private String password;
}
