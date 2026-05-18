package com.mca.pkms.controller;

import com.mca.pkms.dto.TagForm;
import com.mca.pkms.entity.User;
import com.mca.pkms.service.TagService;
import com.mca.pkms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tags")
public class TagController extends BaseController {
    private final TagService tagService;

    public TagController(UserService userService, TagService tagService) {
        super(userService);
        this.tagService = tagService;
    }

    @GetMapping
    public String list(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        model.addAttribute("tags", tagService.list(user));
        model.addAttribute("tagForm", new TagForm());
        return "categories/tags";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute TagForm tagForm, BindingResult bindingResult,
                         Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tags", tagService.list(currentUser(authentication)));
            return "categories/tags";
        }
        tagService.save(tagForm, currentUser(authentication));
        redirectAttributes.addFlashAttribute("success", "Tag saved.");
        return "redirect:/tags";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        tagService.delete(id, currentUser(authentication));
        redirectAttributes.addFlashAttribute("success", "Tag deleted.");
        return "redirect:/tags";
    }
}
