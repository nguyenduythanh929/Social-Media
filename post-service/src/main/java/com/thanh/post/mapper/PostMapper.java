package com.thanh.post.mapper;

import com.thanh.post.dto.response.PostResponse;
import com.thanh.post.entity.Post;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostMapper {
    PostResponse toPostResponse(Post post);
}
