package com.backend.sparkle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
@Schema(description = "응답 정보")
public class CommonResponse<T> {
    @Schema(description = "요청 성공 여부, true이면 요청 성공, false이면 요청 실패", example = "")
    private final boolean isSuccess;

    @Schema(description = "응답 메시지", example = "")
    private final String message;

    @Schema(description = "응답 데이터", example = "")
    private final T data;

    // 성공 시 dto 반환
    public static <T> CommonResponse<T> success(String message, T data){
        return new CommonResponse<>(true, message, data);
    }

    // 실패 시 dto 반환
    // 파라미터: http 상태코드, 메시지
    public static <T> CommonResponse<T> fail(String message){
        return new CommonResponse<>(false, message, null);
    }
}
