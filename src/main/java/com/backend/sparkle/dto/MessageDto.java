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

        @Schema(description = "발송 목적 및 내용", example = "립스틱과 향수 병을 포함한 우아한 뷰티 제품 배열 사진을 만들고 싶어요. 부드러운 꽃들로 둘러싸여 여성스럽고 고급스러운 분위기를 주기 위해 파스텔 색상을 사용합니다.")
        private String inputMessage;

        @Schema(description = "분위기 키워드", example = "차분한 분위기")
        private String mood;

        @Schema(description = "계절감 키워드", example = "봄")
        private String season;

        @Schema(description = "사용자가 직접 입력한 키워드", example = "[\"립스틱\", \"향수병\", \"여성스러운 분위기\"]")
        private List<String> keyWordMessage;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "이미지 생성 응답 정보")
    public static class ImageGenerateResponseDto {
        @Schema(description = "생성된 4장의 이미지 URL 경로 리스트",
                example = "[\"https://sparcleblob.blob.core.windows.net/test-blob/uploaded_image_1730195858730.png\", " +
                        "\"https://sparcleblob.blob.core.windows.net/test-blob/uploaded_image_1730195859420.png\", " +
                        "\"https://sparcleblob.blob.core.windows.net/test-blob/uploaded_image_1730195859651.png\", " +
                        "\"https://sparcleblob.blob.core.windows.net/test-blob/uploaded_image_1730195858740.png\"]")
        private List<String> generatedImageUrls;
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
                example = "https://sparcleblob.blob.core.windows.net/test-blob/uploaded_image_1730195859420.png")
        private String completeImageURL;

        @Schema(description = "발신번호", example = "01012345678")
        private String sendPhoneNumber;

        @Schema(description = "단일 수신번호, 엑셀 파일 목록에 있는 수신번호가 아닌 사용자가 별도로 입력한 수신번호", example = "01056781234")
        private String testSendPhoneNumber;

        @Schema(description = "발송 타입", example = "0이면 즉시 발송, 1이면 예약 발송")
        private int sendType;

        @Schema(description = "발송일자, 현재 날짜로 입력", example = "2024-10-13")
        private String sendDateTime;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "문자 보내기 완료 후 응답 정보")
    public static class SendCompleteResponseDto {
        @Schema(description = "이미지 URL 경로",
                example = "https://sparcleblob.blob.core.windows.net/test-blob/uploaded_image_1730195859420.png")
        private String imageURL;

        @Schema(description = "발송 메시지", example =
                """
                한성대 피자짱 가게에서 방문 포장 시 50% 할인을 진행합니다.
                위 광고 문자를 결제 시 직원분에게 보여주시면 됩니다.
                일부 품목에 한해서 할인이 제한될 수 있습니다.
                감사합니다.""")
        private String sendMessage;

        @Schema(description = "발송일자", example = "2024-10-13 14:06")
        private String sendDateTime;

        @Schema(description = "발신번호", example = "01012345678")
        private String sendPhoneNumber;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "문자 내역 조회 응답 정보")
    public static class HistoryResponseDto {
        @Schema(description = "이미지 URL 경로",
                example = "https://sparcleblob.blob.core.windows.net/test-blob/uploaded_image_1730195859420.png")
        private String imageURL;

        @Schema(description = "발송일자", example = "2024-10-13 14:06")
        private String sendDateTime;

        @Schema(description = "발신번호", example = "010-1234-5678")
        private String sendPhoneNumber;
    }
}