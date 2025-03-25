package com.d208.mr_patent_backend.domain.user.controller;

import com.d208.mr_patent_backend.domain.user.dto.UserSignupRequestDTO;
import com.d208.mr_patent_backend.domain.user.dto.ExpertSignupRequestDTO;
import com.d208.mr_patent_backend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("")
    public ResponseEntity<String> signUpUser(@RequestBody UserSignupRequestDTO requestDto) {
        try {
            userService.signUpUser(requestDto);
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/expert")
    public ResponseEntity<String> signUpExpert(@RequestBody ExpertSignupRequestDTO requestDto) {
        try {
            userService.signUpExpert(requestDto);
            return ResponseEntity.ok("변리사 회원가입이 완료되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/expert/{expert_id}")
    public ResponseEntity<String> approveExpert(@PathVariable("expert_id") Integer expertId) {
        try {
            userService.approveExpert(expertId);
            return ResponseEntity.ok("변리사 승인이 완료되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailDuplicate(@RequestParam String userEmail) {
        try {
            boolean isAvailable = !userService.checkEmailDuplicate(userEmail);
            return ResponseEntity.ok(isAvailable);
        } catch (RuntimeException e) {
            log.error("이메일 중복 체크 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(false);
        }
    }
}
