package com.backend.sparkle.service;

import com.backend.sparkle.dto.DalleRequestDto;
import com.backend.sparkle.dto.MessageDto;
import com.backend.sparkle.strategy.mood.MoodStrategy;
import com.backend.sparkle.strategy.mood.MoodStrategyFactory;
import com.backend.sparkle.strategy.season.SeasonStrategy;
import com.backend.sparkle.strategy.season.SeasonStrategyFactory;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

    private static final List<String> styles = List.of(
            "An illustration style image with a minimalist setting.",
            "A highly detailed and photo-realistic image.",
            "An image in the style of Van Gogh.",
            "An anime style image with a minimalist setting.",
            "A digital art style image.",
            "A pencil sketch style image."
    );

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

    public MessageDto.ImageGenerateResponseDto generateImages(MessageDto.ImageGenerateRequestDto requestDto, List<String> keyPhrases)
            throws WebClientResponseException {

        // 분위기와 계절 전략을 선택하고 변환
        MoodStrategy moodStrategy = MoodStrategyFactory.getMoodStrategy(requestDto.getMood());
        SeasonStrategy seasonStrategy = SeasonStrategyFactory.getSeasonStrategy(requestDto.getSeason());

        // 변환된 분위기와 계절을 사용하여 Dalle 이미지를 생성
        String transformedMood = moodStrategy.applyMood();
        String transformedSeason = seasonStrategy.applySeason();


        // 각 스타일에 대해 비동기 요청을 병렬로 수행
        List<CompletableFuture<Result>> futures = styles.parallelStream()
                .map(style -> CompletableFuture.supplyAsync(() -> generateImageWithDalle(keyPhrases, style, transformedMood, transformedSeason)))
                .toList();

        // 모든 비동기 요청의 결과를 리스트로 수집
        List<Result> results = futures.stream()
                .map(CompletableFuture::join) // 모든 CompletableFuture가 완료될 때까지 대기
                .toList();

        // 결과 리스트에서 필요한 필드 수집
        List<String> generatedImageUrls = results.stream().map(Result::getUrl).collect(Collectors.toList()); // 이미지 생성 리스트
        List<String> revisedPrompts = results.stream().map(Result::getRevisedPrompt).collect(Collectors.toList()); // 수정된 프롬포트 리스트
        List<String> imageStyles = results.stream().map(Result::getStyle).collect(Collectors.toList()); // 이미지 스타일 리스트

        // ImageGenerateResponseDto 반환
        return MessageDto.ImageGenerateResponseDto.builder()
                .generatedImageUrls(generatedImageUrls)
                .revisedPrompts(revisedPrompts)
                .imageStyles(imageStyles)
                .build();
    }

    // 스타일별 이미지 생성 작업
    private Result generateImageWithDalle(List<String> keyPhrases, String style, String transformedMood, String transformedSeason) {        StringBuilder prompt = new StringBuilder();

        // 1. 스타일별 프롬프트 설정
        prompt.append(style);

        // 2. 이미지의 요소 설명
        prompt.append(" The image should be clean and simple, focusing on the essential elements.");

        // 3. 배제 조건 추가
        prompt.append(" No text or human figures should be included. ");

        // 4. 키워드 추가
        prompt.append("Focus on the following keywords: ");
        StringJoiner promptWithKeywords = new StringJoiner(", ");
        keyPhrases.forEach(promptWithKeywords::add);
        prompt.append(promptWithKeywords.toString()).append(". ");

        // 5. 사용자가 입력한 정보 추가
        prompt.append("Atmosphere: ").append(transformedMood).append(". ");
        prompt.append("Creating an image with this atmosphere. ");
        prompt.append("Seasonal theme: ").append(transformedSeason).append(". ");
        prompt.append("Using this season as the background theme. ");

        DalleRequestDto dalleRequestDto = DalleRequestDto.builder()
                .prompt(prompt.toString()) // 최종 프롬프트 문자열
                .size("1024x1024") // 추후 사이즈 수정 필요
                .n(1)
                .quality("hd") // prompt 만져가면서 수정 필요
                .style("vivid") // natural or vivid
                .build();

        log.info("Dalle 이미지 생성 요청, prompt: {}", prompt.toString());

        try {
            // 6. WebClient를 사용하여 Dalle API 요청 및 응답 파싱
            String responseBody = webClient.post()
                    .uri(dalleURI)
                    .header("api-key", dalleApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(dalleRequestDto)
                    .retrieve()
                    .bodyToMono(String.class) // JSON 응답을 String으로 수신
                    .block();

            // Dalle API로부터 응답이 비어 있는 경우 예외 발생
            if (responseBody == null || responseBody.isEmpty()) {
                throw new RuntimeException("Dalle API로부터 응답이 null이거나 비어 있습니다.");
            }

            // JSON 문자열을 JSONObject로 변환 (JSONException 발생 가능)
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray dataArray = jsonResponse.getJSONArray("data");
            JSONObject dataObject = dataArray.getJSONObject(0);
            String revisedPrompt = dataObject.getString("revised_prompt");
            String url = dataObject.getString("url");

            // 생성된 url을 Azure blob storage에 저장하는 로직


            // 현재 스타일과 함께 결과 반환
            return new Result(revisedPrompt, url, style);

        } catch (JSONException e) {
            log.error("JSON 파싱 중 오류 발생", e);
            throw new RuntimeException("JSON 파싱 오류", e);
        }
    }

    // 요청 결과를 담을 내부 클래스 정의
    @Getter
    @AllArgsConstructor
    private static class Result {
        private final String revisedPrompt;
        private final String url;
        private final String style;
    }
}