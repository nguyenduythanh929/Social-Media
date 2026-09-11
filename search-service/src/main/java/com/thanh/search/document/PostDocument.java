package com.thanh.search.document;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(indexName = "posts")
public class PostDocument {

    @Id
    String id;

    @Field(type = FieldType.Keyword)
    String postId;

    @Field(type = FieldType.Keyword)
    String userId;

    @Field(type = FieldType.Text)
    String username;

    @Field(type = FieldType.Text)
    String content;

    @Field(type = FieldType.Date)
    Instant createdDate;
}
