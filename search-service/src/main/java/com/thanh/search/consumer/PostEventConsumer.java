package com.thanh.search.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.thanh.search.document.PostDocument;
import com.thanh.search.dto.event.PostCreatedEvent;
import com.thanh.search.dto.event.PostDeletedEvent;
import com.thanh.search.repository.PostSearchRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostEventConsumer {

    PostSearchRepository postSearchRepository;

    // post-updated carries the same payload; saving by id overwrites the existing document (upsert)
    @KafkaListener(
            topics = {"post-created", "post-updated"},
            groupId = "search-group",
            containerFactory = "postCreatedKafkaListenerContainerFactory")
    public void handlePostCreated(PostCreatedEvent event) {
        log.info("Received post created/updated event for postId: {}", event.getPostId());

        PostDocument doc = PostDocument.builder()
                .id(event.getPostId())
                .postId(event.getPostId())
                .userId(event.getUserId())
                .content(event.getContent())
                .createdDate(event.getCreatedDate())
                .build();

        postSearchRepository.save(doc);
        log.info("Indexed post: {}", event.getPostId());
    }

    @KafkaListener(topics = "post-deleted", groupId = "search-group", containerFactory = "postDeletedKafkaListenerContainerFactory")
    public void handlePostDeleted(PostDeletedEvent event) {
        log.info("Received post-deleted event for postId: {}", event.getPostId());

        postSearchRepository.deleteById(event.getPostId());
        log.info("Removed post from index: {}", event.getPostId());
    }
}
