package com.thanh.chat.service;

import com.thanh.chat.dto.request.ChatMessageRequest;
import com.thanh.chat.dto.response.ChatMessageResponse;
import com.thanh.chat.entity.ChatMessage;
import com.thanh.chat.entity.ParticipantInfo;
import com.thanh.chat.exception.AppException;
import com.thanh.chat.exception.ErrorCode;
import com.thanh.chat.mapper.ChatMessageMapper;
import com.thanh.chat.repository.ChatMessageRepository;
import com.thanh.chat.repository.ConversationRepository;
import com.thanh.chat.repository.httpClient.ProfileClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMessageService {

    ChatMessageMapper chatMessageMapper;
    ConversationRepository conversationRepository;
    ProfileClient profileClient;
    ChatMessageRepository chatMessageRepository;
    SimpMessagingTemplate messagingTemplate;

    public List<ChatMessageResponse> getMessages(String conversationId) {
        String userId = Objects.requireNonNull(
                        SecurityContextHolder.getContext().getAuthentication())
                .getName();

        conversationRepository
                .findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND))
                .getParticipants()
                .stream()
                .filter(participantInfo -> userId.equals(participantInfo.getUserId()))
                .findAny()
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        var chatMessages = chatMessageRepository.findAllByConversationIdOrderByCreatedDateDesc(conversationId);

        return chatMessages.stream().map(this::toChatMessageResponse).toList();
    }

    public ChatMessageResponse create(ChatMessageRequest request) {
        String userId = Objects.requireNonNull(
                        SecurityContextHolder.getContext().getAuthentication())
                .getName();

        var conversation = conversationRepository
                .findById(request.getConversationId())
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        conversation.getParticipants().stream()
                .filter(participantInfo -> userId.equals(participantInfo.getUserId()))
                .findAny()
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        var userResponse = profileClient.getProfile(userId);

        if (Objects.isNull(userResponse)) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        var userInfo = userResponse.getResult();
        ChatMessage chatMessage = chatMessageMapper.toChatMessage(request);
        chatMessage.setSender(ParticipantInfo.builder()
                .userId(userId)
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName())
                .username(userInfo.getUsername())
                .avatar(userInfo.getAvatar())
                .build());
        chatMessage.setCreatedDate(Instant.now());

        chatMessageRepository.save(chatMessage);

        log.info("Sending message to {} participants", conversation.getParticipants().size());
        conversation.getParticipants().forEach(participant -> {
            ChatMessageResponse response = chatMessageMapper.toChatMessageResponse(chatMessage);
            response.setMe(participant.getUserId().equals(userId));
            log.info("Sending to userId={}, me={}", participant.getUserId(), response.isMe());
            messagingTemplate.convertAndSendToUser(
                    participant.getUserId(),
                    "/queue/messages",
                    response
            );
        });

        return toChatMessageResponse(chatMessage);
    }

    private ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage) {
        String userId = Objects.requireNonNull(
                        SecurityContextHolder.getContext().getAuthentication())
                .getName();

        ChatMessageResponse chatMessageResponse = chatMessageMapper.toChatMessageResponse(chatMessage);

        chatMessageResponse.setMe(userId.equals(chatMessage.getSender().getUserId()));

        return chatMessageResponse;
    }
}
