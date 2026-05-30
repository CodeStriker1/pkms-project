package com.mca.pkms.controller;

import com.mca.pkms.dto.AiSummaryResponse;
import com.mca.pkms.entity.Note;
import com.mca.pkms.entity.User;
import com.mca.pkms.service.AiService;
import com.mca.pkms.service.NoteService;
import com.mca.pkms.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiController extends BaseController {
    private final NoteService noteService;
    private final AiService aiService;

    public AiController(UserService userService, NoteService noteService, AiService aiService) {
        super(userService);
        this.noteService = noteService;
        this.aiService = aiService;
    }

    @PostMapping("/notes/{id}/ai/summary")
    public AiSummaryResponse summarize(@PathVariable Long id, Authentication authentication) {
        User user = currentUser(authentication);
        Note note = noteService.find(id, user);
        return aiService.summarize(note);
    }

    @PostMapping("/notes/{id}/ai/study-questions")
    public AiSummaryResponse studyQuestions(@PathVariable Long id, Authentication authentication) {
        User user = currentUser(authentication);
        Note note = noteService.find(id, user);
        return aiService.studyQuestions(note);
    }
}
