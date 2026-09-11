package com.thanh.identityservice.DTO.Request;

import java.time.LocalDate;
import java.util.List;

import com.thanh.identityservice.Validator.DobConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String password;
    String firstName;
    String lastName;
    String username;
    String email;

    @DobConstraint(min = 4, message = "INVALID_DOB")
    LocalDate dob;

    List<String> roles;
}
