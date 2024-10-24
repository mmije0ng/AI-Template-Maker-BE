package com.backend.sparkle.service;

import com.backend.sparkle.dto.DalleRequestDto;
import com.backend.sparkle.dto.MessageDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.StringJoiner;

@Slf4j
@Service
public class DalleService {

    private final WebClient webClient;

    @Value("${azure.dalle.endpoint}")
    private String dalleAzureEndpoint;

    @Value("${azure.dalle.api-version}")
    private String dalleApiVersion;

    @Value("${azure.dalle.key}")
    private String dalleApiKey;

    private String dalleURI;

    @Autowired
    public DalleService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @PostConstruct
    public void init() {
        log.info("dalleAzureEndpoint: {}", dalleAzureEndpoint);

        this.dalleURI = String.format("%s?api-version=%s", dalleAzureEndpoint, dalleApiVersion);
        log.info("Dalle URI: {}", dalleURI);
    }

    public Mono<String> generateImage(MessageDto.ImageGenerateRequestDto requestDto, List<String> keyPhrases) throws WebClientResponseException {
        StringBuilder prompt = new StringBuilder();

        // 1. 이미지 스타일에 대한 설명
        prompt.append("An illustration style image, with a minimalist setting. ");
//        prompt.append("A highly detailed and photo-realistic image.");
        prompt.append("The image should be clean and simple, focusing on the key elements. ");

        // 2. 배제 조건
        prompt.append("No text or human figures should be included. ");

        // 3. 키워드 추가
        prompt.append("Focus on the following keywords: ");
        StringJoiner promptWithKeywords = new StringJoiner(", ");
        keyPhrases.forEach(promptWithKeywords::add);
        prompt.append(promptWithKeywords.toString()).append(". ");

        // 4. 사용자가 입력한 정보 추가
        prompt.append("Atmosphere: ").append(requestDto.getStyle()).append(". ");
        prompt.append("Seasonal theme: ").append(requestDto.getSeason()).append(". ");
//        prompt.append("Message: ").append(requestDto.getInputMessage()).append(". ");

        DalleRequestDto dalleRequestDto = DalleRequestDto.builder()
                .prompt(prompt.toString()) // 최종 프롬프트 문자열
                .size("1024x1024") // 추후 사이즈 수정 필요
                .n(1)
                .quality("hd") // prompt 만져가면서 수정 필요
                .style("natural") // prompt 만져가면서 수정 필요
                .build();

        log.info("Dalle 이미지 생성 요청, prompt: {}", prompt.toString());

        return webClient.post()
                .uri(dalleURI)
                .header("api-key", dalleApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(dalleRequestDto)  // 프롬프트를 포함한 요청 DTO 전송
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(WebClientResponseException.class, e -> {
                    log.error("Dalle 이미지 생성 중 오류 발생: {}", e.getMessage());
                });
    }
}
