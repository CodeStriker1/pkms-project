package com.mca.pkms.service;

import com.mca.pkms.dto.RegisterRequest;
import com.mca.pkms.entity.User;
import com.mca.pkms.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void displayNameCanBePersonalizedAfterRegistration() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Harsh Student");
        request.setEmail("display@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        userService.register(request);
        User user = userRepository.findByEmailIgnoreCase("display@example.com").orElseThrow();

        assertThat(user.needsDisplayNameSetup()).isTrue();
        assertThat(user.getPreferredName()).isEqualTo("Harsh Student");

        userService.updateDisplayName(user, "Harsh");

        User updated = userRepository.findByEmailIgnoreCase("display@example.com").orElseThrow();
        assertThat(updated.needsDisplayNameSetup()).isFalse();
        assertThat(updated.getPreferredName()).isEqualTo("Harsh");
        assertThat(updated.getInitials()).isEqualTo("HA");
    }
}
