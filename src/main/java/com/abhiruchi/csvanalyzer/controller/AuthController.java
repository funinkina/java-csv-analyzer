package com.abhiruchi.csvanalyzer.controller;

import com.abhiruchi.csvanalyzer.model.User;
import com.abhiruchi.csvanalyzer.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        // Check if user is fully authenticated (not anonymous) and has proper authority
        if (authentication != null &&
                authentication.isAuthenticated() &&
                !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))) {
            return "redirect:/";
        }
        // Return the view name. Spring Boot with Thymeleaf will resolve this to
        // /resources/templates/login.html
        return "login";
    }

    @GetMapping("/register")
    public String register(Authentication authentication) {
        // If the user is already authenticated (not anonymous), redirect them to the
        // home page
        if (authentication != null &&
                authentication.isAuthenticated() &&
                !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))) {
            return "redirect:/";
        }
        // Return the view name. Spring Boot with Thymeleaf will resolve this to
        // /resources/templates/register.html
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(String username, String password) {
        // Check if user already exists to avoid errors
        if (userRepository.findByUsername(username).isPresent()) {
            // You might want to add an error message to the redirect
            return "redirect:/register?error";
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return "redirect:/login?success"; // Redirect to login after successful registration
    }
}