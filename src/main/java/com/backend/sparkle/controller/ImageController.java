package com.backend.sparkle.controller;

import com.backend.sparkle.dto.CommonResponse;
import com.backend.sparkle.dto.DalleRequestDto;
import com.backend.sparkle.dto.MessageDto;
import com.backend.sparkle.service.AzureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Tag(name = "Dalle 이미지 생성 컨트롤러", description = "Azure Dalle로 이미지 생성")
@RestController
@RequestMapping("/api/dalle")
public class ImageController {

    private final AzureService azureService;

    @Autowired
    public ImageController(AzureService azureService) {
        this.azureService = azureService;
    }

    @Operation(
            summary = "Dalle 이미지 생성",
            description = "프롬포트를 입력 받아 Azure Dalle로 3장의 이미지를 생성해서 반환한다."
    )
    @PostMapping("/generate")
    public ResponseEntity < CommonResponse<MessageDto.ImageGenerateResponseDto> > generateImage(@RequestBody MessageDto.ImageGenerateRequestDto requestDto) {
        // 시간 형식을 지정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.now(); // 요청 시작 시간 기록
        log.info("Dalle 이미지 생성 요청 시작 시간: {}", startTime.format(formatter));

        try {
            // block()을 사용하여 Mono를 동기적으로 변환
            MessageDto.ImageGenerateResponseDto responseDto = azureService.processPromptAndGenerateImage(requestDto);

            LocalDateTime endTime = LocalDateTime.now(); // 응답 완료 시간 기록
            log.info("Dalle 이미지 생성 요청 완료 시간: {}", endTime.format(formatter));

            // 소요 시간 계산 (초 단위)
            Duration duration = Duration.between(startTime, endTime);
            long seconds = duration.getSeconds();
            log.info("Dalle 이미지 생성 소요 시간: {} 초", seconds);

            return ResponseEntity.ok(CommonResponse.success("Dalle 이미지 생성 성공", responseDto));
        } catch (WebClientResponseException e) {
            log.error("Azure Dalle 이미지 생성 요청 오류");
            return ResponseEntity.status(e.getStatusCode()).body(CommonResponse.fail("Azure Dalle 이미지 생성 요청 오류\n" + e.getResponseBodyAsString()));
        } catch (RuntimeException e) {
            log.error("Dalle API로부터 응답이 null이거나 비어 있습니다.");

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(CommonResponse.fail(e.getMessage()));
        } catch (JSONException e){
            log.error("Dalle 이미지 생성 JSON 파싱 오류");

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(CommonResponse.fail("Dalle 이미지 생성 JSON 파싱 오류\n" +e.getMessage()));
        }
    }

//    @Operation(
//            summary = "Dalle 이미지 생성",
//            description = "프롬포트를 입력 받아 Azure Dalle로 3장의 이미지를 생성해서 반환한다."
//    )
//    @GetMapping("/generate")
//    public ResponseEntity<?> generateImage(@RequestBody MessageDto.ImageGenerateRequestDto requestDto) {
//        // 시간 형식을 지정
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        LocalDateTime startTime = LocalDateTime.now(); // 요청 시작 시간 기록
//        log.info("Dalle 이미지 생성 요청 시작 시간: {}", startTime.format(formatter));
//
//        try {
//            // block()을 사용하여 Mono를 동기적으로 변환
//            String response = dalleService.generateImage(requestDto).block();
//
//            LocalDateTime endTime = LocalDateTime.now(); // 응답 완료 시간 기록
//            log.info("Dalle 이미지 생성 요청 완료 시간: {}", endTime.format(formatter));
//
//            // 소요 시간 계산 (초 단위)
//            Duration duration = Duration.between(startTime, endTime);
//            long seconds = duration.getSeconds();
//            log.info("Dalle 이미지 생성 소요 시간: {} 초", seconds);
//
//            return ResponseEntity.ok(response);
//        } catch (WebClientResponseException e) {
//            LocalDateTime errorTime = LocalDateTime.now(); // 에러 발생 시간 기록
//            log.error("Dalle 이미지 생성 실패, prompt: {}", requestDto.getInputMessage());
//            log.error("Dalle 이미지 생성 요청 오류 메시지: {}", e.getMessage());
//
//            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
//        } catch (Exception e) {
//            LocalDateTime errorTime = LocalDateTime.now(); // 일반 에러 발생 시간 기록
//            log.error("Dalle 이미지 생성 요청 오류 메시지: {}", e.getMessage());
//
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//    }
}
