package com.backend.sparkle.service;

import com.backend.sparkle.dto.DalleRequestDto;
import com.backend.sparkle.dto.MessageDto;
import com.backend.sparkle.strategy.mood.MoodStrategy;
import com.backend.sparkle.strategy.mood.MoodStrategyFactory;
import com.backend.sparkle.strategy.season.SeasonStrategy;
import com.backend.sparkle.strategy.season.SeasonStrategyFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ImageService {
    private final WebClient webClient;
    private final BlobService blobService;
    private final ChatGptService chatGptService;
    private final TextAnalyticsService textAnalyticsService;

    @Value("${azure.dalle.endpoint}")
    private String dalleAzureEndpoint;

    @Value("${azure.dalle.api-version}")
    private String dalleApiVersion;

    @Value("${azure.dalle.key}")
    private String dalleApiKey;

    private String dalleURI;

    // 이미지 생성에 사용될 다양한 스타일 목록 정의 - 일러스트, 사실적, 애니메이션
    private static final List<String> styles = List.of(
            "Minimalist illustration style with pastel tones, featuring a simple and clean composition with soft shading",
            "Highly detailed and realistic photographic style, a high-resolution image with vivid realism and fine detail",
            "Animation style with bright and vibrant colors, presenting characters and background in a simplified form"
    );

    @Autowired
    public ImageService(WebClient.Builder webClientBuilder, BlobService blobService, ChatGptService chatGptService, TextAnalyticsService textAnalyticsService) {
        this.webClient = webClientBuilder.build();
        this.blobService = blobService;
        this.chatGptService = chatGptService;
        this.textAnalyticsService = textAnalyticsService;
    }

    @PostConstruct
    public void init() {
        this.dalleURI = String.format("%s?api-version=%s", dalleAzureEndpoint, dalleApiVersion);
    }

    // 이미지 생성 요청 메소드
    public MessageDto.GeneratedImageMessageResponseDto generateImages(MessageDto.ImageGenerateRequestDto requestDto) {
        // Step 1: 분위기 및 계절 키워드 영어로 변환
        String transformedMood = transformMood(requestDto.getMood());
        String transformedSeason = transformSeason(requestDto.getSeason());

        // Step 2: 광고 메시지 생성 비동기로 처리
        CompletableFuture<String> advertiseMessageFuture = generateAdvertiseMessageAsync(requestDto.getInputMessage());

        // Step 3: 키워드 추출 (Azure Text Analytics와 ChatGPT 번역 사용)
        List<String> keyPhrases = extractAndTranslateKeywords(requestDto);

        // Step 4: 이미지 생성 비동기로 요청
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.now();
        List<CompletableFuture<String>> imageFutures = generateImagesAsync(requestDto, keyPhrases, transformedMood, transformedSeason);

        // Step 5: 모든 비동기 작업 완료 후 결과 결합
        CompletableFuture<Void> allOf = CompletableFuture.allOf(imageFutures.toArray(new CompletableFuture[0]));

        return allOf.thenCombine(advertiseMessageFuture, (v, advertiseMessage) -> {
            List<String> generatedImageUrls = imageFutures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            logProcessingTime(startTime, formatter);

            return MessageDto.GeneratedImageMessageResponseDto.builder()
                    .generatedImageUrls(generatedImageUrls)
                    .advertiseMessage(advertiseMessage)
                    .build();
        }).join();
    }

    // 분위기 키워드 영어로 번역
    private String transformMood(String mood) {
        MoodStrategy styleStrategy = MoodStrategyFactory.getMoodStrategy(mood);
        return styleStrategy.applyMood();
    }

    // 계절 키워드 영어로 번역
    private String transformSeason(String season) {
        SeasonStrategy seasonStrategy = SeasonStrategyFactory.getSeasonStrategy(season);
        return seasonStrategy.applySeason();
    }

    // 비동기로 OpenAI를 이용하여 광고 메시지 생성
    private CompletableFuture<String> generateAdvertiseMessageAsync(String inputMessage) {
        return CompletableFuture.supplyAsync(() -> {
            String message = chatGptService.generateMessage(inputMessage);
            log.info("생성된 광고 메시지: {}", message);
            return message;
        });
    }

    // 발송 목적 및 내용을 영어로 번역하여 Azure Text Analytics를 이용해 키워드 추출
    private List<String> extractAndTranslateKeywords(MessageDto.ImageGenerateRequestDto requestDto) {
        List<String> keyPhrases = requestDto.getKeyWordMessage().stream()
                .map(keyword -> chatGptService.translateText(keyword, "en"))
                .collect(Collectors.toList());
        keyPhrases.addAll(textAnalyticsService.extractKeyPhrases(requestDto.getInputMessage()));
        return keyPhrases;
    }

    // 스타일별로 이미지 Dalle 이미지 생성 비동기로 수행
    private List<CompletableFuture<String>> generateImagesAsync(
            MessageDto.ImageGenerateRequestDto requestDto,
            List<String> keyPhrases,
            String transformedMood,
            String transformedSeason
    ) {
        return styles.parallelStream()
                .map(style -> retryGenerateImage(style, keyPhrases, requestDto.getInputMessage(), transformedMood, transformedSeason))
                .collect(Collectors.toList());
    }

    // 이미지 생성 소요 시간 출력
    private void logProcessingTime(LocalDateTime startTime, DateTimeFormatter formatter) {
        LocalDateTime endTime = LocalDateTime.now();
        log.info("Dalle 이미지 생성 요청 완료 시간: {}", endTime.format(formatter));
        Duration duration = Duration.between(startTime, endTime);
        log.info("Dalle 이미지 생성 소요 시간: {} 초", duration.getSeconds());
    }

    // 이미지 생성 요청 재시도
    private CompletableFuture<String> retryGenerateImage(String imageStyle, List<String> keyPhrases, String inputMessage, String mood, String season) {
        return CompletableFuture.supplyAsync(() -> {
            int maxRetries = 5;
            int retryCount = 0;
            while (retryCount < maxRetries) {
                try {
                    return generateImageWithDalle(imageStyle, keyPhrases, inputMessage, mood, season).join();
                } catch (Exception e) {
                    retryCount++;
                    log.warn(e.getMessage());
                    log.warn("{}초 후 재시도 ({}/{})", retryCount * 5, retryCount, maxRetries);
                    try {
                        TimeUnit.SECONDS.sleep(retryCount * 5);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            throw new RuntimeException("Dalle API 요청이 여러 번 실패했습니다.");
        });
    }

    // DALL-E API에 이미지 생성 요청
    private CompletableFuture<String> generateImageWithDalle(String imageStyle, List<String> keyPhrases, String inputMessage, String mood, String season) {
        String prompt = generatePrompt(imageStyle, keyPhrases, inputMessage, mood, season);
        DalleRequestDto dalleRequestDto = DalleRequestDto.builder()
                .prompt(prompt)
                .size("1024x1792")
                .n(1)
                .quality("standard")
                .style("vivid")
                .build();

        log.info("Dalle 이미지 생성 요청, prompt: {}", prompt);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String responseBody = webClient.post()
                        .uri(dalleURI)
                        .header("api-key", dalleApiKey)
                        .header("Content-Type", "application/json")
                        .bodyValue(dalleRequestDto)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                if (responseBody == null || responseBody.isEmpty()) {
                    throw new RuntimeException("Dalle API로부터 응답이 null이거나 비어 있습니다.");
                }

                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONArray dataArray = jsonResponse.getJSONArray("data");
                String url = dataArray.getJSONObject(0).getString("url");
                log.info("Dalle 생성 이미지 url: {}", url);
                log.info("Dalle revised_prompt: {}", dataArray.getJSONObject(0).getString("revised_prompt"));

                return url;
            } catch (JSONException e) {
                log.error("JSON 파싱 중 오류 발생: {}", e.getMessage());
                throw new RuntimeException("JSON 파싱 오류", e);
            }
        });
    }

    // Dalle 요청 프롬프트 생성
    private String generatePrompt(String imageStyle, List<String> keyPhrases, String inputMessage, String mood, String season) {
        return String.format(
                "Please create an image in a %s style. The image should emphasize a clean and minimalist design and layout. " +
                        "Text and human figures must not be included, and absolutely no letters or characters should appear in the image. " +
                        "Description: %s. " +
                        "Visually express the following keywords: %s. Set the mood to %s and reflect this atmosphere in the image. " +
                        "The background should be based on a %s seasonal theme, kept simple in a solid color. Ensure that the background does not include any text, letters, or characters, and excludes complex background elements. " +
                        "The background should not contain any elements other than the objects described and the keywords.",
                imageStyle,
                chatGptService.translateText(inputMessage, "en"),
                String.join(", ", keyPhrases),
                mood,
                season
        );


//    private String generatePrompt(String imageStyle, List<String> keyPhrases, String inputMessage, String mood, String season) {
//        return String.format(
//                "Create a clean and minimalist image in a %s style. The design should avoid any text, letters, or human figures. " +
//                        "Description: %s. " +
//                        "Focus on visually expressing these keywords: %s. Set the mood to %s to reflect this atmosphere in the image. " +
//                        "Use a simple, solid-colored background inspired by a %s seasonal theme, without including text or complex elements. " +
//                        "Only include the objects and keywords described.",
//                imageStyle,
//                chatGptService.translateText(inputMessage, "en"),
//                String.join(", ", keyPhrases),
//                mood,
//                season
//        );
    }
    public String generateAdvertiseMessage(String inputMessage) {
        log.info("광고 메시지 생성 요청: {}", inputMessage);
        try {
            return chatGptService.generateMessage(inputMessage); // ChatGPT 서비스로 광고 메시지 생성
        } catch (Exception e) {
            log.error("광고 메시지 생성 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("광고 메시지 생성 실패", e);
        }
    }
}
