package com.thanh.identityservice.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.thanh.event.dto.NotificationEvent;
import com.thanh.identityservice.DTO.Request.UserCreationRequest;
import com.thanh.identityservice.DTO.Request.UserUpdateRequest;
import com.thanh.identityservice.DTO.Response.UserResponse;
import com.thanh.identityservice.Entity.Role;
import com.thanh.identityservice.Entity.User;
import com.thanh.identityservice.Exception.AppException;
import com.thanh.identityservice.Exception.ErrorCode;
import com.thanh.identityservice.Mapper.ProfileMapper;
import com.thanh.identityservice.Mapper.UserMapper;
import com.thanh.identityservice.Repository.RoleRepository;
import com.thanh.identityservice.Repository.UserRepository;
import com.thanh.identityservice.Repository.httpclient.ProfileClient;
import com.thanh.identityservice.constant.PredefinedRole;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    final UserRepository userRepository;
    final RoleRepository roleRepository;
    final UserMapper userMapper;
    final PasswordEncoder passwordEncoder;
    final ProfileMapper profileMapper;
    final ProfileClient profileClient;
    final KafkaTemplate<String, Object> kafkaTemplate;

    public UserResponse createUser(UserCreationRequest request) {

        log.info(request.toString());
        User user = userMapper.toUser(request);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        user.setRoles(roles);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        var profileRequest = profileMapper.toProfileCreationRequest(request);

        profileRequest.setUserId(user.getId());
        log.info(profileRequest.getUserId());
        log.info(profileRequest.getUsername());
        var profile = profileClient.createProfile(profileRequest);

        log.info(profile.toString());

        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("EMAIL")
                .recipient(request.getEmail())
                .subject("Welcome to bookteria")
                .body("Hello, " + user.getUsername())
                .build();

        // Publish message to kafka
        kafkaTemplate.send("notification-delivery", notificationEvent);

        // Publish user-created event for search indexing
        kafkaTemplate.send(
                "user-created",
                com.thanh.identityservice.dto.event.UserCreatedEvent.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build());

        var userCreationReponse = userMapper.toUserResponse(user);
        userCreationReponse.setId(profile.getResult().getId());

        return userCreationReponse;
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = Objects.requireNonNull(context.getAuthentication()).getName();

        User user = userRepository.findById(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        log.info("In method get Users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(String id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }
}
