package com.backend.sparkle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
public class UserDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginRequestDto {
        private String loginId; // 로그인 아이디
        private String loginPassword; // 로그인 비밀번호
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RegisterRequestDto {
        private String loginId;
        private String loginPassword;
        private String checkPassword; // 확인 비밀번호
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LoginRegisterResponseDto {
        private Long userId; // 사용자 PK
    }
}