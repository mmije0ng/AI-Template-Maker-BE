package com.backend.sparkle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

public class AddressDto  {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "주소록 업로드 요청 정보")
    public static class UploadRequestDto {
        @Schema(description = "주소록 파일", example = "")
        private MultipartFile addressFile;

        @Schema(description = "주소록 별칭 이름", example = "한성대 주소록")
        private String addressListName;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "주소록 목록 응답 정보")
    public static class HistoryResponseDto {
        @Schema(description = "목록 번호", example = "1")
        private Integer number;

        @Schema(description = "주소록 별칭 이름", example = "한성대 주소록")
        private String addressListName;

        @Schema(description = "발송 날짜 및 시간", example = "2024-10-14")
        private String sendDateTime;
    }

}
