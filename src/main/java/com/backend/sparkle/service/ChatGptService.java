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

    public String translateText(String inputMessage) {
        // 요청 JSON 생성
        ChatGptDto.ChatRequestDto chatRequestDto = new ChatGptDto.ChatRequestDto(
                "gpt-4",
                4000,
                0.0,
                List.of(
                        new ChatGptDto.ChatRequestDto.Message("user", "아래 쓸 내용을 영어로 번역해줘"),
                        new ChatGptDto.ChatRequestDto.Message("assistant", "물론입니다! 번역할 내용을 제공해 주시면 영어로 번역해 드리겠습니다."),
                        new ChatGptDto.ChatRequestDto.Message("user", inputMessage)
                )
        );

        // WebClient를 사용하여 OpenAI API 호출하고 응답을 String으로 수신
        String responseBody = webClient.post()
                .bodyValue(chatRequestDto)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            // 응답 JSON 파싱
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choicesNode = root.path("choices");

            if (choicesNode.isArray() && choicesNode.size() > 0) {
                // "content" 필드에서 JSON 형식의 텍스트를 파싱하여 "englishText" 추출
                String content = choicesNode.get(0).path("message").path("content").asText();

                if (!content.isEmpty()) {
                    log.info("번역된 텍스트: " + content);
                    return content;
                } else {
                    log.info("번역된 텍스트 없음");
                    throw new RuntimeException("번역 결과를 가져오는 데 실패했습니다.");
                }
            } else {
                log.info("번역된 텍스트 없음");
                throw new RuntimeException("번역 결과를 가져오는 데 실패했습니다.");
            }
        } catch (Exception e) {
            log.error("JSON 파싱 중 오류 발생: ", e);
            throw new RuntimeException("JSON 파싱 오류", e);
        }
    }
}
