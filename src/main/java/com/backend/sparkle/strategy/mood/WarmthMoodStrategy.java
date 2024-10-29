package com.backend.sparkle.strategy.mood;

public class WarmthMoodStrategy implements MoodStrategy{
    @Override
    public String applyMood() {
        return "warmth";
    }
}
