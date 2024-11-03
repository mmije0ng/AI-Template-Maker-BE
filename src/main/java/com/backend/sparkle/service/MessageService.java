package com.backend.sparkle.service;

import com.backend.sparkle.dto.MessageDto;
import com.backend.sparkle.entity.Message;
import com.backend.sparkle.entity.User;
import com.backend.sparkle.repository.MessageRepository;
import com.backend.sparkle.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Slf4j
@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository, UserRepository userRepository){
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }
    
    // 사용자가 전송한 메시지 내역 저장
    public void addMessage(Long userId, MessageDto.SendRequestDto requestDto){
        // User 객체 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다. userId: " + userId));

        // 생성자를 사용하여 Message 엔티티 생성
        Message message = new Message(user, requestDto);
        messageRepository.save(message);
    }

    // 문자 발송 완료 후 메시지 조회
    public MessageDto.SendCompleteResponseDto getSendCompleteMessage(Long userId) {
        Message message = messageRepository.findTopByUserIdOrderByCreatedAtDesc(userId);

        if (message == null)
            throw new NoSuchElementException("해당 userId로 최근 메시지를 찾을 수 없습니다. userId: " + userId);

        MessageDto.SendCompleteResponseDto responseDto = MessageDto.SendCompleteResponseDto.builder()
                .imageURL(message.getMessageImageUrl())
                .sendDateTime(message.getCreatedAt().toString())
                .sendPhoneNumber(message.getSendPhoneNumber())
                .build();

        return responseDto;
    }
}
