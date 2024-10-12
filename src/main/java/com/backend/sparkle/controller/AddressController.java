package com.backend.sparkle.controller;

import com.backend.sparkle.dto.AddressDto;
import com.backend.sparkle.dto.CommonResponse;
import com.backend.sparkle.dto.MessageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Tag(name = "주소록 관리 페이지", description = "주소록 업로드 및 조회에 관한 API")
@RestController
@RequestMapping("/api/address")
public class AddressController {

    // 주소록 파일 업로드
    @Operation(summary = "주소록 파일 업로드", description = "주소록 파일과 별칭을 입력 받아 업로드한다.")
    @PostMapping("/upload/{userId}")
    public CommonResponse<?> createAddressListToAddressByFile(@PathVariable(name = "userId") Long userId, @RequestBody AddressDto.UploadRequestDto requestDto) {
        try {
            log.info("주소록 파일 업로드 userId: {}", userId);

            return CommonResponse.success("주소록 파일 업로드 성공", userId);
        } catch (Exception e) {
            log.error("주소록 파일 업로드 실패: {}", e.getMessage());
            return CommonResponse.fail(HttpStatus.NOT_FOUND, "주소록 파일 업로드 실패");
        }
    }

    // 주소록 직접 업로드
    // 수정 필요


    // 주소록 목록 불러오기
    @Operation(summary = "나의 주소록 목록", description = "페이지 번호에 맞는 나의 주소록 목록 조회")
    @GetMapping("history")
    public CommonResponse<?> getAddressListToAddressHistory(@RequestParam(name = "userId") Long userId, @RequestParam(name = "pageNumber") Integer pageNumber) {
        try {
            log.info("주소록 목록 조회 userId: {}, page 번호: {}", userId, pageNumber);

            // 페이지 설정, 최신순으로 13개씩 가져오기
//            Pageable pageable = PageRequest.of(pageNumber, 13, Sort.by("createdAt").descending());
            Pageable pageable = PageRequest.of(pageNumber, 13);

            // 실제 서비스에서는 DB에서 페이징 처리된 문자 내역을 조회해야 함
            // 여기서는 예시로 임의의 데이터 생성
            List<AddressDto.HistoryResponseDto> addressHistoryList = new ArrayList<>();
            addressHistoryList.add(new AddressDto.HistoryResponseDto(1, "한성대 주소록", "2024-10-13"));
            addressHistoryList.add(new AddressDto.HistoryResponseDto(2, "수학 학원", "2024-10-12"));
            addressHistoryList.add(new AddressDto.HistoryResponseDto(3, "영어 학원", "2024-10-11"));
            addressHistoryList.add(new AddressDto.HistoryResponseDto(4, "성북구 주소록", "2024-10-11"));

            // 페이징에 맞는 데이터 서브리스트 추출 (나중에 jpa로 pageSize에 맞는 데이터만 추출할 수 있음)
            int start = Math.min((int) pageable.getOffset(), addressHistoryList.size());
            int end = Math.min((start + pageable.getPageSize()), addressHistoryList.size());
            List<AddressDto.HistoryResponseDto> subList = addressHistoryList.subList(start, end);

            // 페이징 처리된 리스트로 변환
            Page<AddressDto.HistoryResponseDto> responseDto = new PageImpl<>(subList, pageable, addressHistoryList.size());

            return CommonResponse.success("주소록 목록 조회 성공", responseDto);
        } catch (Exception e) {
            log.error("주소록 목록 조회 실패: {}", e.getMessage());
            return CommonResponse.fail(HttpStatus.NOT_FOUND, "주소록 목록 조회 실패");
        }
    }
}
