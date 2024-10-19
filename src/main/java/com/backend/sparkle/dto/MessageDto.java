package com.backend.sparkle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

public class MessageDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "이미지 생성 요청 정보")
    public static class ImageGenerateRequestDto {

        @Schema(description = "발송 목적 및 내용", example = "피자 광고에 관한 이미지를 만들고 싶다.")
        private String inputMessage;

        @Schema(description = "분위기 키워드", example = "차분한 분위기")
        private String style;

        @Schema(description = "계절감 키워드", example = "봄")
        private String season;

        @Schema(description = "사용자가 직접 입력된 키워드", example = "피자, 광고, 할인")
        private String keyWordMessage;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "이미지 생성 응답 정보")
    public static class ImageGenerateResponseDto {
        @Schema(description = "생성된 3장의 이미지 URL 경로 리스트",
                example = "[\"https://i.pinimg.com/564x/48/3d/a7/483da78ca17fa011004bac70b7e7c763.jpg\", " +
                        "\"https://i.pinimg.com/564x/38/73/51/387351a404a2dcf47ada6a138b7a14e7.jpg\", " +
                        "\"https://i.pinimg.com/564x/f0/e0/9c/f0e09cba73d689fc2c0ef01bbbbeae1a.jpg\"]")
        private List<String> generatedImageUrls;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "문자 내역 조회 응답 정보")
    public static class HistoryResponseDto {
        @Schema(description = "이미지 URL 경로",
                example = "https://i.pinimg.com/564x/f0/e0/9c/f0e09cba73d689fc2c0ef01bbbbeae1a.jpg")
        private String imageURL;

        @Schema(description = "발송일자", example = "2024-10-13")
        private String sendDateTime;

        @Schema(description = "발신번호", example = "010-1234-5678")
        private String sendPhoneNumber;

        @Schema(description = "주소록 별칭", example = "김선생 영어학원 주소록")
        private String addressName;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "템플릿 및 발송 화면 응답 정보")
    public static class TemplateResponseDto {

        @Schema(description = "선택된 이미지 URL 경로",
                example = "https://i.pinimg.com/564x/f0/e0/9c/f0e09cba73d689fc2c0ef01bbbbeae1a.jpg")
        private String selectedImageURL;


        @Schema(description = "발신번호 목록", example = "[\"010-0000-0000\", \"010-1234-5678\", \"010-5678-1234\"]")
        private List<String> sendPhoneNumbers;

        @Schema(description = "수신번호 주소록 별칭 목록", example = "[\"한성대 주소록\", \"김선생 수학 학원 주소록\"]")
        private List<String> addressNames;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "이미지 + 텍스트 문자 전송 요청 정보")
    public static class SendRequestDto {

        @Schema(description = "발송 메시지", example =
                """
                한성대 피자짱 가게에서 방문 포장 시 50% 할인을 진행합니다.
                위 광고 문자를 결제 시 직원분에게 보여주시면 됩니다.
                일부 품목에 한해서 할인이 제한될 수 있습니다.
                감사합니다.""")
        private String sendMessage;

        @Schema(description = "템플릿 기능을 통해 완성된 이미지 URL 경로",
                example = "https://i.pinimg.com/564x/f0/e0/9c/f0e09cba73d689fc2c0ef01bbbbeae1a.jpg")
        private String completeImageURL;

        @Schema(description = "발신번호", example = "010-1234-5678")
        private String sendPhoneNumber;

        @Schema(description = "주소록 별칭", example = "김선생 영어학원 주소록")
        private String addressName;

        @Schema(description = "발송 타입", example = "0이면 즉시 발송, 1이면 예약 발송")
        private int sendType;

        @Schema(description = "발송일자", example = "2024-10-13")
        private String sendDateTime;
    }

//    @Getter
//    @Builder
//    @AllArgsConstructor
//    @Schema(description = "이미지 + 텍스트 문자 전송 응답 정보")
//    public static class SendResponseDto {
//        @Schema(description = "사용자 PK", example = "1")
//        private Long userId;
//
//        @Schema(description = "입력된 메시지", example =
//                """
//                한성대 피자짱 가게에서 방문 포장 시 50% 할인을 진행합니다.
//                위 광고 문자를 결제 시 직원분에게 보여주시면 됩니다.
//                일부 품목에 한해서 할인이 제한될 수 있습니다.
//                감사합니다.""")
//        private String sendMessage; // 입력된 메시지
//
//        @Schema(description = "템플릿 기능을 통해 완성된 이미지 URL 경로, 즉 발송 이미지 URL 경로",
//                example = "https://i.pinimg.com/564x/f0/e0/9c/f0e09cba73d689fc2c0ef01bbbbeae1a.jpg")
//        private String completeImageURL;
//
//        @Schema(description = "발송일자", example = "2024-10-13")
//        private String sendDateTime; // 발송 날짜 및 시간
//
//        @Schema(description = "발신번호", example = "010-1234-5678")
//        private String sendPhoneNumber;
//
//        @Schema(description = "주소록 별칭", example = "김선생 영어학원 주소록")
//        private String addressName;
//    }
}