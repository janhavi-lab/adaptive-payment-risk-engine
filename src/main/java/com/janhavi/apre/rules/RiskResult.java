package com.janhavi.apre.rules;

import java.util.ArrayList;
import java.util.List;

public class RiskResult {

    private int score;

    private final List<String> reasons = new ArrayList<>();

    public void addScore(int points) {
        score += points;
    }

    public void addReason(String reason) {
        reasons.add(reason);
    }

    public int getScore() {
        return score;
    }

    public List<String> getReasons() {
        return reasons;
    }
}