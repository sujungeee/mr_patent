package com.d208.mr_patent_backend.domain.user.service;

import com.d208.mr_patent_backend.domain.user.dto.UserSignupRequestDTO;
import com.d208.mr_patent_backend.domain.user.dto.ExpertSignupRequestDTO;
import com.d208.mr_patent_backend.domain.user.entity.User;
import com.d208.mr_patent_backend.domain.user.entity.Expert;
import com.d208.mr_patent_backend.domain.user.repository.UserRepository;
import com.d208.mr_patent_backend.domain.user.repository.ExpertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ExpertRepository expertRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public void signUpUser(UserSignupRequestDTO requestDto) {
        String email = requestDto.getUserEmail();

        // 이메일 중복 체크
        if (userRepository.existsByUserEmail(email)) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        // 이메일 인증 확인
        if (!emailService.isVerifiedEmail(email)) {
            throw new RuntimeException("이메일 인증이 필요합니다.");
        }

        // User 엔티티 생성 및 저장
        User user = new User();
        user.setUserEmail(requestDto.getUserEmail());
        user.setUserPw(passwordEncoder.encode(requestDto.getUserPw()));
        user.setUserNickname(requestDto.getUserNickname());
        user.setUserImage(requestDto.getUserImage());
        user.setUserRole(requestDto.getUserRole());

        userRepository.save(user);
    }

    @Transactional
    public void signUpExpert(ExpertSignupRequestDTO requestDto) {
        String email = requestDto.getUserEmail();

        // 이메일 중복 체크
        if (userRepository.existsByUserEmail(requestDto.getUserEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        // 이메일 인증 확인
        if (!emailService.isVerifiedEmail(email)) {
            throw new RuntimeException("이메일 인증이 필요합니다.");
        }

        // User 엔티티 생성 및 저장
        User user = new User();
        user.setUserEmail(requestDto.getUserEmail());
        user.setUserPw(passwordEncoder.encode(requestDto.getUserPw()));
        user.setUserNickname(requestDto.getUserNickname());
        user.setUserImage(requestDto.getUserImage());
        user.setUserRole(requestDto.getUserRole());

        userRepository.save(user);

        // Expert 엔티티 생성 및 저장
        Expert expert = new Expert();
        expert.setUser(user);
        expert.setExpertName(requestDto.getExpertName());
        expert.setExpertIdentification(requestDto.getExpertIdentification());
        expert.setExpertDescription(requestDto.getExpertDescription());
        expert.setExpertAddress(requestDto.getExpertAddress());
        expert.setExpertPhone(requestDto.getExpertPhone());
        expert.setExpertGetDate(requestDto.getExpertGetDate());
        expert.setExpertLicense(requestDto.getExpertLicense());
        expert.setExpertLicenseNumber(requestDto.getExpertLicenseNumber());

        expertRepository.save(expert);
    }

    @Transactional
    public void approveExpert(Integer expertId) {
        Expert expert = expertRepository.findById(expertId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 변리사입니다."));

        // 이미 승인된 변리사인지 확인
        if (expert.getExpertStatus() == 1) {
            throw new RuntimeException("이미 승인된 변리사입니다.");
        }

        // 변리사 승인 상태로 변경
        expert.setExpertStatus(1);

        // 해당 유저의 역할도 변리사로 변경 (userRole = 1: 변리사)
        User user = expert.getUser();
        user.setUserRole(1);

        expertRepository.save(expert);
        userRepository.save(user);
    }

    // 이메일 중복 체크 메서드 추가
    public boolean checkEmailDuplicate(String email) {
        return userRepository.existsByUserEmail(email);
    }
}
