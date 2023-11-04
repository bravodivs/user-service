package com.example.userservice.controller;

import com.example.userservice.exception.CustomException;
import com.example.userservice.model.LoginRequest;
import com.example.userservice.model.LoginResponse;
import com.example.userservice.model.RegisterRequest;
import com.example.userservice.model.UserDto;
import com.example.userservice.service.LoginRegisterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WebMvcTest(UserController.class)
//@TestPropertySource
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private LoginRegisterService loginRegisterService;

/*

    @MockBean
    UserDetailsImpl userDetailsImpl;

    @MockBean
    UserDetailsServiceImpl userDetailsServiceImpl;

    @MockBean
    JwtUtility jwtUtility;

*/
    @InjectMocks
    private UserController userController;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest mockRegisterRequest;

    @BeforeEach
    void setUp() {
        List<String> mockRoles = List.of("ADMIN");
        mockRegisterRequest = new RegisterRequest(
                "test user",
                "test password",
                "test address",
                "test@email.com",
                mockRoles,
                "8982762817"
        );
    }

    @Test
    void testLoginSuccess() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testUser", "testPassword");
        LoginResponse loginResponse = new LoginResponse("fhwhfow", 35000, "BEARER");

        when(loginRegisterService.login(loginRequest)).thenReturn(loginResponse);

        String response = mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertNotEquals(0, response.length());
    }

    @Test
    void testLoginFailure() throws Exception {
        when(loginRegisterService.login(any())).thenThrow(new CustomException("invalid credentials", HttpStatus.UNAUTHORIZED));
        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/auth/login"));
        } catch (CustomException e) {
            assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
        }
    }

    @Test
    void testRegisterSuccess(){
        when(loginRegisterService.registerUser(mockRegisterRequest)).thenReturn(new UserDto());

        ResponseEntity<UserDto> responseEntity = userController.register(mockRegisterRequest);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

    }

    @Test
    void update() {
    }

    @Test
    void viewUser() {
    }

    @WithMockUser(authorities = "ADMIN")
    @Test
    void viewAll() {
        assertThrows(AccessDeniedException.class, ()-> mockMvc.perform(MockMvcRequestBuilders.get("/view/user")));
    }
}