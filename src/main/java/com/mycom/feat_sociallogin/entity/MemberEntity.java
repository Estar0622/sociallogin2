package com.mycom.feat_sociallogin.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String provider;       // Google, Kakao 등 제공자 정보
    private String providerId;    // 제공자가 발급한 고유 ID
    private String nickname;      // 사용자의 닉네임
    private String profileImage;  // 프로필 이미지 URL
    private String email;         // 사용자 이메일
}
