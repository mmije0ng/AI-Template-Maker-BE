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
        private List<String> imageUrlList;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    // 문자 내역 조회 응답 dto
    public static class HistoryResponseDto {
        private String imageUrl; // 이미지 URL 경로
        private String sendDate; // 발송일자
        private String sendPhoneNumber; // 발신번호
        private String addressName; // 주소록 별칭
    }
}