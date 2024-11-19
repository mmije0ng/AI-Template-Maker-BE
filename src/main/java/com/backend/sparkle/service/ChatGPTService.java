package com.backend.sparkle.service;

import com.backend.sparkle.dto.ChatGptDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.service.OpenAiService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class ChatGPTService {

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.api.key}")
    private String apiKey;

    private WebClient webClient;
    private final ObjectMapper objectMapper;
    private OpenAiService openAiService;

    public ChatGPTService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public OpenAiService getOpenAiService() {
        return new OpenAiService(apiKey, Duration.ofSeconds(30));
    }

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();

        this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(30));
    }

    // 입력 메시지를 영어 또는 한국어로 번역
    public String translateText(String inputMessage, String targetLanguage) {
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
                    return content;
                }
            }
        } catch (Exception e) {
            log.error("JSON 파싱 중 오류 발생: ", e);
        }
        throw new RuntimeException("번역 실패: " + targetLanguage + "로 변환할 수 없습니다.");
    }

    // DALL-E API를 사용해 이미지를 생성하고 URL 반환

    public String generateImageWithDalle(String prompt, String size) {
        try {
            // 이미지 생성 요청을 생성
            CreateImageRequest createImageRequest = CreateImageRequest.builder()
                    .prompt(prompt)
                    .size(size) // 이미지 크기 설정
                    .n(4) // 생성할 이미지 수 설정
                    .build();

            // OpenAI API를 통해 이미지 생성 요청 수행
            var response = openAiService.createImage(createImageRequest);

            // 이미지 URL 추출
            String url = response.getData().get(0).getUrl();

            // JSON 응답에서 수정된 프롬프트(revised_prompt) 추출
            Optional<String> revisedPrompt = response.getData().stream()
                    .map(data -> {
                        try {
                            JsonNode jsonNode = objectMapper.readTree(data.toString());
                            return jsonNode.path("revised_prompt").asText();
                        } catch (Exception e) {
                            log.error("프롬프트 추출 중 오류 발생", e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .findFirst();

            // 수정된 프롬프트가 있을 경우 로그에 기록
            revisedPrompt.ifPresent(rp -> log.info("수정된 DALL-E 프롬프트: {}", rp));

            log.info("생성된 DALL-E 이미지 URL: {}", url);
            return url;

        } catch (Exception e) {
            log.error("DALL-E 이미지 생성 중 오류 발생: ", e);
            throw new RuntimeException("DALL-E 이미지 생성 실패", e);
        }
    }
}
