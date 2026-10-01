package com.thanh.post.repository;

import com.thanh.post.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentRepository extends MongoRepository<Comment, String> {
    Page<Comment> findAllByPostId(String postId, Pageable pageable);

    void deleteAllByPostId(String postId);
}
