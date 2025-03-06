package com.url.shortener.services;

import com.url.shortener.models.UrlMapping;
import com.url.shortener.repositories.UrlMappingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class UrlShortenerService {

    private final UrlMappingRepository urlMappingRepository;

    public UrlShortenerService(UrlMappingRepository urlMappingRepository) {
        this.urlMappingRepository = urlMappingRepository;
    }

    // Method to shorten a URL
    @Transactional
    public UrlMapping shortenUrl(String originalUrl, String customAlias, LocalDateTime expiresAt) {
        String shortUrl = (customAlias != null && !customAlias.isEmpty()) ? customAlias : generateShortUrl(originalUrl);

        // Check if the custom alias already exists
        if (urlMappingRepository.findByShortUrl(shortUrl).isPresent()) {
            throw new RuntimeException("Custom alias already in use");
        }

        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setShortUrl(shortUrl);
        urlMapping.setCreatedAt(LocalDateTime.now());
        urlMapping.setExpiresAt(expiresAt);
        urlMapping.setClickCount(0);

        return urlMappingRepository.save(urlMapping);
    }


    // Retrieve original URL from short URL
    public Optional<UrlMapping> getOriginalUrl(String shortUrl) {
        return urlMappingRepository.findByShortUrl(shortUrl);
    }

    // Increment click count
    @Transactional
    public void incrementClickCount(UrlMapping urlMapping) {
        urlMapping.setClickCount(urlMapping.getClickCount() + 1);
        urlMappingRepository.save(urlMapping);
    }

    // Generate a short URL using Base64 encoding
    private String generateShortUrl(String url) {
        String encoded = Base64.getUrlEncoder().encodeToString(url.getBytes(StandardCharsets.UTF_8));
        return encoded.substring(0, 8); // Take first 8 characters
    }

    public boolean deleteShortUrl(String shortUrl) {
        Optional<UrlMapping> urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (urlMapping.isPresent()) {
            urlMappingRepository.delete(urlMapping.get());
            return true;
        }
        return false;
    }
}
