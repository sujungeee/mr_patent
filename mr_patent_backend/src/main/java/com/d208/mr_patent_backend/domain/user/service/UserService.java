package com.d208.mr_patent_backend.domain.user.service;

import com.d208.mr_patent_backend.domain.user.dto.UserSignupRequestDTO;
import com.d208.mr_patent_backend.domain.user.dto.ExpertSignupRequestDTO;
import com.d208.mr_patent_backend.domain.user.entity.User;
import com.d208.mr_patent_backend.domain.user.entity.Expert;
import com.d208.mr_patent_backend.domain.user.repository.UserRepository;
import com.d208.mr_patent_backend.domain.user.repository.ExpertRepository;
import com.d208.mr_patent_backend.domain.category.entity.ExpertCategory;
import com.d208.mr_patent_backend.domain.category.entity.Category;
import com.d208.mr_patent_backend.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.d208.mr_patent_backend.global.jwt.TokenInfo;
import com.d208.mr_patent_backend.global.jwt.JwtTokenProvider;
import com.d208.mr_patent_backend.domain.user.dto.LoginRequestDTO;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ExpertRepository expertRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;

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
        user.setUserName(requestDto.getUserName());
        user.setUserImage(requestDto.getUserImage());
        user.setUserRole(requestDto.getUserRole());

        userRepository.save(user);
    }

    @Transactional
    public void signUpExpert(ExpertSignupRequestDTO requestDto) {
        String email = requestDto.getUserEmail();

        // 이메일 중복 체크 확인
        if (!emailService.isEmailChecked(email)) {
            throw new RuntimeException("이메일 중복 확인이 필요합니다.");
        }

        // 이메일 인증 확인
        if (!emailService.isVerifiedEmail(email)) {
            throw new RuntimeException("이메일 인증이 필요합니다.");
        }

        // User 엔티티 생성 및 저장
        User user = new User();
        user.setUserEmail(requestDto.getUserEmail());
        user.setUserPw(passwordEncoder.encode(requestDto.getUserPw()));
        user.setUserName(requestDto.getUserName());
        user.setUserImage(requestDto.getUserImage());
        user.setUserRole(requestDto.getUserRole());

        userRepository.save(user);

        // Expert 엔티티 생성 및 저장
        Expert expert = new Expert();
        expert.setUser(user);
        expert.setExpertIdentification(requestDto.getExpertIdentification());
        expert.setExpertDescription(requestDto.getExpertDescription());
        expert.setExpertAddress(requestDto.getExpertAddress());
        expert.setExpertPhone(requestDto.getExpertPhone());
        expert.setExpertLicense(requestDto.getExpertLicense());
        expert.setExpertGetDate(requestDto.getExpertGetDate());

        // ExpertCategory 설정
        if (requestDto.getExpertCategory() != null && !requestDto.getExpertCategory().isEmpty()) {
            for (ExpertCategory categoryDto : requestDto.getExpertCategory()) {
                Integer categoryId = categoryDto.getCategory().getCategoryId();
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("존재하지 않는 카테고리입니다: " + categoryId));

                // 새로운 ExpertCategory 객체 생성
                ExpertCategory newExpertCategory = new ExpertCategory();
                newExpertCategory.setExpert(expert);
                newExpertCategory.setCategory(category);

                // 양방향 관계 설정
                expert.getExpertCategory().add(newExpertCategory);
                category.getExpertCategory().add(newExpertCategory);
            }
        }

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

    // 이메일 중복 체크
    public boolean checkEmailDuplicate(String email) {
        return userRepository.existsByUserEmail(email);
    }

    @Transactional
    public TokenInfo login(LoginRequestDTO requestDto) {
        // 이메일로 사용자 찾기
        User user = userRepository.findByUserEmail(requestDto.getUserEmail())
                .orElseThrow(() -> new RuntimeException("가입되지 않은 이메일입니다."));

        // 변리사인 경우 승인 상태 확인
        if (user.getUserRole() == 1) {
            Expert expert = expertRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("변리사 정보를 찾을 수 없습니다."));
            if (expert.getExpertStatus() == 0) {
                throw new RuntimeException("아직 승인되지 않은 변리사입니다.");
            }
        }

        // 비밀번호 확인
        if (!passwordEncoder.matches(requestDto.getUserPw(), user.getUserPw())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 권한 설정
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getUserRole() == 1) {
            authorities.add(new SimpleGrantedAuthority("ROLE_EXPERT"));
        } else if (user.getUserRole() == 2) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // 인증 객체 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUserEmail(), "", authorities);

        // 토큰 생성
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

        // Refresh Token 저장
        user.setUserRefreshToken(tokenInfo.getRefreshToken());
        userRepository.save(user);

        return tokenInfo;
    }

    @Transactional
    public void logout(String accessToken) {
        // 토큰에서 사용자 이메일 추출
        String userEmail = jwtTokenProvider.getUserEmail(accessToken);

        // 사용자 찾기
        User user = userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // Refresh Token 제거
        user.setUserRefreshToken(null);
        userRepository.save(user);
    }

    @Transactional
    public TokenInfo reissue(String refreshToken) {
        // 리프레시 토큰 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
        }

        // DB에서 리프레시 토큰으로 사용자 찾기
        User user = userRepository.findByUserRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("토큰에 해당하는 사용자가 없습니다."));

        // 권한 설정
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getUserRole() == 1) {
            authorities.add(new SimpleGrantedAuthority("ROLE_EXPERT"));
        } else if (user.getUserRole() == 2) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // 새로운 토큰 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUserEmail(), "", authorities);
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

        // 리프레시 토큰 업데이트
        user.setUserRefreshToken(tokenInfo.getRefreshToken());
        userRepository.save(user);

        return tokenInfo;
    }
}
