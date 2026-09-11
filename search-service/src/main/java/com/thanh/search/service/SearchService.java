package com.thanh.search.service;

import java.util.List;

import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import com.thanh.search.document.PostDocument;
import com.thanh.search.document.UserDocument;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SearchService {

    ElasticsearchOperations elasticsearchOperations;

    public List<UserDocument> searchUsers(String keyword) {
        Query query = NativeQuery.builder()
                .withQuery(q -> q.multiMatch(mm -> mm
                        .fields("username", "firstName", "lastName")
                        .query(keyword)
                        .fuzziness("AUTO")))
                .build();

        SearchHits<UserDocument> hits = elasticsearchOperations.search(query, UserDocument.class);

        return hits.stream()
                .map(SearchHit::getContent)
                .toList();
    }

    public List<PostDocument> searchPosts(String keyword) {
        Query query = NativeQuery.builder()
                .withQuery(q -> q.match(m -> m
                        .field("content")
                        .query(keyword)
                        .fuzziness("AUTO")))
                .build();

        SearchHits<PostDocument> hits = elasticsearchOperations.search(query, PostDocument.class);

        return hits.stream()
                .map(SearchHit::getContent)
                .toList();
    }
}
