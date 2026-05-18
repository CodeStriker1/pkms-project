package com.mca.pkms.controller;

import com.mca.pkms.dto.CategoryForm;
import com.mca.pkms.entity.User;
import com.mca.pkms.service.CategoryService;
import com.mca.pkms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController extends BaseController {
    private final CategoryService categoryService;

    public CategoryController(UserService userService, CategoryService categoryService) {
        super(userService);
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        model.addAttribute("categories", categoryService.list(user));
        model.addAttribute("categoryForm", new CategoryForm());
        return "categories/list";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute CategoryForm categoryForm, BindingResult bindingResult,
                         Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.list(currentUser(authentication)));
            return "categories/list";
        }
        categoryService.save(categoryForm, currentUser(authentication));
        redirectAttributes.addFlashAttribute("success", "Category saved.");
        return "redirect:/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        categoryService.delete(id, currentUser(authentication));
        redirectAttributes.addFlashAttribute("success", "Category deleted.");
        return "redirect:/categories";
    }
}
