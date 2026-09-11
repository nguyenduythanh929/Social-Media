package com.thanh.search.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.thanh.search.document.UserDocument;
import com.thanh.search.dto.event.UserCreatedEvent;
import com.thanh.search.repository.UserSearchRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserEventConsumer {

    UserSearchRepository userSearchRepository;

    @KafkaListener(topics = "user-created", groupId = "search-group", containerFactory = "userCreatedKafkaListenerContainerFactory")
    public void handleUserCreated(UserCreatedEvent event) {
        log.info("Received user-created event for userId: {}", event.getUserId());

        UserDocument doc = UserDocument.builder()
                .id(event.getUserId())
                .userId(event.getUserId())
                .username(event.getUsername())
                .email(event.getEmail())
                .build();

        userSearchRepository.save(doc);
        log.info("Indexed user: {}", event.getUsername());
    }
}
