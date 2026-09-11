package com.thanh.post.dto.event;

import java.time.Instant;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostCreatedEvent {
    String postId;
    String userId;
    String content;
    Instant createdDate;
}
