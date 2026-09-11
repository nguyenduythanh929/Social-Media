package com.thanh.chat.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.thanh.chat.dto.response.ConversationResponse;
import com.thanh.chat.entity.Conversation;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    ConversationResponse toConversationResponse(Conversation conversation);

    List<ConversationResponse> toConversationResponseList(List<Conversation> conversations);
}
