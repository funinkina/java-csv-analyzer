package com.abhiruchi.csvanalyzer.controller;

import com.abhiruchi.csvanalyzer.model.User;
import com.abhiruchi.csvanalyzer.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    private final UserRepository userRepository;

    public ViewController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Safely retrieve the user; if not found, force logout to avoid redirect loops
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/logout";
        }

        model.addAttribute("username", user.getUsername());
        return "index";
    }
}