package com.thanh.search.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.thanh.search.document.PostDocument;

import java.util.Optional;

public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, String> {
    Optional<PostDocument> findByPostId(String postId);
}
