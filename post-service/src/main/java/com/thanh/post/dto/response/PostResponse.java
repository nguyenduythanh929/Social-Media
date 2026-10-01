package com.thanh.post.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostResponse {
    String id;
    String content;
    List<String> mediaUrls;
    String userId;
    String username;
    String avatar;
    String created;
    Instant createdDate;
    Instant modifiedDate;
    boolean edited;
    long likeCount;
    boolean likedByMe;
    long commentCount;
    boolean ownedByMe;
}
