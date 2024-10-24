package com.backend.sparkle.service;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.backend.sparkle.dto.MessageDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TextAnalyticsService {

    private TextAnalyticsClient textAnalyticsClient; // 텍스트 분석 클라이언트

    @Value("${azure.cognitiveservice.endpoint}")
    private String cognitiveAzureEndpoint;

    @Value("${azure.cognitiveservice.key}")
    private String cognitiveApiKey;

    @PostConstruct
    public void init() {
        log.info("cognitiveAzureEndpoint: "+ cognitiveAzureEndpoint);
        // 텍스트 분석 클라이언트 생성
        this.textAnalyticsClient = new TextAnalyticsClientBuilder()
                .credential(new AzureKeyCredential(cognitiveApiKey))
                .endpoint(cognitiveAzureEndpoint)
                .buildClient();
        
        log.info("TextAnalyticsClient 생성 완료");
    }

    // 핵심 구문 추출 기능
    public List<String> extractKeyPhrases(MessageDto.ImageGenerateRequestDto requestDto) {
        // KeyPhrasesCollection을 반환하며, 이를 List<String>으로 변환
        List<String> keyPhrasesList = new ArrayList<>();

        textAnalyticsClient.extractKeyPhrases(requestDto.getInputMessage()).forEach(keyPhrase -> {
            keyPhrasesList.add(keyPhrase); // 키워드 추가
            log.info("추출된 키워드: {}", keyPhrase); // 키워드 출력
        });

        log.info("키워드 추출 완료");

        return keyPhrasesList;
    }
}
