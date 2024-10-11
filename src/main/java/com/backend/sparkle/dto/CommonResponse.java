package com.backend.sparkle.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class CommonResponse<T> {
    private final int status;
    private final String message;
    private final T data;

    // 성공 시 dto 반환
    public static <T> CommonResponse<T> success(String message, T data){
        return new CommonResponse<>(HttpStatus.OK.value(), message, data);
    }

    // 실패 시 dto 반환
    // 파라미터: http 상태코드, 메시지
    public static <T> CommonResponse<T> fail(HttpStatus httpStatus, String message){
        return new CommonResponse<>(httpStatus.value(), message, null);
    }
}
