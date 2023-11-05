package com.example.userservice.model;

import com.example.userservice.convertor.StringListConvertor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "deleted_users")
@Getter
@Setter
public class DeletedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JdbcTypeCode(SqlTypes.LONGNVARCHAR)
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(columnDefinition = "VARCHAR(36)")
    @JdbcTypeCode(SqlTypes.LONGNVARCHAR)
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

    @Convert(converter = StringListConvertor.class)
    @NotNull(message = "User must have at least one role")
    @NotEmpty(message = "User must have at least one role")
    private List<String> role;

    @NotNull(message = "User mobile number may not be empty")
    @NotBlank(message = "User mobile number may not be empty")
    private String mobileNumber;

    @NotNull(message = "User address may not be empty")
    @NotBlank(message = "User address may not be empty")
    @Column(columnDefinition = "VARCHAR(255)")
    private String address;

    private String lastUpdatedBy;

    private Date createdAt;

    private Date modifiedAt;

    private Boolean isEnabled;

    private String deletedBy;
}
