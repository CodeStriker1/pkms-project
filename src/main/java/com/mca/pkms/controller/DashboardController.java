package com.mca.pkms.controller;

import com.mca.pkms.entity.User;
import com.mca.pkms.service.CategoryService;
import com.mca.pkms.service.NoteService;
import com.mca.pkms.service.TagService;
import com.mca.pkms.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController extends BaseController {
    private final NoteService noteService;
    private final CategoryService categoryService;
    private final TagService tagService;

    public DashboardController(UserService userService, NoteService noteService, CategoryService categoryService, TagService tagService) {
        super(userService);
        this.noteService = noteService;
        this.categoryService = categoryService;
        this.tagService = tagService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        if (user.needsDisplayNameSetup()) {
            return "redirect:/onboarding/display-name";
        }
        model.addAttribute("user", user);
        model.addAttribute("stats", noteService.stats(user));
        model.addAttribute("notes", noteService.dashboardNotes(user));
        model.addAttribute("categories", categoryService.list(user));
        model.addAttribute("tags", tagService.list(user));
        return "dashboard";
    }
}
