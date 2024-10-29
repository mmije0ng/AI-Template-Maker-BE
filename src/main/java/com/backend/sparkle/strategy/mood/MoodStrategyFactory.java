package com.backend.sparkle.strategy.mood;

import java.util.Map;

// 레지스트리 패턴
public class MoodStrategyFactory{
    private static final Map<String, MoodStrategy> MOOD_MAP = Map.of(
            "차분한 분위기", new CalmMoodStrategy(),
            "활기찬 분위기", new EnergeticMoodStrategy(),
            "따뜻한 느낌", new WarmthMoodStrategy()
    );

    public static MoodStrategy getMoodStrategy(String mood) {
        MoodStrategy strategy = MOOD_MAP.get(mood);
        if (strategy == null) {
            throw new IllegalArgumentException("지원되지 않는 스타일: " + mood);
        }
        return strategy;
    }
}
