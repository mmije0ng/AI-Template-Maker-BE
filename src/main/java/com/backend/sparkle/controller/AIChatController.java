package com.backend.sparkle.controller;

import com.backend.sparkle.dto.AIChatDto;
import com.backend.sparkle.dto.CommonResponse;
import com.backend.sparkle.dto.MessageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Tag(name = "AI 챗봇", description = "AI 챗봇에 관한 API")
@RestController
@RequestMapping("/api/ai-chat")
public class AIChatController {
    @Operation(
            summary = "AI 챗봇 질문",
            description = "AI 챗봇에게 질문한 뒤 답변 결과를 응답 받는다",
            parameters = {
                    @Parameter(name = "userId", description = "사용자 PK", required = true, example = "1")
            }
    )
    @PostMapping("/{userId}")
    public ResponseEntity<CommonResponse<AIChatDto>> createAIChatMessage(@PathVariable Long userId, @RequestBody AIChatDto aiChatDto) {
        try {
            log.info("AI 챗봇 질문 요청 userId: {}", userId);

            aiChatDto.setChatMessage("특별한 기회를 놓치지 마세요!\n" +
                    "\n" +
                    "지금 바로 50% 할인 행사에 참여하세요!\n" +
                    "한정된 시간 동안 진행되는 이번 행사에서는 인기 상품을 절반 가격으로 만나볼 수 있습니다.\n");
            aiChatDto.setMine(false);

            return ResponseEntity.ok(CommonResponse.success("AI 챗봇 질문 응답 성공", aiChatDto));
        } catch (Exception e) {
            log.error("AI 챗봇 질문 요청 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(CommonResponse.fail("AI 챗봇 질문 실패"));
        }
    }
}
