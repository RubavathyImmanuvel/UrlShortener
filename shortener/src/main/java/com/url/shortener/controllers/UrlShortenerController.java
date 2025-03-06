package com.url.shortener.controllers;

//import com.url.shortener.models.AccessLog;
import com.url.shortener.models.UrlMapping;
import com.url.shortener.services.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    public UrlShortenerController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    // Shorten URL
    @PostMapping("/url/shorten")
    public ResponseEntity<UrlMapping> shortenUrl(@RequestParam String originalUrl,
                                                 @RequestParam(required = false) LocalDateTime expiresAt) {
        UrlMapping urlMapping = urlShortenerService.shortenUrl(originalUrl, null, expiresAt); // Pass `null` for customAlias
        return ResponseEntity.ok(urlMapping);
    }

    // Redirect to Original URL
    @GetMapping("/url/{shortUrl}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl) {
        Optional<UrlMapping> urlMappingOpt = urlShortenerService.getOriginalUrl(shortUrl);

        if (urlMappingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UrlMapping urlMapping = urlMappingOpt.get();

        // Check if the URL has expired
        if (urlMapping.getExpiresAt() != null && urlMapping.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE).build(); // 410 Gone
        }

        // Increment click count
        urlShortenerService.incrementClickCount(urlMapping);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(urlMapping.getOriginalUrl()));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @PostMapping("/url/shorten/custom")
    public ResponseEntity<UrlMapping> shortenUrlWithCustomAlias(@RequestParam String originalUrl,
                                                                @RequestParam String customAlias,
                                                                @RequestParam(required = false) LocalDateTime expiresAt) {
        UrlMapping urlMapping = urlShortenerService.shortenUrl(originalUrl, customAlias, expiresAt);
        return ResponseEntity.ok(urlMapping);
    }

//    @GetMapping("/url/{shortUrl}")
//    public ResponseEntity<Void> redirect(@PathVariable String shortUrl, HttpServletRequest request) {
//        Optional<UrlMapping> urlMappingOpt = urlShortenerService.getOriginalUrl(shortUrl);
//
//        if (urlMappingOpt.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        UrlMapping urlMapping = urlMappingOpt.get();
//        urlShortenerService.incrementClickCount(urlMapping);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setLocation(URI.create(urlMapping.getOriginalUrl()));
//        return new ResponseEntity<>(headers, HttpStatus.FOUND);
//    }

    @DeleteMapping("/admin/url/{shortUrl}")
    public ResponseEntity<?> deleteShortUrl(@PathVariable String shortUrl) {
        boolean deleted = urlShortenerService.deleteShortUrl(shortUrl);
        if (deleted) {
            return ResponseEntity.ok("Short URL deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Short URL not found.");
        }
    }


}
