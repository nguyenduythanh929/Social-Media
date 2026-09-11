package com.thanh.search.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.thanh.search.document.UserDocument;
import com.thanh.search.dto.event.ProfileUpdatedEvent;
import com.thanh.search.repository.UserSearchRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProfileEventConsumer {

    UserSearchRepository userSearchRepository;

    @KafkaListener(topics = "profile-updated", groupId = "search-group", containerFactory = "profileUpdatedKafkaListenerContainerFactory")
    public void handleProfileUpdated(ProfileUpdatedEvent event) {
        log.info("Received profile-updated event for userId: {}", event.getUserId());

        // Tìm UserDocument đã tồn tại, nếu chưa có thì tạo mới
        UserDocument doc = userSearchRepository
                .findByUserId(event.getUserId())
                .orElse(UserDocument.builder()
                        .id(event.getUserId())
                        .userId(event.getUserId())
                        .build());

        doc.setUsername(event.getUsername());
        doc.setFirstName(event.getFirstName());
        doc.setLastName(event.getLastName());
        doc.setAvatar(event.getAvatar());
        doc.setCity(event.getCity());

        userSearchRepository.save(doc);
        log.info("Updated index for user: {}", event.getUsername());
    }
}
