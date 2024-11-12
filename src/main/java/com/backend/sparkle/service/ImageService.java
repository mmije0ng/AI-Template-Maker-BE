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
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class ImageService {

    private final WebClient webClient;
    private final BlobService blobService;
    private final ChatGPTService chatGptService;

    private final TextAnalyticsService textAnalyticsService;

    @Value("${azure.dalle.endpoint}")
    private String dalleAzureEndpoint;

    @Value("${azure.dalle.api-version}")
    private String dalleApiVersion;

    @Value("${azure.dalle.key}")
    private String dalleApiKey;

    private String dalleURI;

    // 이미지 생성에 사용될 다양한 스타일 목록 정의
    private static final List<String> styles = List.of(
            "Minimalist illustration style with pastel tones, featuring a simple and clean composition with soft shading",
            "Highly detailed and realistic photographic style, a high-resolution image with vivid realism and fine detail",
            "Animation style with bright and vibrant colors, presenting characters and background in a simplified form"
//            "Modern digital art style, an image highlighted by rich colors and creative design elements in a minimalistic layout"
    );

//    private static final List<String> styles = List.of(
//            "파스텔 색감으로 표현된 미니멀리스트 일러스트 스타일, 단순하고 깔끔한 구도와 부드러운 음영 효과가 특징인 이미지",
//            "매우 세밀하고 사실적인 사진 스타일, 현실감 있고 디테일이 살아있는 고해상도 이미지",
//            "밝고 생동감 있는 색감을 사용한 애니메이션 스타일, 캐릭터와 배경이 간결하게 표현된 이미지",
//            "현대적인 디지털 아트 스타일, 풍부한 색감과 창의적인 디자인 요소가 돋보이는 간결하게 표현된 이미지"
//    );

    // WebClient와 BlobService를 생성자 주입을 통해 초기화
    @Autowired
    public ImageService(WebClient.Builder webClientBuilder, BlobService blobService, ChatGPTService chatGptService, TextAnalyticsService textAnalyticsService) {
        this.webClient = webClientBuilder.build();
        this.blobService = blobService;
        this.chatGptService = chatGptService;
        this.textAnalyticsService = textAnalyticsService;
    }

    // 서비스 초기화 시 DALL-E API URI 설정
    @PostConstruct
    public void init() {
        this.dalleURI = String.format("%s?api-version=%s", dalleAzureEndpoint, dalleApiVersion);
    }


    // 이미지 생성 요청 메소드
    public MessageDto.ImageGenerateResponseDto generateImages(MessageDto.ImageGenerateRequestDto requestDto) {
        // 스타일과 계절 전략을 선택하고 변환
        MoodStrategy styleStrategy = MoodStrategyFactory.getMoodStrategy(requestDto.getMood());
        SeasonStrategy seasonStrategy = SeasonStrategyFactory.getSeasonStrategy(requestDto.getSeason());

        // 변환된 스타일과 계절을 사용하여 Dalle 이미지를 생성
        String transformedMood= styleStrategy.applyMood();
        String transformedSeason = seasonStrategy.applySeason();

        // 사용자가 입력한 키워드 + Azure textAnalytics 를 이용하여 추출된 키워드 리스트
        List<String> inputKeyWords = requestDto.getKeyWordMessage();
        List<String> keyPhrases = inputKeyWords.stream()
                .map(keyword -> chatGptService.translateText(keyword, "en"))
                .collect(Collectors.toList());

        keyPhrases.addAll(textAnalyticsService.extractKeyPhrases(requestDto.getInputMessage()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.now(); // 요청 시작 시간 기록

        // 스타일 목록을 비동기 처리하여 이미지 생성 요청
        List<CompletableFuture<String>> futures = styles.parallelStream()
                .map(style -> retryGenerateImage(style, keyPhrases, requestDto.getInputMessage(), transformedMood, transformedSeason))
                .collect(Collectors.toList());

        // 모든 비동기 요청의 결과를 모아 리스트로 반환
        List<String> generatedImageUrls = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        // 이미지 생성 소요 시간 계산
        LocalDateTime endTime = LocalDateTime.now(); // 응답 완료 시간 기록
        log.info("Dalle 이미지 생성 요청 완료 시간: {}", endTime.format(formatter));
        Duration duration = Duration.between(startTime, endTime);
        long seconds = duration.getSeconds();
        log.info("Dalle 이미지 생성 소요 시간: {} 초", seconds);

        return MessageDto.ImageGenerateResponseDto.builder()
                .generatedImageUrls(generatedImageUrls)
                .build();
    }

    // 이미지 생성 요청 재시도
    private CompletableFuture<String> retryGenerateImage(String imageStyle, List<String> keyPhrases, String inputMessage, String mood, String season) {
        int maxRetries = 5;
        int retryCount = 0;
        CompletableFuture<String> result = new CompletableFuture<>();

        while (retryCount < maxRetries) {
            try {
                return generateImageWithDalle(imageStyle, keyPhrases, inputMessage, mood, season);
            } catch (WebClientResponseException e) {
                if (e.getStatusCode().value() == 429 || e.getStatusCode().value() == 400) {
                    retryCount++;
                    log.warn("{}초 후 재시도 ({}/{})", retryCount * 8, retryCount, maxRetries);
                    try {
                        TimeUnit.SECONDS.sleep(retryCount * 8);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    log.error("Dalle API 요청 중 오류 발생: {}", e.getMessage());
                    result.completeExceptionally(e);
                    return result;
                }
            }
        }
        result.completeExceptionally(new RuntimeException("Dalle API 요청이 여러 번 실패했습니다."));
        return result;
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

                // Blob 업로드를 비동기적으로 처리
                return CompletableFuture.supplyAsync(() -> blobService.uploadImageByUrl(url));
            } catch (JSONException e) {
                log.error("JSON 파싱 중 오류 발생: {}", e.getMessage());
                throw new RuntimeException("JSON 파싱 오류", e);
            }
        }).thenCompose(blobUploadFuture -> blobUploadFuture);  // 비동기적으로 Blob 업로드 결과 반환
    }


    // 이미지 생성에 필요한 프롬프트 생성
    private String generatePrompt(String imageStyle, List<String> keyPhrases, String inputMessage,  String mood, String season) {

        return String.format(
                "Please create an image in a %s style. The image should emphasize a clean and minimalist design and layout. " +
                        "Text and human figures must not be included, and absolutely no letters or characters should appear in the image. " +
                        "Description: %s. " +
                        "Visually express the following keywords: %s. Set the mood to %s and reflect this atmosphere in the image. " +
                        "The background should be based on a %s seasonal theme, kept simple in a solid color. Exclude any complex background elements. " +
                        "The background should not contain any elements other than the objects described and the keywords.",
                imageStyle,
                chatGptService.translateText(inputMessage, "en"),
                String.join(", ", keyPhrases),
                mood,
                season
        );

    }

}