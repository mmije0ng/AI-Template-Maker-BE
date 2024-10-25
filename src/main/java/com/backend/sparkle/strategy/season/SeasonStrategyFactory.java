package com.backend.sparkle.strategy.season;


import java.util.Map;

// 레지스트리 패턴
public class SeasonStrategyFactory {

    private static final Map<String, SeasonStrategy> MOOD_MAP = Map.of(
            "봄", new SpringSeasonStrategy(),
            "여름", new SummerSeasonStrategy(),
            "가을", new AutumnSeasonStrategy(),
            "겨울", new WinterSeasonStrategy()
    );

    public static SeasonStrategy getSeasonStrategy(String style) {
        SeasonStrategy strategy = MOOD_MAP.get(style);
        if (strategy == null) {
            throw new IllegalArgumentException("지원되지 않는 계절: " + style);
        }
        return strategy;
    }

}
