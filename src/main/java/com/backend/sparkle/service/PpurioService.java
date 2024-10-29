package com.backend.sparkle.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
            // URL에서 이미지 파일을 다운로드하여 Base64로 인코딩
            messageParams.put("files", List.of(downloadAndEncodeImage(imageUrl)));
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
    private HttpURLConnection createConnection(Request request) throws IOException {
        URL url = new URL(request.getRequestUri());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", request.getAuthorization());
        conn.setConnectTimeout(TIME_OUT);
        conn.setReadTimeout(TIME_OUT);
        return conn;
    }
    private Map<String, Object> createFileParams(String filePath) throws IOException {
        File file = new File(filePath);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] fileBytes = new byte[(int) file.length()];
            int readBytes = fileInputStream.read(fileBytes);

            if (readBytes != file.length()) {
                throw new IOException("파일 크기와 읽은 바이트 수가 일치하지 않습니다.");
            }

            String encodedFileData = Base64.getEncoder().encodeToString(fileBytes);

            HashMap<String, Object> fileParams = new HashMap<>();
            fileParams.put("size", file.length());
            fileParams.put("name", file.getName());
            fileParams.put("data", encodedFileData);
            return fileParams;
        }
    }
    // URL에서 이미지를 다운로드하고 Base64로 인코딩하여 파일 객체를 생성
    private Map<String, Object> downloadAndEncodeImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        try (InputStream in = url.openStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            byte[] imageBytes = out.toByteArray();
            String encodedFileData = Base64.getEncoder().encodeToString(imageBytes);

            HashMap<String, Object> fileParams = new HashMap<>();
            fileParams.put("size", imageBytes.length);
            fileParams.put("name", "image.jpg");
            fileParams.put("data", encodedFileData);
            return fileParams;
        }
    }
}

class Request {
    private final String requestUri;
    private final String authorization;

    public Request(String requestUri, String authorization) {
        this.requestUri = requestUri;
        this.authorization = authorization;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public String getAuthorization() {
        return authorization;
    }
}
