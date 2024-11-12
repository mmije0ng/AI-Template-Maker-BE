package com.backend.sparkle.controller;

import com.backend.sparkle.dto.CommonResponse;
import com.backend.sparkle.dto.MessageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Tag(name = "문자 내역 페이지", description = "사용자가 보낸 문자 내역에 관한 API")
@RestController
@RequestMapping("/api/message/history")
public class MessageHistoryController {

    @Operation(
            summary = "문자 내역 조회",
            description = "사용자의 userId와 page 번호를 입력받아, 페이지 번호에 맞는 문자 내역을 3개씩 최신순으로 가져온다.",
            parameters = {
                    @Parameter(name = "userId", description = "사용자 PK", required = true, example = "1"),
                    @Parameter(name = "pageNumber", description = "페이지 번호", required = true, example = "0")
            }
    )
    @GetMapping()
    public ResponseEntity<CommonResponse<Page<MessageDto.HistoryResponseDto>>> getMessageHistory(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "pageNumber") Integer pageNumber) {
        try {
            log.info("문자 내역 조회 - userId: {}, page 번호: {}", userId, pageNumber);

            // 페이지 설정, 최신순으로 3개씩 가져오기
            Pageable pageable = PageRequest.of(pageNumber, 3);

            // 실제 서비스에서는 DB에서 페이징 처리된 문자 내역을 조회해야 함
            // 여기서는 예시로 임의의 데이터 생성
            List<MessageDto.HistoryResponseDto> messageHistoryList = new ArrayList<>();
            messageHistoryList.add(new MessageDto.HistoryResponseDto("https://i.pinimg.com/564x/48/3d/a7/483da78ca17fa011004bac70b7e7c763.jpg", "2024-10-12", "010-1234-5678"));
            messageHistoryList.add(new MessageDto.HistoryResponseDto("https://i.pinimg.com/564x/38/73/51/387351a404a2dcf47ada6a138b7a14e7.jpg", "2024-10-11", "010-8765-4321"));
            messageHistoryList.add(new MessageDto.HistoryResponseDto("https://i.pinimg.com/564x/f0/e0/9c/f0e09cba73d689fc2c0ef01bbbbeae1a.jpg", "2024-10-11", "010-1111-2222"));
            messageHistoryList.add(new MessageDto.HistoryResponseDto("https://i.pinimg.com/736x/14/02/9b/14029bc5f6735407bfadc065fa482bfe.jpg", "2024-10-10", "010-1111-2222"));

            // 페이징에 맞는 데이터 서브리스트 추출 (나중에 jpa로 pageSize에 맞는 데이터만 추출할 수 있음)
            int start = Math.min((int) pageable.getOffset(), messageHistoryList.size());
            int end = Math.min((start + pageable.getPageSize()), messageHistoryList.size());
            List<MessageDto.HistoryResponseDto> subList = messageHistoryList.subList(start, end);

            // 페이징 처리된 리스트로 변환
            Page<MessageDto.HistoryResponseDto> responseDto = new PageImpl<>(subList, pageable, messageHistoryList.size());

            return ResponseEntity.ok(CommonResponse.success("문자 내역 조회 성공", responseDto));
        } catch (Exception e) {
            log.error("문자 내역 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(CommonResponse.fail("문자 내역 조회 실패"));
        }
    }
}
