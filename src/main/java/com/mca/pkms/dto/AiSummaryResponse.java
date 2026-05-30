package com.mca.pkms.dto;

public record AiSummaryResponse(boolean configured, String summary, String message) {
    public static AiSummaryResponse unavailable() {
        return new AiSummaryResponse(false, "",
                "AI is not configured yet. Add GEMINI_API_KEY or OPENAI_API_KEY in the deployment environment to enable summaries.");
    }

    public static AiSummaryResponse success(String summary) {
        return new AiSummaryResponse(true, summary, "Summary generated.");
    }

    public static AiSummaryResponse failure(String message) {
        return new AiSummaryResponse(true, "", message);
    }
}
