package com.backend.sparkle.controller;

import com.backend.sparkle.dto.CommonResponse;
import com.backend.sparkle.dto.MessageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Tag(name = "문자 보내기 페이지", description = "문자 보내기 및 이미지 생성에 관한 API")
@RestController
@RequestMapping("/api/message-send")
public class MessageSendController {

    // 메시지 및 키워드 입력 후 이미지 생성
    @Operation(summary = "메시지 및 키워드 입력 후 이미지 생성",
            description = "사용자가 메시지 및 키워드를 입력 후 이미지 생성하기 버튼을 클릭하여 3개의 이미지 생성")
    @PostMapping("/generate/{userId}")
    public CommonResponse<?> createImages(@PathVariable Long userId, @RequestBody MessageDto.ImageGenerateRequestDto requestDto) {
        try {
            log.info("이미지 생성 요청 userId: {}", userId);

            // 서비스 메서드에서 이미지 생성 로직을 처리해야 함
            // ArrayList 인스턴스 생성 및 데이터 추가
            List<String> imageUrlList = new ArrayList<>();
            imageUrlList.add("https://i.pinimg.com/564x/48/3d/a7/483da78ca17fa011004bac70b7e7c763.jpg");
            imageUrlList.add("https://i.pinimg.com/564x/38/73/51/387351a404a2dcf47ada6a138b7a14e7.jpg");
            imageUrlList.add("https://i.pinimg.com/564x/f0/e0/9c/f0e09cba73d689fc2c0ef01bbbbeae1a.jpg");

            // 이미지 생성 응답 객체 생성
            MessageDto.ImageGenerateResponseDto responseDto = MessageDto.ImageGenerateResponseDto.builder()
                    .imageUrlList(imageUrlList)
                    .build();

            return CommonResponse.success("이미지 생성 성공", responseDto);
        } catch (Exception e) { // 예외 처리 필요
            log.error("이미지 생성 실패: {}", e.getMessage());
            return CommonResponse.fail(HttpStatus.NOT_FOUND, "이미지 생성 실패");
        }
    }
}
