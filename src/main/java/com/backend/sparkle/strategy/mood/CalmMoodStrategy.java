package com.backend.sparkle.strategy.mood;

public class CalmMoodStrategy implements MoodStrategy{
    @Override
    public String applyMood() {
        return "calm";
    }
}
