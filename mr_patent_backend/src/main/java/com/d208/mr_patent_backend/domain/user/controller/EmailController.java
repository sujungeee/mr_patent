package com.d208.mr_patent_backend.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.d208.mr_patent_backend.domain.user.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/request")
    public ResponseEntity<String> sendAuthCode(@RequestParam String email) {
        try {
            emailService.sendAuthEmail(email);
            return ResponseEntity.ok("인증 코드가 발송되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verification")
    public ResponseEntity<Boolean> verifyAuthCode(
            @RequestParam String email,
            @RequestParam String authCode) {
        boolean isValid = emailService.verifyAuthCode(email, authCode);
        return ResponseEntity.ok(isValid);
    }
}