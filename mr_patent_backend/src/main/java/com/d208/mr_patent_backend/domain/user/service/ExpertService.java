package com.d208.mr_patent_backend.domain.user.service;

import com.d208.mr_patent_backend.domain.user.dto.ExpertResponseDTO;
import com.d208.mr_patent_backend.domain.user.entity.Expert;
import com.d208.mr_patent_backend.domain.user.repository.ExpertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpertService {
    private final ExpertRepository expertRepository;

    public List<ExpertResponseDTO> getApprovedExperts() {
        List<Expert> experts = expertRepository.findByExpertStatus(1); // 승인된 변리사만 조회

        return experts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ExpertResponseDTO convertToDTO(Expert expert) {
        // 카테고리 이름 리스트 추출
        List<String> categories = expert.getExpertCategory().stream()
                .map(ec -> ec.getCategory().getCategoryName())
                .collect(Collectors.toList());

        return ExpertResponseDTO.builder()
                .expertId(expert.getExpertId())
                .name(expert.getUser().getUserName())
                .email(expert.getUser().getUserEmail())
                .description(expert.getExpertDescription())
                .address(expert.getExpertAddress())
                .phone(expert.getExpertPhone())
                .license(expert.getExpertLicense())
                .getDate(expert.getExpertGetDate())
                .categories(categories)
                .build();
    }
}