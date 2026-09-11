package com.thanh.search.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.thanh.search.document.UserDocument;

import java.util.Optional;

public interface UserSearchRepository extends ElasticsearchRepository<UserDocument, String> {
    Optional<UserDocument> findByUserId(String userId);
}
