package com.thanh.chat.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.thanh.chat.dto.request.ChatMessageRequest;
import com.thanh.chat.dto.response.ChatMessageResponse;
import com.thanh.chat.entity.ChatMessage;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {
    ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage);

    ChatMessage toChatMessage(ChatMessageRequest request);

    List<ChatMessageResponse> toChatMessageResponses(List<ChatMessage> chatMessages);
}
