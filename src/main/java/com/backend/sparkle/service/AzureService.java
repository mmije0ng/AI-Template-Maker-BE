package com.backend.sparkle.service;

import com.backend.sparkle.dto.DalleRequestDto;
import com.backend.sparkle.dto.MessageDto;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class AzureService {

    private final TextAnalyticsService textAnalyticsService;
    private final DalleService dalleService;

    @Autowired
    public AzureService(TextAnalyticsService textAnalyticsService, DalleService dalleService) {
        this.textAnalyticsService = textAnalyticsService;
        this.dalleService = dalleService;
    }
    
    // 키워드 추출을 적용한 이미지 생성
    public MessageDto.ImageGenerateResponseDto processPromptAndGenerateImage(@RequestBody MessageDto.ImageGenerateRequestDto requestDto)
            throws WebClientResponseException, JSONException, RuntimeException{
        // Text Analytics에서 키 구문 추출
        List<String> keyPhrases = textAnalyticsService.extractKeyPhrases(requestDto);
        log.info("Text Analytics를 이용한 keyword 추출 결과");

        // DALL·E API 호출을 통해 이미지 생성
        log.info("DALL·E API 호출을 통해 이미지 생성");
        return dalleService.generateImages(requestDto, keyPhrases);
    }
}
