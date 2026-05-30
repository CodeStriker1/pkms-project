package com.mca.pkms.controller;

import com.mca.pkms.dto.PasswordForm;
import com.mca.pkms.dto.ProfileForm;
import com.mca.pkms.entity.User;
import com.mca.pkms.exception.BadRequestException;
import com.mca.pkms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController extends BaseController {
    private final UserService userService;

    public ProfileController(UserService userService) {
        super(userService);
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        ProfileForm profileForm = new ProfileForm();
        profileForm.setName(user.getName());
        profileForm.setEmail(user.getEmail());
        profileForm.setDisplayName(user.getPreferredName());
        model.addAttribute("user", user);
        model.addAttribute("profileForm", profileForm);
        model.addAttribute("passwordForm", new PasswordForm());
        return "profile/index";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute ProfileForm profileForm, BindingResult bindingResult,
                                Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", currentUser(authentication));
            model.addAttribute("passwordForm", new PasswordForm());
            return "profile/index";
        }
        try {
            userService.updateProfile(currentUser(authentication), profileForm);
        } catch (BadRequestException ex) {
            bindingResult.reject("profile", ex.getMessage());
            model.addAttribute("user", currentUser(authentication));
            model.addAttribute("passwordForm", new PasswordForm());
            return "profile/index";
        }
        redirectAttributes.addFlashAttribute("success", "Profile updated.");
        return "redirect:/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(@Valid @ModelAttribute PasswordForm passwordForm, BindingResult bindingResult,
                                 Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            User user = currentUser(authentication);
            ProfileForm profileForm = new ProfileForm();
            profileForm.setName(user.getName());
            profileForm.setEmail(user.getEmail());
            profileForm.setDisplayName(user.getPreferredName());
            model.addAttribute("user", user);
            model.addAttribute("profileForm", profileForm);
            return "profile/index";
        }
        try {
            userService.changePassword(currentUser(authentication), passwordForm);
        } catch (BadRequestException ex) {
            bindingResult.reject("password", ex.getMessage());
            User user = currentUser(authentication);
            ProfileForm profileForm = new ProfileForm();
            profileForm.setName(user.getName());
            profileForm.setEmail(user.getEmail());
            profileForm.setDisplayName(user.getPreferredName());
            model.addAttribute("user", user);
            model.addAttribute("profileForm", profileForm);
            return "profile/index";
        }
        redirectAttributes.addFlashAttribute("success", "Password changed.");
        return "redirect:/profile";
    }
}
