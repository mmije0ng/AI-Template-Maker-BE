package com.backend.sparkle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class ChatGptDto {

    @Getter
    @AllArgsConstructor
    public static class ChatRequestDto {
        private String model;

        @JsonProperty("max_tokens")
        private int maxTokens;

        private double temperature;

        private List<Message> messages;

        @Getter
        @AllArgsConstructor
        public static class Message {
            private String role;
            private String content;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatResponseDto {
        private List<Choice> choices;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Choice {
            private Message message;

            @Getter
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Message  {
                private String content;
            }
        }
    }
}
