package com.mca.pkms.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mca.pkms.dto.AiSummaryResponse;
import com.mca.pkms.entity.Note;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AiService {
    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final String provider;
    private final String openAiApiKey;
    private final String openAiModel;
    private final String geminiApiKey;
    private final String geminiModel;
    private final RestClient openAiRestClient;
    private final RestClient geminiRestClient;
    private final ObjectMapper objectMapper;

    public AiService(@Value("${app.ai.provider:auto}") String provider,
                     @Value("${app.ai.openai.api-key:}") String openAiApiKey,
                     @Value("${app.ai.openai.model:gpt-5.2}") String openAiModel,
                     @Value("${app.ai.gemini.api-key:}") String geminiApiKey,
                     @Value("${app.ai.gemini.model:gemini-2.5-flash}") String geminiModel,
                     ObjectMapper objectMapper) {
        this.provider = provider;
        this.openAiApiKey = openAiApiKey;
        this.openAiModel = openAiModel;
        this.geminiApiKey = geminiApiKey;
        this.geminiModel = geminiModel;
        this.objectMapper = objectMapper;
        this.openAiRestClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .build();
        this.geminiRestClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    public AiSummaryResponse summarize(Note note) {
        return generate(note, summaryInstructions(), 350, "AI summary");
    }

    public AiSummaryResponse studyQuestions(Note note) {
        return generate(note, studyQuestionInstructions(), 500, "AI study questions");
    }

    private AiSummaryResponse generate(Note note, String instructions, int maxOutputTokens, String featureName) {
        String selectedProvider = selectedProvider();
        if (selectedProvider.isBlank()) {
            return AiSummaryResponse.unavailable();
        }
        try {
            String output = "gemini".equals(selectedProvider)
                    ? generateWithGemini(note, instructions, maxOutputTokens)
                    : generateWithOpenAi(note, instructions, maxOutputTokens);
            if (output.isBlank()) {
                return AiSummaryResponse.failure("AI responded, but no text was returned. Please try again.");
            }
            return AiSummaryResponse.success(output);
        } catch (RestClientResponseException ex) {
            log.warn("{} generation failed with {} status {} for note id {}: {}",
                    featureName, selectedProvider, ex.getStatusCode(), note.getId(), ex.getResponseBodyAsString(), ex);
            return AiSummaryResponse.failure(friendlyApiError(selectedProvider, ex));
        } catch (RestClientException | IOException ex) {
            log.warn("{} generation failed with {} for note id {}", featureName, selectedProvider, note.getId(), ex);
            return AiSummaryResponse.failure(featureName + " could not be generated right now. Please try again later.");
        }
    }

    private String selectedProvider() {
        String normalized = provider == null ? "auto" : provider.trim().toLowerCase();
        if ("gemini".equals(normalized)) {
            return hasText(geminiApiKey) ? "gemini" : "";
        }
        if ("openai".equals(normalized)) {
            return hasText(openAiApiKey) ? "openai" : "";
        }
        if (hasText(geminiApiKey)) {
            return "gemini";
        }
        return hasText(openAiApiKey) ? "openai" : "";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String generateWithOpenAi(Note note, String instructions, int maxOutputTokens) throws IOException {
        String response = openAiRestClient.post()
                .uri("/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(openAiApiKey))
                .body(openAiRequest(note, instructions, maxOutputTokens))
                .retrieve()
                .body(String.class);
        return extractOpenAiOutputText(response);
    }

    private String generateWithGemini(Note note, String instructions, int maxOutputTokens) throws IOException {
        String response = geminiRestClient.post()
                .uri("/v1beta/models/{model}:generateContent", geminiModel)
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-goog-api-key", geminiApiKey)
                .body(geminiRequest(note, instructions, maxOutputTokens))
                .retrieve()
                .body(String.class);
        return extractGeminiOutputText(response);
    }

    private Map<String, Object> openAiRequest(Note note, String instructions, int maxOutputTokens) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", openAiModel);
        request.put("instructions", instructions);
        request.put("input", notePrompt(note));
        request.put("max_output_tokens", maxOutputTokens);
        return request;
    }

    private Map<String, Object> geminiRequest(Note note, String instructions, int maxOutputTokens) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("systemInstruction", Map.of("parts", parts(instructions)));
        request.put("contents", java.util.List.of(Map.of(
                "role", "user",
                "parts", parts(notePrompt(note))
        )));
        request.put("generationConfig", Map.of(
                "temperature", 0.3,
                "maxOutputTokens", maxOutputTokens
        ));
        return request;
    }

    private java.util.List<Map<String, String>> parts(String text) {
        return java.util.List.of(Map.of("text", text));
    }

    private String summaryInstructions() {
        return """
                You are an assistant inside a personal knowledge management app.
                Summarize the user's note in a clear, useful way.
                Return:
                1. A short 2-3 sentence summary.
                2. Three key points if present.
                Keep it concise and do not invent facts.
                """;
    }

    private String studyQuestionInstructions() {
        return """
                You are an academic study assistant inside a personal knowledge management app.
                Create viva-friendly study questions from the user's note.
                Return:
                1. Five important questions.
                2. One short answer under each question.
                3. One final revision tip.
                Keep the answers accurate to the note and do not invent facts.
                """;
    }

    private String notePrompt(Note note) {
        return "Title: " + note.getTitle() + "\n\nContent:\n" +
                Optional.ofNullable(note.getPlainText()).orElse("");
    }

    private String extractOpenAiOutputText(String responseBody) throws IOException {
        if (responseBody == null || responseBody.isBlank()) {
            return "";
        }
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode outputText = root.path("output_text");
        if (outputText.isTextual()) {
            return outputText.asText().trim();
        }
        StringBuilder builder = new StringBuilder();
        for (JsonNode output : root.path("output")) {
            for (JsonNode content : output.path("content")) {
                JsonNode text = content.path("text");
                if (text.isTextual()) {
                    if (!builder.isEmpty()) {
                        builder.append(System.lineSeparator()).append(System.lineSeparator());
                    }
                    builder.append(text.asText().trim());
                }
            }
        }
        return builder.toString().trim();
    }

    private String extractGeminiOutputText(String responseBody) throws IOException {
        if (responseBody == null || responseBody.isBlank()) {
            return "";
        }
        JsonNode root = objectMapper.readTree(responseBody);
        StringBuilder builder = new StringBuilder();
        for (JsonNode candidate : root.path("candidates")) {
            for (JsonNode part : candidate.path("content").path("parts")) {
                JsonNode text = part.path("text");
                if (text.isTextual()) {
                    if (!builder.isEmpty()) {
                        builder.append(System.lineSeparator()).append(System.lineSeparator());
                    }
                    builder.append(text.asText().trim());
                }
            }
        }
        return builder.toString().trim();
    }

    private String friendlyApiError(String selectedProvider, RestClientResponseException ex) {
        String apiMessage = extractApiErrorMessage(ex.getResponseBodyAsString());
        String detail = apiMessage.isBlank() ? "" : " Details: " + apiMessage;
        int status = ex.getStatusCode().value();
        if (status == 401) {
            return "AI API key is invalid or missing. Check " + keyName(selectedProvider) + " and restart the app." + detail;
        }
        if (status == 403) {
            return "AI access is not allowed for this key or project. Check your account permissions." + detail;
        }
        if (status == 404) {
            return "AI model was not found or is not available for your account. Try changing " + modelName(selectedProvider) + "." + detail;
        }
        if (status == 429) {
            return "AI request limit or billing limit was reached. Check your AI provider usage and billing." + detail;
        }
        if (status >= 400 && status < 500) {
            return "AI request was rejected. Check " + modelName(selectedProvider) + " and API key settings." + detail;
        }
        return "AI provider is temporarily unavailable. Please try again later." + detail;
    }

    private String keyName(String selectedProvider) {
        return "gemini".equals(selectedProvider) ? "GEMINI_API_KEY" : "OPENAI_API_KEY";
    }

    private String modelName(String selectedProvider) {
        return "gemini".equals(selectedProvider) ? "GEMINI_MODEL" : "OPENAI_MODEL";
    }

    private String extractApiErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "";
        }
        try {
            return objectMapper.readTree(responseBody).path("error").path("message").asText("");
        } catch (IOException ignored) {
            return "";
        }
    }
}
