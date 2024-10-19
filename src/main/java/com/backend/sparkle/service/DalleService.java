package com.backend.sparkle.service;

import com.backend.sparkle.dto.DalleRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class DalleService {

    private final WebClient webClient;

    @Value("${azure.dalle.endpoint}")
    private String azureEndpoint;

    @Value("${azure.dalle.key}")
    private String apiKey;

    @Autowired
    public DalleService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<String> generateImage(String prompt) throws WebClientResponseException {
        DalleRequestDto requestDto = DalleRequestDto.builder()
                .prompt(prompt)
                .size("1024x1024") //추후 사이즈 수정 필요
                .n(1)
                .quality("hd") // prompt 만져가면서 수정 필요
                .style("vivid") // prompt 만져가면서 수정 필요
                .build();

        log.info("Dalle 이미지 생성 요청, prompt: {}", prompt);

        return webClient.post()
                .uri(azureEndpoint)
                .header("api-key", apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestDto)  // 프롬프트를 포함한 요청 DTO 전송
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(WebClientResponseException.class, e -> {
                    log.error("Dalle 이미지 생성 중 오류 발생: {}", e.getMessage());
                });
    }
}
