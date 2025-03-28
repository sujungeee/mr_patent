package com.d208.mr_patent_backend.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.d208.mr_patent_backend.domain.user.service.EmailService;
import com.d208.mr_patent_backend.domain.user.service.UserService;
import com.d208.mr_patent_backend.domain.user.dto.EmailAvailableResponseDTO;
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
    private final UserService userService;

    @GetMapping("/check")
    public ResponseEntity<Map<String, EmailAvailableResponseDTO>> checkEmailDuplicate(@RequestParam String email) {
        try {
            boolean isDuplicate = userService.checkEmailDuplicate(email);
            Map<String, EmailAvailableResponseDTO> response = new HashMap<>();
            response.put("data", EmailAvailableResponseDTO.of(!isDuplicate));  // DB에 없으면 available true

            if (!isDuplicate) {  // DB에 없는 경우에만 체크 표시
                emailService.setEmailChecked(email);
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("이메일 중복 체크 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

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