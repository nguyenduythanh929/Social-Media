package com.thanh.chat.dto.response;

import java.time.Instant;
import java.util.List;

import com.thanh.chat.entity.ParticipantInfo;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationResponse {
    String id;
    String type;
    String participantHash;
    String conversationName;
    String conversationAvatar;
    List<ParticipantInfo> participants;
    Instant createdDate;
    Instant modifiedDate;
}
