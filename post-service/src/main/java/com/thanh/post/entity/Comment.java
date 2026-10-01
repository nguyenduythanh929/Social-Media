package com.thanh.post.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "comment")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comment {
    @MongoId
    String id;

    @Indexed
    String postId;

    String userId;
    String content;
    Instant createdDate;
}
