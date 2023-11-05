package com.example.userservice.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.NumberFormat;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotNull(message = "Username may not be null")
    @NotBlank(message = "Username may not be empty")
    private String username;

    @NotNull(message = "Password may not be null")
    @NotBlank(message = "Password may not be blank")
    private String password;

    @NotNull(message = "User address may not be empty")
    private Address address;

    @NotNull(message = "Email may not be null")
    @NotBlank(message = "Email may not be blank")
    @Email
    private String email;

/*
    @NotNull(message = "User must have at least one role")
    @NotEmpty(message = "User must have at least one role")
    private List<String> role;
*/

    @NumberFormat(style = NumberFormat.Style.NUMBER, pattern = "#####-#####")
    @Size(min = 10, max = 10)
    @Digits(integer = 10, fraction = 0, message = "Mobile number cannot exceed a 10 digit number")
    @NotNull(message = "User mobile number may not be empty")
    @NotBlank(message = "User mobile number may not be empty")
    private String mobileNumber;
}
