package com.thanh.identityservice.Service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.thanh.identityservice.DTO.Request.UserCreationRequest;
import com.thanh.identityservice.DTO.Response.UserResponse;
import com.thanh.identityservice.Entity.User;
import com.thanh.identityservice.Exception.AppException;
import com.thanh.identityservice.Repository.UserRepository;

@SpringBootTest
@TestPropertySource("/test.properties")
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    private UserCreationRequest request;
    private UserResponse response;
    private User user;
    private LocalDate dob;

    @BeforeEach
    void initData() {
        dob = LocalDate.of(1992, 1, 1);

        request = UserCreationRequest.builder()
                .username("jame1")
                .password("12345678")
                .firstName("John")
                .lastName("Doe")
                .dob(dob)
                .build();

        response = UserResponse.builder()
                .id("a4e80523d4bf")
                .username("jame1")
                .firstName("John")
                .lastName("Doe")
                .dob(dob)
                .build();

        user = User.builder()
                .id("a4e80523d4bf")
                .username("jame1")
                .firstName("John")
                .lastName("Doe")
                .dob(dob)
                .build();
    }

    @Test
    void createUser_validRequest_success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(user);

        // WHEN
        UserResponse response = userService.createUser(request);

        // THEN
        Assertions.assertThat(response.getId()).isEqualTo("a4e80523d4bf");
        Assertions.assertThat(response.getUsername()).isEqualTo("jame1");
    }

    @Test
    void createUser_userExit_fail() {
        // GIVEN
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // WHEN
        var exception = assertThrows(AppException.class, () -> userService.createUser(request));

        // THEN
        Assertions.assertThat(exception.getErrorCode().getCode()).isEqualTo(1002);
    }

    @Test
    @WithMockUser(username = "jame1")
    void getMyInfo_valid_success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        UserResponse response = userService.getMyInfo();

        Assertions.assertThat(response.getUsername()).isEqualTo("jame1");
        Assertions.assertThat(response.getId()).isEqualTo("a4e80523d4bf");
    }

    @Test
    @WithMockUser(username = "jame1")
    void getMyInfo_userNotFound_error() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.ofNullable(null));

        var exception = assertThrows(AppException.class, () -> userService.getMyInfo());

        Assertions.assertThat(exception.getErrorCode().getCode()).isEqualTo(1005);
    }
}
