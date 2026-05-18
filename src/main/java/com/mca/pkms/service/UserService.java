package com.mca.pkms.service;

import com.mca.pkms.dto.PasswordForm;
import com.mca.pkms.dto.ProfileForm;
import com.mca.pkms.dto.RegisterRequest;
import com.mca.pkms.entity.Role;
import com.mca.pkms.entity.User;
import com.mca.pkms.exception.BadRequestException;
import com.mca.pkms.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match.");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BadRequestException("Email is already registered.");
        }
        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);
    }

    public User current(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadRequestException("Authenticated user not found."));
    }

    @Transactional
    public void updateProfile(User user, ProfileForm form) {
        String email = form.getEmail().trim().toLowerCase();
        userRepository.findByEmailIgnoreCase(email)
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> { throw new BadRequestException("Email is already used by another account."); });
        user.setName(form.getName().trim());
        user.setEmail(email);
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(User user, PasswordForm form) {
        if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect.");
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new BadRequestException("New passwords do not match.");
        }
        user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        userRepository.save(user);
    }
}
