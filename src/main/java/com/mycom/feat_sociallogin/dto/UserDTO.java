package com.mycom.feat_sociallogin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private String provider;       // Google, Kakao 등 제공자 정보
    private String providerId;    // 제공자가 발급한 고유 ID
    private String nickname;      // 사용자의 닉네임
    private String profileImage;  // 프로필 이미지 URL
    private String email;         // 사용자 이메일
}
