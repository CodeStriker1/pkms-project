package com.mca.pkms.controller;

import com.mca.pkms.entity.User;
import com.mca.pkms.service.NoteService;
import com.mca.pkms.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LibraryController extends BaseController {
    private final NoteService noteService;

    public LibraryController(UserService userService, NoteService noteService) {
        super(userService);
        this.noteService = noteService;
    }

    @GetMapping("/favorites")
    public String favorites(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        model.addAttribute("notes", noteService.favorites(user));
        model.addAttribute("pageTitle", "Favorite Notes");
        return "notes/simple-list";
    }

    @GetMapping("/archive")
    public String archive(Authentication authentication, Model model) {
        model.addAttribute("notes", noteService.archived(currentUser(authentication)));
        model.addAttribute("pageTitle", "Archived Notes");
        return "notes/simple-list";
    }

    @GetMapping("/trash")
    public String trash(Authentication authentication, Model model) {
        model.addAttribute("notes", noteService.trash(currentUser(authentication)));
        return "notes/trash";
    }
}
