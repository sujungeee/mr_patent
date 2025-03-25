package com.d208.mr_patent_backend.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class UserSignupRequestDTO {
    private String userEmail;
    private String userPw;
    private String userNickname;
    private String userImage;
    private Integer userRole = 0;
}
