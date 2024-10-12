package com.backend.sparkle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class MessageDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    // 이미지 생성 요청 dto
    public static class ImageGenerateRequestDto {
        private String inputMessage; // 입력된 메시지
        private String keyWordMessage; // 입력된 키워드
    }

    @Getter
    @Builder
    @AllArgsConstructor
    // 이미지 생성 응답 dto
    public static class ImageGenerateResponseDto {
        // 생성된 3장의 이미지 URL 경로 리스트
        private List<String> imageURLList;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    // 문자 내역 조회 응답 dto
    public static class HistoryResponseDto {
        private String imageURL; // 이미지 URL 경로
        private String sendDate; // 발송일자
        private String sendPhoneNumber; // 발신번호
        private String addressName; // 주소록 별칭
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    // 템플릿 및 발송 화면 요청 dto
    public static class TemplateRequestDto {
        private String inputMessage; // 입력된 메시지
        private String selectedImageURL; // 선택된 이미지 URL 경로
    }

    @Getter
    @Builder
    @AllArgsConstructor
    // 문자 내역 조회 응답 dto
    public static class TemplateResponseDto {
        private String inputMessage; // 입력된 메시지
        private String selectedImageURL; // 선택된 이미지 URL 경로
        private List<String> sendPhoneNumbers; // 발신번호 목록
        private List<String> addressListNames; // 수신번호 주소록 별칭 목록
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    // 이미지 + 텍스트 문자 전송 요청 dto
    public static class SendRequestDto {
        private String inputMessage; // 입력된 메시지
        private String completeImageURL; // 템플릿 기능을 통해 완성된 이미지 URL 경로
        private String sendPhoneNumber; // 발신번호
        private String addressListName; // 주소록 별칭
        private int sendType; // 0이면 즉시 발송, 1이면 예약 발송
        private String sendDateTime; // 발송 날짜 및 시간
    }

    @Getter
    @Builder
    @AllArgsConstructor
    // 이미지 + 텍스트 문자 전송 응답 dto
    public static class SendResponseDto {
        private Long userId; // 사용자 PK
        private String completeImageURL; // 템플릿 기능을 통해 완성된 이미지 URL 경로 = 발송 이미지
        private String sendDateTime; // 발송 날짜 및 시간
        private String sendPhoneNumber; // 발신번호
        private String addressListName; // 주소록 별칭
    }
}