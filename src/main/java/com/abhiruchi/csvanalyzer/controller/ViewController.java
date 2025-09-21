package com.abhiruchi.csvanalyzer.controller;

import com.abhiruchi.csvanalyzer.model.ChatMessage;
import com.abhiruchi.csvanalyzer.model.User;
import com.abhiruchi.csvanalyzer.repository.UserRepository;
import com.abhiruchi.csvanalyzer.services.ChatHistoryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class ViewController {
    private final ChatHistoryService chatHistoryService;
    private final UserRepository userRepository;

    public ViewController(ChatHistoryService chatHistoryService, UserRepository userRepository) {
        this.chatHistoryService = chatHistoryService;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Safely retrieve the user; if not found, force logout to avoid redirect loops
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/logout";
        }

        List<ChatMessage> history = chatHistoryService.getChatHistory(user);
        model.addAttribute("chatHistory", history);
        model.addAttribute("username", user.getUsername());
        return "index";
    }
}