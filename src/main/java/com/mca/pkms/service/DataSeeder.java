package com.mca.pkms.service;

import com.mca.pkms.dto.CategoryForm;
import com.mca.pkms.dto.NoteForm;
import com.mca.pkms.dto.RegisterRequest;
import com.mca.pkms.dto.TagForm;
import com.mca.pkms.entity.Category;
import com.mca.pkms.entity.Tag;
import com.mca.pkms.entity.User;
import com.mca.pkms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {
    private final boolean enabled;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final NoteService noteService;

    public DataSeeder(@Value("${app.seed.enabled:true}") boolean enabled, UserRepository userRepository,
                      UserService userService, CategoryService categoryService, TagService tagService,
                      NoteService noteService) {
        this.enabled = enabled;
        this.userRepository = userRepository;
        this.userService = userService;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.noteService = noteService;
    }

    @Override
    public void run(String... args) {
        if (!enabled || userRepository.existsByEmailIgnoreCase("student@example.com")) {
            return;
        }
        RegisterRequest request = new RegisterRequest();
        request.setName("MCA Student");
        request.setEmail("student@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        userService.register(request);
        User user = userRepository.findByEmailIgnoreCase("student@example.com").orElseThrow();

        CategoryForm researchForm = new CategoryForm();
        researchForm.setName("Research");
        researchForm.setDescription("Academic references and literature notes");
        researchForm.setColor("#2563eb");
        Category research = categoryService.save(researchForm, user);

        TagForm springForm = new TagForm();
        springForm.setName("Spring Boot");
        springForm.setColor("#16a34a");
        Tag spring = tagService.save(springForm, user);

        NoteForm note = new NoteForm();
        note.setTitle("PKMS Project Objectives");
        note.setContent("<p>Build a secure personal knowledge system with notes, tags, search, favorites, archive, and trash.</p>");
        note.setCategoryId(research.getId());
        note.setTagIds(Set.of(spring.getId()));
        noteService.create(note, user);
    }
}
