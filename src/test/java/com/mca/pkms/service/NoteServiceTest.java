package com.mca.pkms.service;

import com.mca.pkms.dto.NoteForm;
import com.mca.pkms.dto.RegisterRequest;
import com.mca.pkms.entity.Note;
import com.mca.pkms.entity.User;
import com.mca.pkms.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class NoteServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private NoteService noteService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void keywordSearchRanksTitleMatchesFirst() {
        User user = createUser("rank@example.com");
        createNote("Spring Security", "<p>Authentication and authorization</p>", user);
        createNote("Hibernate Notes", "<p>Spring appears only in content</p>", user);

        List<Note> results = noteService.search(user, "spring", null, null);

        assertThat(results).hasSize(2);
        assertThat(results.getFirst().getTitle()).isEqualTo("Spring Security");
    }

    @Test
    void softDeleteAndRestoreKeepsNoteRecoverable() {
        User user = createUser("restore@example.com");
        Note note = createNote("Trash Test", "<p>Recover me</p>", user);

        noteService.softDelete(note.getId(), user);
        assertThat(noteService.trash(user)).hasSize(1);

        noteService.restore(note.getId(), user);
        assertThat(noteService.trash(user)).isEmpty();
        assertThat(noteService.dashboardNotes(user)).hasSize(1);
    }

    private User createUser(String email) {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail(email);
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        userService.register(request);
        return userRepository.findByEmailIgnoreCase(email).orElseThrow();
    }

    private Note createNote(String title, String content, User user) {
        NoteForm form = new NoteForm();
        form.setTitle(title);
        form.setContent(content);
        return noteService.create(form, user);
    }
}
