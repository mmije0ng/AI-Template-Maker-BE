package com.backend.sparkle.service;

import com.backend.sparkle.dto.MessageDto;
import com.backend.sparkle.entity.Message;
import com.backend.sparkle.entity.User;
import com.backend.sparkle.repository.MessageRepository;
import com.backend.sparkle.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;

@Slf4j
@Transactional(readOnly = true)
@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    public MessageService(MessageRepository messageRepository, UserRepository userRepository){
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }
    
    // 사용자가 전송한 메시지 내역 저장
    @Transactional
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
            throw new NoSuchElementException("최근 메시지를 찾을 수 없습니다. userId: " + userId);

        String formattedDateTime = message.getCreatedAt().format(DATE_TIME_FORMATTER);

        return MessageDto.SendCompleteResponseDto.builder()
                .imageURL(message.getMessageImageUrl())
                .sendMessage(message.getMessageContent())
                .sendDateTime(formattedDateTime)
                .sendPhoneNumber(message.getSendPhoneNumber())
                .build();
    }
}
