package com.abhiruchi.csvanalyzer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String role; // "user" or "bot"

    @Column(length = 10000) // Increase length for long text or chart JSON
    @Lob
    private String content;

    private String contentType; // "text" or "chart"

    private LocalDateTime timestamp;
}