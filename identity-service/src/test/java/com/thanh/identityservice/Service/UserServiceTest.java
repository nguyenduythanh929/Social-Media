package com.thanh.identityservice.Service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.thanh.identityservice.DTO.ApiResponse;
import com.thanh.identityservice.DTO.Request.UserCreationRequest;
import com.thanh.identityservice.DTO.Response.UserProfileResponse;
import com.thanh.identityservice.DTO.Response.UserResponse;
import com.thanh.identityservice.Entity.User;
import com.thanh.identityservice.Exception.AppException;
import com.thanh.identityservice.Repository.RoleRepository;
import com.thanh.identityservice.Repository.UserRepository;
import com.thanh.identityservice.Repository.httpclient.ProfileClient;

// Everything outside identity-service (database, profile-service, Kafka) is mocked,
// so these tests run without any infrastructure (e.g. in CI)
@SpringBootTest
@TestPropertySource("/test.properties")
public class UserServiceTest {

    private static final String USER_ID = "a4e80523d4bf";
    private static final String PROFILE_ID = "profile-1";

    @Autowired
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private ProfileClient profileClient;

    @MockitoBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    private UserCreationRequest request;
    private User user;

    @BeforeEach
    void initData() {
        request = UserCreationRequest.builder()
                .username("jame1")
                .password("12345678")
                .email("jame1@example.com")
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1992, 1, 1))
                .build();

        user = User.builder()
                .id(USER_ID)
                .username("jame1")
                .email("jame1@example.com")
                .build();
    }

    @Test
    void createUser_validRequest_success() {
        // GIVEN
        when(roleRepository.findById(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(user);
        when(profileClient.createProfile(any()))
                .thenReturn(ApiResponse.<UserProfileResponse>builder()
                        .result(UserProfileResponse.builder().id(PROFILE_ID).build())
                        .build());

        // WHEN
        UserResponse response = userService.createUser(request);

        // THEN
        Assertions.assertThat(response.getUsername()).isEqualTo("jame1");
        // createUser returns the id of the profile created in profile-service
        Assertions.assertThat(response.getId()).isEqualTo(PROFILE_ID);
        verify(kafkaTemplate).send(eq("notification-delivery"), any());
        verify(kafkaTemplate).send(eq("user-created"), any());
    }

    @Test
    void createUser_userExisted_fail() {
        // GIVEN: the unique constraint on username/email rejects the insert
        when(roleRepository.findById(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        // WHEN
        var exception = assertThrows(AppException.class, () -> userService.createUser(request));

        // THEN
        Assertions.assertThat(exception.getErrorCode().getCode()).isEqualTo(1002);
        verify(profileClient, never()).createProfile(any());
    }

    @Test
    @WithMockUser(username = USER_ID)
    void getMyInfo_valid_success() {
        // The authenticated principal name is the user id (JWT subject)
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        UserResponse response = userService.getMyInfo();

        Assertions.assertThat(response.getUsername()).isEqualTo("jame1");
        Assertions.assertThat(response.getId()).isEqualTo(USER_ID);
    }

    @Test
    @WithMockUser(username = USER_ID)
    void getMyInfo_userNotFound_error() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        var exception = assertThrows(AppException.class, () -> userService.getMyInfo());

        Assertions.assertThat(exception.getErrorCode().getCode()).isEqualTo(1005);
    }
}
