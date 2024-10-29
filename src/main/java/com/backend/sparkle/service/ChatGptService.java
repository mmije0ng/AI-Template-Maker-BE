package com.backend.sparkle.service;

import com.backend.sparkle.dto.ChatGptDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
public class ChatGptService {

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.api.key}")
    private String apiKey;

    private WebClient webClient;
    private final ObjectMapper objectMapper;

    public ChatGptService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
        log.info("ChatGpt WebClient 초기화 완료. API URL: {}", apiUrl);
    }

    /**
     * 번역 메서드: 입력 메시지를 지정한 언어로 번역합니다.
     *
     * @param inputMessage 번역할 텍스트
     * @param targetLanguage 대상 언어 ("en" 또는 "ko")
     * @return 번역된 텍스트
     */
    public String translateText(String inputMessage, String targetLanguage) {
        // 대상 언어에 따른 프롬프트 설정
        String prompt = targetLanguage.equalsIgnoreCase("en")
                ? "아래 내용을 영어로 번역해줘"
                : "아래 영어 텍스트를 한국어로 번역해줘";

        ChatGptDto.ChatRequestDto chatRequestDto = new ChatGptDto.ChatRequestDto(
                "gpt-4",
                4000,
                0.0,
                List.of(
                        new ChatGptDto.ChatRequestDto.Message("user", prompt),
                        new ChatGptDto.ChatRequestDto.Message("user", inputMessage)
                )
        );

        String responseBody = webClient.post()
                .bodyValue(chatRequestDto)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choicesNode = root.path("choices");

            if (choicesNode.isArray() && choicesNode.size() > 0) {
                String content = choicesNode.get(0).path("message").path("content").asText();
                if (!content.isEmpty()) {
//                    log.info("번역된 텍스트: " + content);
                    return content;
                }
            }
        } catch (Exception e) {
            log.error("JSON 파싱 중 오류 발생: ", e);
        }
        throw new RuntimeException("번역 실패: " + targetLanguage + "로 변환할 수 없습니다.");
    }
}
