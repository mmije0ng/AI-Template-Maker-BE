package com.backend.sparkle.strategy.mood;

public class EnergeticMoodStrategy implements MoodStrategy {
    @Override
    public String applyMood() {
        return "energetic";
    }
}
