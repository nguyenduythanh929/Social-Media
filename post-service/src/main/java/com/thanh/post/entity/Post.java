package com.thanh.post.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Data
@Builder
@Document(value = "post")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Post {
    @MongoId
    String id;
    String userId;
    String content;
    Instant createdDate;
    Instant modifiedDate;
    List<String> mediaUrls;

    // Only changed with atomic $addToSet / $pull / $inc updates so concurrent requests never overwrite each other.
    // Both are null on posts created before likes/comments existed.
    Set<String> likedBy;
    Long commentCount;
}
