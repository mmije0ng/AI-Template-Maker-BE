package com.backend.sparkle.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PpurioService {

    private static final Integer TIME_OUT = 5000;

    @Value("${spring.ppurio.Apikey}")
    private String PpurioApiKey;

    @Value("${spring.ppurio.ACCOUNT}")
    private String PpurioACCOUNT;

    @Value("${spring.ppurio.PpurioURI}")
    private String PpurioURI;

    public boolean sendSmsWithImage(String sendPhoneNumber, List<String> recipientNumbers, String sendMessage, String imageUrl, int sendType, String sendDateTime) {
        String basicAuthorization = Base64.getEncoder().encodeToString((PpurioACCOUNT + ":" + PpurioApiKey).getBytes());
        Map<String, Object> tokenResponse = getToken(PpurioURI, basicAuthorization); // 토큰 발급

        if (tokenResponse == null || !tokenResponse.containsKey("token")) {
            log.error("토큰 발급 실패: {}", tokenResponse);
            return false;
        }

        String token = (String) tokenResponse.get("token");
        boolean allSuccess = true;

        for (String recipient : recipientNumbers) {
            log.info("수신 번호 {}로 메시지 전송 시도", recipient);
            try {
                Map<String, Object> sendResponse = send(PpurioURI, token, sendPhoneNumber, recipient, sendMessage, imageUrl, sendType, sendDateTime);
                if (sendResponse == null || !sendResponse.containsKey("success") || !(boolean) sendResponse.get("success")) {
                    allSuccess = false;
                    log.error("수신자 {}에게 메시지 전송 실패: {}", recipient, sendResponse);
                } else {
                    log.info("수신자 {}에게 메시지 전송 성공", recipient);
                }
            } catch (Exception e) {
                log.error("수신자 {}에게 메시지 전송 중 예외 발생: {}", recipient, e.getMessage());
                allSuccess = false;
            }
        }
        return allSuccess;
    }

    private Map<String, Object> getToken(String baseUri, String basicAuthorization) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(baseUri + "/v1/token");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Basic " + basicAuthorization);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(TIME_OUT);
            conn.setReadTimeout(TIME_OUT);

            return getResponseBody(conn);
        } catch (IOException e) {
            log.error("토큰 요청 중 IOException 발생: {}", e.getMessage());
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private Map<String, Object> send(String baseUri, String accessToken, String sendPhoneNumber, String recipientPhoneNumber, String sendMessage, String imageUrl, int sendType, String sendDateTime) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(baseUri + "/v1/message");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(TIME_OUT);
            conn.setReadTimeout(TIME_OUT);
            conn.setDoOutput(true);

            Map<String, Object> messageParams = new HashMap<>();
            messageParams.put("account", PpurioACCOUNT);
            messageParams.put("from", sendPhoneNumber);
            messageParams.put("content", sendMessage);
            messageParams.put("duplicateFlag", "Y");
            messageParams.put("targetCount", 1);
            messageParams.put("messageType", "MMS");

            // 리사이즈된 이미지를 인코딩하여 전송
            messageParams.put("files", List.of(downloadResizeAndEncodeImage(imageUrl)));
            messageParams.put("refKey", RandomStringUtils.random(32, true, true));
            Map<String, Object> target = new HashMap<>();
            target.put("to", recipientPhoneNumber);
            target.put("name", "tester");  // 수신자 이름 설정
            target.put("changeWord", Map.of("var1", "Ppurio API Test"));

            messageParams.put("targets", List.of(target));

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = new ObjectMapper().writeValueAsString(messageParams).getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            return getResponseBody(conn);
        } catch (IOException e) {
            log.error("문자 발송 요청 중 IOException 발생: {}", e.getMessage());
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private Map<String, Object> getResponseBody(HttpURLConnection conn) throws IOException {
        InputStream inputStream = (conn.getResponseCode() == 200) ? conn.getInputStream() : conn.getErrorStream();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder responseBody = new StringBuilder();
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                responseBody.append(inputLine);
            }
            return new ObjectMapper().readValue(responseBody.toString(), new TypeReference<>() {});
        } catch (IOException e) {
            log.error("응답 처리 중 IOException 발생: {}", e.getMessage());
            throw e;
        }
    }

    private Map<String, Object> downloadResizeAndEncodeImage(String imageUrl) throws IOException {
        // 이미지 다운로드
        URL url = new URL(imageUrl);
        BufferedImage originalImage = ImageIO.read(url);

        // 이미지 리사이즈
        BufferedImage resizedImage = new BufferedImage(640, 960, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, 640, 960, null);
        g.dispose();

        // 리사이즈된 이미지를 Base64로 인코딩
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        String encodedFileData = Base64.getEncoder().encodeToString(imageBytes);

        // Ppurio API에서 요구하는 파일 파라미터
        Map<String, Object> fileParams = new HashMap<>();
        fileParams.put("size", imageBytes.length);
        fileParams.put("name", "image.jpg");
        fileParams.put("data", encodedFileData);
        return fileParams;
    }
}
