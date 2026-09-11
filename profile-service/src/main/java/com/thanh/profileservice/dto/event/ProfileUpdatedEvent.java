package com.thanh.profileservice.dto.event;

import java.time.LocalDate;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileUpdatedEvent {
    String userId;
    String username;
    String firstName;
    String lastName;
    String avatar;
    String city;
    LocalDate dob;
}
