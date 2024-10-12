package com.backend.sparkle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@AllArgsConstructor
public class AddressDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    // 주소록 업로드 요청 dto
    public static class UploadRequestDto {
        private MultipartFile addressFile; // 주소록 파일
        private String addressListName; // 주소록 별칭 이름
    }

    @Getter
    @Builder
    @AllArgsConstructor
    // 나의 주소록 목록 응답 dto
    public static class HistoryResponseDto {
        private Integer number; // 번호
        private String addressListName; // 주소록 별칭 이름
        private String sendDateTime; // 발송 날짜 및 시간
    }

}
