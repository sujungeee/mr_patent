package com.d208.mr_patent_backend.domain.user.controller;

import com.d208.mr_patent_backend.domain.user.dto.UserSignupRequestDTO;
import com.d208.mr_patent_backend.domain.user.dto.ExpertSignupRequestDTO;
import com.d208.mr_patent_backend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import com.d208.mr_patent_backend.global.jwt.TokenInfo;

import jakarta.validation.Valid;

import com.d208.mr_patent_backend.domain.user.dto.LoginRequestDTO;
import com.d208.mr_patent_backend.domain.user.dto.TokenRequestDTO;
import com.d208.mr_patent_backend.domain.user.dto.UserInfoResponseDTO;
import com.d208.mr_patent_backend.domain.user.dto.UserUpdateRequestDTO;
import com.d208.mr_patent_backend.domain.user.dto.UserUpdateResponseDTO;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("")
    public ResponseEntity<String> signUpUser(@Valid @RequestBody UserSignupRequestDTO requestDto) {
        try {
            userService.signUpUser(requestDto);
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/expert")
    public ResponseEntity<String> signUpExpert(@Valid @RequestBody ExpertSignupRequestDTO requestDto) {
        try {
            userService.signUpExpert(requestDto);
            return ResponseEntity.ok("변리사 회원가입이 완료되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<TokenInfo> login(@Valid @RequestBody LoginRequestDTO requestDto) {
        try {
            TokenInfo tokenInfo = userService.login(requestDto);
            return ResponseEntity.ok(tokenInfo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        try {
            // Bearer 토큰에서 실제 토큰 값만 추출
            String accessToken = token.substring(7);
            userService.logout(accessToken);
            return ResponseEntity.ok("로그아웃이 완료되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reissue")
    public ResponseEntity<Map<String, Object>> reissue(@RequestBody TokenRequestDTO tokenRequestDTO) {
        try {
            TokenInfo tokenInfo = userService.reissue(tokenRequestDTO.getRefreshToken());

            Map<String, Object> response = new HashMap<>();
            response.put("data", tokenInfo);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getUserInfo() {
        UserInfoResponseDTO userInfo = userService.getUserInfo();

        Map<String, Object> response = new HashMap<>();
        response.put("data", userInfo);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<Map<String, Object>> updateUserInfo(
            @Valid @RequestBody UserUpdateRequestDTO requestDto) {
        UserUpdateResponseDTO responseDto = userService.updateUserInfo(requestDto);

        Map<String, Object> response = new HashMap<>();
        response.put("data", responseDto);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Map<String, Object>> deleteUser() {
        userService.deleteUser();

        Map<String, Object> response = new HashMap<>();
        Map<String, String> data = new HashMap<>();
        data.put("message", "회원 탈퇴가 완료되었습니다.");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me/pw")
    public ResponseEntity<Map<String, Object>> updatePassword(@RequestBody Map<String, String> request) {
        String currentPassword = request.get("current_password");
        String newPassword = request.get("new_password");

        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            throw new RuntimeException("현재 비밀번호는 필수 입력값입니다.");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("새 비밀번호는 필수 입력값입니다.");
        }

        userService.updatePassword(currentPassword, newPassword);

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("message", "비밀번호가 성공적으로 변경되었습니다.");
        data.put("user_updated_at", LocalDateTime.now());
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("user_email");
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("이메일은 필수 입력값입니다.");
        }

        userService.sendPasswordResetEmail(email);

        Map<String, Object> response = new HashMap<>();
        Map<String, String> data = new HashMap<>();
        data.put("message", "비밀번호 재설정 이메일이 전송되었습니다.");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/password/verification")
    public ResponseEntity<Map<String, Object>> verifyPasswordCode(@RequestBody Map<String, String> request) {
        String email = request.get("user_email");
        String authCode = request.get("verification_code");

        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("이메일은 필수 입력값입니다.");
        }
        if (authCode == null || authCode.trim().isEmpty()) {
            throw new RuntimeException("인증 코드는 필수 입력값입니다.");
        }

        userService.verifyPasswordCode(email, authCode);

        Map<String, Object> response = new HashMap<>();
        Map<String, String> data = new HashMap<>();
        data.put("message", "이메일 인증이 완료되었습니다.");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("user_email");
        String newPassword = request.get("new_password");

        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("이메일은 필수 입력값입니다.");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("새 비밀번호는 필수 입력값입니다.");
        }

        // 이메일 인증 여부 확인 후 비밀번호 변경
        userService.resetPassword(email, newPassword);

        Map<String, Object> response = new HashMap<>();
        Map<String, String> data = new HashMap<>();
        data.put("message", "비밀번호가 성공적으로 재설정되었습니다.");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}
