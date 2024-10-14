package com.backend.sparkle.controller;

import com.backend.sparkle.dto.CommonResponse;
import com.backend.sparkle.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "로그인/회원가입", description = "로그인 및 회원가입에 관한 API")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Operation(summary = "로그인", description = "사용자의 로그인 아이디와 비밀번호를 입력받아 로그인한다.")
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<UserDto.LoginRegisterResponseDto>> loginUser(@RequestBody UserDto.LoginRequestDto requestDto) {
        try {
            // 서비스 메서드에서 실제 로그인 로직을 처리
            log.info("로그인 요청 로그인 ID: {}", requestDto.getLoginId());

            // 로그인 성공 가정
            UserDto.LoginRegisterResponseDto responseDto = UserDto.LoginRegisterResponseDto.builder()
                    .userId(1L) // 실제 유저 ID를 사용해야 함
                    .build();

            return ResponseEntity.ok(CommonResponse.success("로그인 성공", responseDto));
        } catch (Exception e) {
            log.error("로그인 실패: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(CommonResponse.fail("로그인 실패"));
        }
    }

    @Operation(summary = "회원가입", description = "사용자의 로그인 아이디, 비밀번호, 재확인 비밀번호를 입력 받아 회원가입 하기")
    @PostMapping("/register")
    public ResponseEntity<CommonResponse<UserDto.LoginRegisterResponseDto>> registerUser(@RequestBody UserDto.RegisterRequestDto requestDto) {
        try {
            // 서비스 메서드에서 실제 회원가입 로직을 처리
            log.info("회원가입 요청 로그인 ID: {}", requestDto.getLoginId());

            // 회원가입 성공 가정
            UserDto.LoginRegisterResponseDto responseDto = UserDto.LoginRegisterResponseDto.builder()
                    .userId(1L) // 실제 유저 ID를 사용해야 함
                    .build();

            return ResponseEntity.ok(CommonResponse.success("회원가입 성공", responseDto));
        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(CommonResponse.fail("회원가입 실패"));
        }
    }
}
