package com.backend.sparkle.service;

import com.backend.sparkle.dto.DalleRequestDto;
import com.backend.sparkle.dto.MessageDto;
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
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ImageService {

    private final WebClient webClient;
    private final BlobService blobService;

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
            "파스텔 색감으로 표현된 미니멀리스트 일러스트 스타일, 단순하고 깔끔한 구도와 부드러운 음영 효과가 특징인 이미지",
            "매우 세밀하고 사실적인 사진 스타일, 현실감 있고 디테일이 살아있는 고해상도 이미지",
            "밝고 생동감 있는 색감을 사용한 미니멀리스트 애니메이션 스타일, 캐릭터와 배경이 간결하게 표현된 이미지",
            "현대적인 디지털 아트 스타일, 풍부한 색감과 창의적인 디자인 요소가 돋보이는 이미지"
    );

    // WebClient와 BlobService를 생성자 주입을 통해 초기화
    @Autowired
    public ImageService(WebClient.Builder webClientBuilder, BlobService blobService, TextAnalyticsService textAnalyticsService) {
        this.webClient = webClientBuilder.build();
        this.blobService = blobService;
        this.textAnalyticsService=textAnalyticsService;
    }

    // 서비스 초기화 시 DALL-E API URI 설정
    @PostConstruct
    public void init() {
        this.dalleURI = String.format("%s?api-version=%s", dalleAzureEndpoint, dalleApiVersion);
    }

    // 이미지 생성 요청 메소드
    public MessageDto.ImageGenerateResponseDto generateImages(MessageDto.ImageGenerateRequestDto requestDto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.now(); // 요청 시작 시간 기록

        // 스타일 목록을 비동기 처리하여 이미지 생성 요청
        List<CompletableFuture<String>> futures = styles.parallelStream()
                .map(style -> CompletableFuture.supplyAsync(() -> retryGenerateImage(style, requestDto)))
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
                .keyWord(textAnalyticsService.extractKeyPhrases(requestDto.getInputMessage(), requestDto.getKeyWordMessage()))
                .build();
    }

    // 이미지 생성 요청을 재시도
    private String retryGenerateImage(String imageStyle, MessageDto.ImageGenerateRequestDto requestDto) {
        int maxRetries = 5;  // 최대 재시도 횟수
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                return generateImageWithDalle(imageStyle, requestDto); // 이미지 생성 요청 시도
            } catch (WebClientResponseException e) {
                if (e.getStatusCode().value() == 429) {  // Too Many Requests (429 오류) 발생 시 재시도
                    retryCount++;
                    log.warn("429 Too Many Requests 발생. {}초 후 재시도 ({}/{})", retryCount * 8, retryCount, maxRetries);
                    try {
                        TimeUnit.SECONDS.sleep(retryCount * 8); // 재시도 간격을 증가시키며 대기
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    log.error("Dalle API 요청 중 오류 발생: {}", e.getMessage());
                    throw e;
                }
            }
        }
        throw new RuntimeException("Dalle API 요청이 여러 번 실패했습니다.");
    }

    // DALL-E API에 이미지 생성 요청
    private String generateImageWithDalle(String imageStyle, MessageDto.ImageGenerateRequestDto requestDto) {
        String prompt = generatePrompt(imageStyle, requestDto); // 생성된 프롬프트

        // DALL-E API에 전송할 요청 데이터 준비
        DalleRequestDto dalleRequestDto = DalleRequestDto.builder()
                .prompt(prompt)
                .size("1024x1792")
                .n(1)
                .quality("hd")
                .style("vivid")
                .build();

        log.info("Dalle 이미지 생성 요청, prompt: {}", prompt);

        String responseBody;
        try {
            // DALL-E API 요청 및 응답 수신
            responseBody = webClient.post()
                    .uri(dalleURI)
                    .header("api-key", dalleApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(dalleRequestDto)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // 응답이 null이거나 비어 있으면 예외 발생
            if (responseBody == null || responseBody.isEmpty()) {
                throw new RuntimeException("Dalle API로부터 응답이 null이거나 비어 있습니다.");
            }

            // JSON 응답 파싱하여 이미지 URL 추출
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray dataArray = jsonResponse.getJSONArray("data");
            JSONObject dataObject = dataArray.getJSONObject(0);
            String url = dataObject.getString("url");

            // 추출한 URL을 Blob Storage에 업로드하여 새로운 URL 반환
            return blobService.uploadImageByUrl(url);
        } catch (JSONException e) {
            log.error("JSON 파싱 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("JSON 파싱 오류", e);
        } catch (WebClientResponseException e) {
            log.error("Dalle API 요청 중 오류 발생: {}", e.getMessage());
            throw e;
        }
    }

    // 이미지 생성에 필요한 프롬프트 생성
    private String generatePrompt(String imageStyle, MessageDto.ImageGenerateRequestDto requestDto) {
        return String.format("%s를 만들어 주세요. 이 이미지는 깨끗하고 간결한 디자인을 강조하며, 필수적인 요소에 집중하여 표현합니다. " +
                        "텍스트나 문자는 절대로 포함되지 않으며, 어떠한 글자나 글씨도 나타나지 않아야 합니다. 설명: %s. " +
                        "다음 키워드를 반영하여 시각적으로 표현합니다: %s. 분위기는 %s이며, 이 분위기를 반영하여 이미지를 생성합니다. " +
                        "계절적 요소로는 %s을(를) 배경 테마로 설정합니다.",
                imageStyle,
                requestDto.getInputMessage(),
                String.join(", ", requestDto.getKeyWordMessage()),
                requestDto.getMood(),
                requestDto.getSeason());
    }
}