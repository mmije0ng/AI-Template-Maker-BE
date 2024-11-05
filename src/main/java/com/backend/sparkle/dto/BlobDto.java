package com.backend.sparkle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;


@Getter
@Setter
@Builder
public class BlobDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RequetDto {
        private String base64Image;

    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResponseDto {
        private String imageUrl;

    }

}
