package com.thanh.search.document;

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
@Document(indexName = "users")
public class UserDocument {

    @Id
    String id;

    @Field(type = FieldType.Keyword)
    String userId;

    @Field(type = FieldType.Text)
    String username;

    @Field(type = FieldType.Text)
    String firstName;

    @Field(type = FieldType.Text)
    String lastName;

    @Field(type = FieldType.Keyword)
    String email;

    @Field(type = FieldType.Keyword)
    String avatar;

    @Field(type = FieldType.Keyword)
    String city;
}
