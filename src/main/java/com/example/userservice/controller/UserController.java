package com.example.userservice.controller;

import com.example.userservice.model.*;
import com.example.userservice.service.LoginRegisterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import static com.example.userservice.constants.UserConstants.*;

@RestController
public class UserController {

    @Autowired
    private LoginRegisterService loginRegisterService;

    @PostMapping(value = "/auth/login", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Logs in a user", description = LOGIN_DESCRIPTION)
    @ApiResponse(responseCode = "500", description = SERVER_ERROR_DESCRIPTION)
    @ApiResponse(responseCode = "200", description = "User logged in")
    @ApiResponse(responseCode = "401", description = "Invalid username or password entered")
    @ApiResponse(responseCode = "400", description = "The user is disabled hence action prohibited")
    public ResponseEntity<LoginResponse> login(@Valid
                                               @RequestBody
                                               @Parameter(name = "Login Request",
                                                       description = "JSON of username and password",
                                                       example = "{" +
                                                               "'username':'user'" +
                                                               "'password':'Password@1'" +
                                                               "}")
                                               LoginRequest loginRequest) {
        return new ResponseEntity<>(loginRegisterService.login(loginRequest), HttpStatus.OK);
    }

    @PostMapping(value = "/logout/{username}")
    @Operation(summary = "Logs out a user", description = LOGOUT_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully logged out"),
            @ApiResponse(responseCode = "401", description = "User already logged out"),
            @ApiResponse(responseCode = "403", description = "Access denied for trying to log out other accounts"),
            @ApiResponse(responseCode = "500", description = "Internal Error")
    })
    public ResponseEntity<OkResponse> logout(@RequestHeader("Authorization") String accessToken,
                                             @PathVariable String username) {
        return new ResponseEntity<>(loginRegisterService.logoutUser(username, accessToken), HttpStatus.OK);
    }

    @PostMapping(value = "/auth/register", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Registers a CUSTOMER", description = REGISTER_DESCRIPTION)
    @ApiResponse(responseCode = "500", description = SERVER_ERROR_DESCRIPTION)
    @ApiResponse(responseCode = "201", description = "User registered")
    @ApiResponse(responseCode = "406", description = "The user already exists or invalid fields provided while registering")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return new ResponseEntity<>(loginRegisterService.registerUser(registerRequest, false), HttpStatus.CREATED);
    }

    @PostMapping(value = "/auth/register_admin", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Registers an ADMIN", description = REGISTER_ADMIN_DESCRIPTION)
    @ApiResponse(responseCode = "500", description = SERVER_ERROR_DESCRIPTION)
    @ApiResponse(responseCode = "201", description = "User registered")
    @ApiResponse(responseCode = "406", description = "The user already exists or invalid fields provided while registering")
    public ResponseEntity<UserDto> registerAdmin(@Valid @RequestBody RegisterRequest registerRequest) {
        return new ResponseEntity<>(loginRegisterService.registerUser(registerRequest, true), HttpStatus.CREATED);
    }

    @PutMapping(value = "/update/{username}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "500", description = "Internal server error regarding queries or other error")
    @ApiResponse(responseCode = "201", description = "User found and updated")
    @ApiResponse(responseCode = "401", description = BAD_REQUEST_DESCRIPTION)
    @ApiResponse(responseCode = "404", description = "The user is not found")
    @ApiResponse(responseCode = "400", description = "The user is disabled hence action prohibited")
    @Operation(summary = "Updates the given user", description = UPDATE_DESCRIPTION)
    public ResponseEntity<UserDto> update(@RequestBody RegisterRequest registerRequest,
                                          @RequestHeader("Authorization") String accessToken,
                                          @PathVariable String username) {
        return new ResponseEntity<>(loginRegisterService.update(accessToken, username, registerRequest), HttpStatus.CREATED);
    }

    @GetMapping(value = "view/{username}")
    @Operation(summary = "Views profile of a user",
            description = VIEW_USER_DESCRIPTION)
    @ApiResponse(responseCode = "500", description = SERVER_ERROR_DESCRIPTION)
    @ApiResponse(responseCode = "200", description = "User found and returned")
    @ApiResponse(responseCode = "401", description = BAD_REQUEST_DESCRIPTION)
    @ApiResponse(responseCode = "404", description = "The user is not found")
    @ApiResponse(responseCode = "400", description = "The user is disabled hence action prohibited")
    public ResponseEntity<UserDto> viewUser(@PathVariable String username, Principal principal) {
        return new ResponseEntity<>(loginRegisterService.viewUser(username, principal), HttpStatus.OK);
    }

    @GetMapping(value = "/view_users")
    @Operation(summary = "View all the users",
            description = VIEW_ALL_DESCRIPTION)
    @ApiResponse(responseCode = "500", description = SERVER_ERROR_DESCRIPTION)
    @ApiResponse(responseCode = "200", description = "User list returned")
    @ApiResponse(responseCode = "401", description = BAD_REQUEST_DESCRIPTION)
    @ApiResponse(responseCode = "404", description = "The user list is empty")
    public ResponseEntity<List<UserDto>> viewAll(@RequestParam(defaultValue = "0") boolean disabled) {
        return new ResponseEntity<>(loginRegisterService.getAllUsers(disabled), HttpStatus.OK);
    }

    @PostMapping(value = "/action/disable/{username}")
    @Operation(summary = "Disable a user",
            description = DISABLE_DESCRIPTION)
    @ApiResponse(responseCode = "500", description = SERVER_ERROR_DESCRIPTION)
    @ApiResponse(responseCode = "200", description = "User found and disabled")
    @ApiResponse(responseCode = "401", description = BAD_REQUEST_DESCRIPTION)
    @ApiResponse(responseCode = "404", description = "The user is not found")
    public ResponseEntity<OkResponse> disableUser(@PathVariable String username) {
        return new ResponseEntity<>(loginRegisterService.disableUser(username), HttpStatus.OK);
    }

    @PostMapping(value = "/action/enable/{username}")
    @Operation(summary = "Enable a user",
            description = ENABLE_DESCRIPTION)
    @ApiResponse(responseCode = "500", description = SERVER_ERROR_DESCRIPTION)
    @ApiResponse(responseCode = "200", description = "User found and enabled")
    @ApiResponse(responseCode = "401", description = BAD_REQUEST_DESCRIPTION)
    @ApiResponse(responseCode = "404", description = "The user is not found")
    public ResponseEntity<OkResponse> enableUser(@PathVariable String username) {
        return new ResponseEntity<>(loginRegisterService.enableUser(username), HttpStatus.OK);
    }

    @DeleteMapping(value = "/action/delete/{username}")
    @Operation(summary = "Deletes a user",
            description = DELETE_DESCRIPTION)
    @ApiResponse(responseCode = "200", description = "User found and deleted")
    @ApiResponse(responseCode = "401", description = BAD_REQUEST_DESCRIPTION)
    @ApiResponse(responseCode = "404", description = "When the user is not found")
    @ApiResponse(responseCode = "500", description = SERVER_ERROR_DESCRIPTION)
    public ResponseEntity<OkResponse> deleteUser(@RequestHeader("Authorization") String accessToken,
                                                 @PathVariable String username) {
        return new ResponseEntity<>(loginRegisterService.deleteUser(accessToken, username), HttpStatus.OK);
    }
}
