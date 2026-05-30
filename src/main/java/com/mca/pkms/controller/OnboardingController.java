package com.mca.pkms.controller;

import com.mca.pkms.dto.DisplayNameForm;
import com.mca.pkms.entity.User;
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
public class OnboardingController extends BaseController {
    private final UserService userService;

    public OnboardingController(UserService userService) {
        super(userService);
        this.userService = userService;
    }

    @GetMapping("/onboarding/display-name")
    public String displayName(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        if (!user.needsDisplayNameSetup()) {
            return "redirect:/dashboard";
        }
        DisplayNameForm form = new DisplayNameForm();
        form.setDisplayName(user.getName());
        model.addAttribute("displayNameForm", form);
        return "onboarding/display-name";
    }

    @PostMapping("/onboarding/display-name")
    public String saveDisplayName(@Valid @ModelAttribute DisplayNameForm displayNameForm,
                                  BindingResult bindingResult,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "onboarding/display-name";
        }
        userService.updateDisplayName(currentUser(authentication), displayNameForm.getDisplayName());
        redirectAttributes.addFlashAttribute("success", "Your workspace is ready.");
        return "redirect:/dashboard";
    }
}
