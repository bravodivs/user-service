package com.example.userservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {

    private UUID userId;

    @NotNull(message = "Username may not be null")
    @NotBlank(message = "Username may not be empty")
    private String username;

    @NotNull(message = "Password may not be null")
    @NotBlank(message = "Password may not be blank")
    @JsonIgnore
    private String password;

    @NotNull(message = "Email may not be null")
    @NotBlank(message = "Email may not be blank")
    @Email
    private String email;

    @NotNull(message = "User must have at least one role")
    @NotEmpty(message = "User must have at least one role")
    private List<String> role;

    @NotNull(message = "User mobile number may not be empty")
    @NotBlank(message = "User mobile number may not be empty")
    private String mobileNumber;

    @NotNull(message = "User address may not be empty")
    private Address address;

    private String lastUpdatedBy;

    private Date createdAt;

    private Date modifiedAt;

    private Boolean isEnabled = Boolean.TRUE;

    public UserDto(String username, String password, String email, List<String> role, String mobileNumber, Address address) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.mobileNumber = mobileNumber;
        this.address = address;
    }

    @Override
    public String toString() {
        return "UserDto{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", address='" + address + '\'' +
                ", lastUpdatedBy='" + lastUpdatedBy + '\'' +
                ", createdAt=" + createdAt +
                ", modifiedAt=" + modifiedAt +
                ", isEnabled=" + isEnabled +
                '}';
    }
}
