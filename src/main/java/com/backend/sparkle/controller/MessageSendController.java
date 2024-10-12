package com.backend.sparkle.controller;

import com.backend.sparkle.dto.CommonResponse;
import com.backend.sparkle.dto.MessageDto;
import com.backend.sparkle.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Tag(name = "문자 보내기 페이지", description = "문자 보내기 및 이미지 생성에 관한 API")
@RestController
@RequestMapping("/api/message")
public class MessageSendController {

    // 메시지 및 키워드 입력 후 이미지 생성
    @Operation(summary = "메시지 및 키워드 입력 후 이미지 생성",
            description = "사용자가 메시지 및 키워드를 입력 후 이미지 생성하기 버튼을 클릭하여 3개의 이미지 생성")
    @PostMapping("/generate/{userId}")
    public CommonResponse<?> createImages(@PathVariable Long userId, @RequestBody MessageDto.ImageGenerateRequestDto requestDto) {
        try {
            log.info("이미지 생성 요청 userId: {}", userId);

            // 서비스 메서드에서 이미지 생성 로직을 처리해야 함
            // ArrayList 인스턴스 생성 및 데이터 추가
            List<String> imageUrlList = new ArrayList<>();
            imageUrlList.add("https://i.pinimg.com/564x/48/3d/a7/483da78ca17fa011004bac70b7e7c763.jpg");
            imageUrlList.add("https://i.pinimg.com/564x/38/73/51/387351a404a2dcf47ada6a138b7a14e7.jpg");
            imageUrlList.add("https://i.pinimg.com/564x/f0/e0/9c/f0e09cba73d689fc2c0ef01bbbbeae1a.jpg");

            // 이미지 생성 응답 객체 생성
            MessageDto.ImageGenerateResponseDto responseDto = MessageDto.ImageGenerateResponseDto.builder()
                    .imageURLList(imageUrlList)
                    .build();

            return CommonResponse.success("이미지 생성 성공", responseDto);
        } catch (Exception e) { // 예외 처리 필요
            log.error("이미지 생성 실패: {}", e.getMessage());
            return CommonResponse.fail(HttpStatus.NOT_FOUND, "이미지 생성 실패");
        }
    }

    // 템플릿 기능을 통해 완성된 이미지 + 텍스트를 뿌리오 API와 연동하여 문자 전송
    @Operation(summary = "템플릿 및 발송화면",
            description = "생성된 이미지 3장 중 사용자가 1장 선택한 후 템플릿 및 발송화면으로 전환")
    @GetMapping("/template/{userId}")
    public CommonResponse<?> getTemplateSendPage(@PathVariable Long userId, @RequestBody MessageDto.TemplateRequestDto requestDto) {
        try {
            log.info("템플릿 및 발송화면 요청 userId: {}", userId);

            List<String> sendPhoneNumbers = new ArrayList<>(); // 발신자 목록
            sendPhoneNumbers.add("010-0000-0000");
            sendPhoneNumbers.add("010-1234-5678");
            sendPhoneNumbers.add("010-5678-1234");

            List<String> addressListNames = new ArrayList<>(); // 주소록 별칭 목록
            addressListNames.add("한성대 주소록");
            addressListNames.add("김선생 수학 학원 주소록");

            MessageDto.TemplateResponseDto responseDto = MessageDto.TemplateResponseDto.builder()
                    .inputMessage(requestDto.getInputMessage())
                    .selectedImageURL(requestDto.getSelectedImageURL())
                    .sendPhoneNumbers(sendPhoneNumbers)
                    .addressListNames(addressListNames)
                    .build();

            return CommonResponse.success("템플릿 및 발송화면 요청 성공", responseDto);
        } catch (Exception e) { // 예외 처리 필요
            log.error("템플릿 및 발송화면 요청 실패: {}", e.getMessage());
            return CommonResponse.fail(HttpStatus.NOT_FOUND, "템플릿 및 발송화면 요청 실패");
        }
    }

    // 이미지 + 텍스트 문자 발송하기
    // 프론트 => 백으로 이미지 + 텍스트 넘겨주면 백에서 뿌리오 API와 연동하여 메시지를 보내야 함 (별도의 로직 필요)
    // 뿌리오 API와 연동하여 메시지를 보낸 후 프론트로 응답 전송
    @Operation(summary = "이미지 + 텍스트 문자 발송", description = "템플릿 기능을 통해 완성된 이미지 + 텍스트 문자 전송 요청")
    @PostMapping("/send/{userId}")
    public CommonResponse<?> sendMessage(@PathVariable Long userId, @RequestBody MessageDto.SendRequestDto requestDto) {
        try {
            log.info("이미지 + 텍스트 문자 발송 요청 userId: {}", userId);

            MessageDto.SendResponseDto responseDto = MessageDto.SendResponseDto.builder()
                    .userId(userId)
                    .completeImageURL("https://i.pinimg.com/564x/f0/e0/9c/f0e09cba73d689fc2c0ef01bbbbeae1a.jpg")
                    .sendDateTime("2024-10-13")
                    .sendPhoneNumber("010-0000-0000")
                    .addressListName("한성대 주소록")
                    .build();

            return CommonResponse.success("이미지 + 텍스트 문자 발송 요청 성공", responseDto);
        } catch (Exception e) { // 예외 처리 필요
            log.error("이미지 + 텍스트 문자 발송 요청 실패: {}", e.getMessage());
            return CommonResponse.fail(HttpStatus.NOT_FOUND, "이미지 + 텍스트 문자 발송 요청 실패");
        }
    }

    // 문자 테스트 발송
    // 수정 필요
    @Operation(summary = "이미지 + 텍스트 문자 테스트 발송", description = "템플릿 기능을 통해 완성된 이미지 + 텍스트 문자 테스트 발송 요청")
    @PostMapping("/test/{userId}")
    public CommonResponse<?> sendTestMessage(@PathVariable Long userId /*, @RequestBody MessageDto.SendRequestDto requestDto*/) {
        try {
//            log.info("이미지 + 텍스트 문자 발송 요청 userId: {}", userId);
//
//            MessageDto.SendResponseDto responseDto = MessageDto.SendResponseDto.builder()
//                    .userId(userId)
//                    .completeImageURL("https://i.pinimg.com/564x/f0/e0/9c/f0e09cba73d689fc2c0ef01bbbbeae1a.jpg")
//                    .sendDateTime("2024-10-13")
//                    .sendPhoneNumber("010-0000-0000")
//                    .addressListName("한성대 주소록")
//                    .build();

            return CommonResponse.success("이미지 + 텍스트 문자 테스트 발송 요청 성공", userId);
        } catch (Exception e) { // 예외 처리 필요
            log.error("이미지 + 텍스트 문자 테스트 발송 요청 실패: {}", e.getMessage());
            return CommonResponse.fail(HttpStatus.NOT_FOUND, "이미지 + 텍스트 문자 테스트 발송 요청 실패");
        }
    }
}
