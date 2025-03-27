package com.d208.mr_patent_backend.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.d208.mr_patent_backend.domain.user.service.EmailService;
import com.d208.mr_patent_backend.domain.user.service.UserService;
import com.d208.mr_patent_backend.domain.user.dto.EmailVerificationResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/request")
    public ResponseEntity<String> sendAuthCode(@RequestParam String email) {
        try {
            // 중복 확인을 했는지 체크
            if (!emailService.isEmailChecked(email)) {
                return ResponseEntity.badRequest().body("먼저 중복 확인을 해 주세요.");
            }

            emailService.sendAuthEmail(email);
            return ResponseEntity.ok("인증 코드가 발송되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verification")
    public ResponseEntity<?> verifyAuthCode(
            @RequestParam String email,
            @RequestParam String authCode) {
        boolean isValid = emailService.verifyAuthCode(email, authCode);
        if (isValid) {
            Map<String, EmailVerificationResponseDTO> response = new HashMap<>();
            response.put("data", EmailVerificationResponseDTO.of(email));
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body("인증번호가 올바르지 않거나 만료되었습니다.");
    }
}