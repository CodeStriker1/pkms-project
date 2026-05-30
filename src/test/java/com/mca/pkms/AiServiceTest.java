package com.mca.pkms;

import com.mca.pkms.dto.AiSummaryResponse;
import com.mca.pkms.entity.Note;
import com.mca.pkms.service.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AiServiceTest {
    @Autowired
    private AiService aiService;

    @Test
    void summarizeReturnsHelpfulMessageWhenApiKeyIsMissing() {
        Note note = new Note();
        note.setTitle("Revision Plan");
        note.setPlainText("Study Spring Boot controllers and Thymeleaf templates.");

        AiSummaryResponse response = aiService.summarize(note);

        assertThat(response.configured()).isFalse();
        assertThat(response.summary()).isBlank();
        assertThat(response.message()).contains("GEMINI_API_KEY").contains("OPENAI_API_KEY");
    }
}
