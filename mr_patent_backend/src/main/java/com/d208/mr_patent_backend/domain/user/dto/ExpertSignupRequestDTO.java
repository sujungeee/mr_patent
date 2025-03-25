package com.d208.mr_patent_backend.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ExpertSignupRequestDTO {
    // User 정보
    private String userEmail;
    private String userPw;
    private String userNickname;
    private String userImage;
    private Integer userRole = 1;

    // Expert 추가 정보
    private String expertName;
    private String expertIdentification;
    private String expertDescription;
    private String expertAddress;
    private String expertPhone;
    private LocalDate expertGetDate;
    private String expertLicense;
    private String expertLicenseNumber;
}
