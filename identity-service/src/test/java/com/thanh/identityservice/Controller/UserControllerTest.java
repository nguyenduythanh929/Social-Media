package com.thanh.identityservice.Controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.thanh.identityservice.DTO.Response.UserResponse;
import com.thanh.identityservice.Service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
public class UserControllerTest {

    private static final String REGISTRATION_URL = "/users/registration";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    // Request bodies are plain JSON so the test does not depend on a particular Jackson version
    private static String registrationJson(String username, String email) {
        return """
				{
				"username": "%s",
				"password": "12345678",
				"email": "%s",
				"firstName": "John",
				"lastName": "Doe",
				"dob": "1992-01-01"
				}
				"""
                .formatted(username, email);
    }

    @Test
    void createUser_validRequest_success() throws Exception {
        when(userService.createUser(any()))
                .thenReturn(UserResponse.builder()
                        .id("a4e80523d4bf")
                        .username("jame1")
                        .build());

        mockMvc.perform(MockMvcRequestBuilders.post(REGISTRATION_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(registrationJson("jame1", "jame1@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value(1000))
                .andExpect(jsonPath("result.id").value("a4e80523d4bf"))
                .andExpect(jsonPath("result.username").value("jame1"));
    }

    @Test
    void createUser_userNameInvalid_fail() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(REGISTRATION_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(registrationJson("ja", "jame1@example.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value(1003))
                .andExpect(jsonPath("message").value("Username must be at least 3 characters"));
    }

    @Test
    void createUser_emailInvalid_fail() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(REGISTRATION_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(registrationJson("jame1", "not-an-email")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value(1009))
                .andExpect(jsonPath("message").value("Email is not valid"));
    }
}
