package com.url.shortener.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "url_mapping")
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String originalUrl;

    @Column(unique = true, nullable = false, length = 10)
    private String shortUrl;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    private int clickCount;
}
