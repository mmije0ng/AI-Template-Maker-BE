package com.backend.sparkle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DalleRequestDto {

    private String prompt;  // 이미지 생성을 위한 프롬프트
    private String size;    // 이미지 크기
    private int n;          // 생성할 이미지 개수 (한번의 요청에서 한개만 가능)
    private String quality; // 이미지 품질
    private String style;   // 스타일

}
