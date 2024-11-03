package com.backend.sparkle.repository;

import com.backend.sparkle.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // 특정 userId에 해당하는 가장 최근에 생성된 Message 하나 가져오기
    Message findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
