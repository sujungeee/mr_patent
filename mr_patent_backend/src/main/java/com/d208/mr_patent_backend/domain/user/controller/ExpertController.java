package com.d208.mr_patent_backend.domain.user.controller;

import com.d208.mr_patent_backend.domain.user.dto.ExpertResponseDTO;
import com.d208.mr_patent_backend.domain.user.service.ExpertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expert")
@RequiredArgsConstructor
public class ExpertController {
    private final ExpertService expertService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getApprovedExperts() {
        List<ExpertResponseDTO> experts = expertService.getApprovedExperts();

        Map<String, Object> response = new HashMap<>();
        response.put("data", experts);

        return ResponseEntity.ok(response);
    }
}