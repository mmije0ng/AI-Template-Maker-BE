package com.backend.sparkle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "로그인 요청 dto")
    public static class LoginRequestDto {
        @Schema(description = "사용자 로그인 아이디", example = "user123")
        private String loginId;

        @Schema(description = "로그인 비밀번호", example = "password123")
        private String loginPassword;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "회원가입 요청 dto")
    public static class RegisterRequestDto {
        @Schema(description = "사용자 로그인 아이디", example = "user123")
        private String loginId;

        @Schema(description = "로그인 비밀번호", example = "password123")
        private String loginPassword;

        @Schema(description = "확인 비밀번호", example = "password123")
        private String checkPassword;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "로그인 및 회원가입 응답 dto")
    public static class LoginRegisterResponseDto {
        @Schema(description = "사용자 PK", example = "1")
        private Long userId;
    }
}
