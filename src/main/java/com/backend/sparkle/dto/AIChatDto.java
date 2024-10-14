package com.backend.sparkle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "AI 챗봇 요청 및 응답 정보")
public class AIChatDto {

    @Schema(description = "챗봇과 주고 받는 메시지", example = "<예시>\n"+
            "사용자가 보낸 메시지일 경우: 50% 할인 행사 홍보를 주제로 홍보 문구 만들어줘.\n" +
            "챗봇이 보낸 메시지일 경우: 특별한 기회를 놓치지 마세요!\n" +
            "지금 바로 50% 할인 행사에 참여하세요!\n" +
            "한정된 시간 동안 진행되는 이번 행사에서는 인기 상품을 절반 가격으로 만나볼 수 있습니다."
    )
    private String chatMessage;

    @Schema(description = "내가 보낸 메시지인지 여부, 내가 보낸 메시지 이면 true, 챗봇이 보낸 메시지이면 false", example = "true")
    @JsonProperty("isMine")
    private boolean isMine;
}