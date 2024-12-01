package com.mycom.feat_sociallogin.dto;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {
    private final Map<String, Object> attributes;

    public KakaoResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount != null && kakaoAccount.containsKey("email")) {
            return kakaoAccount.get("email").toString();
        }
        return null; // 이메일이 없는 경우 null 반환
    }


    @Override
    public String getName() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        return properties != null ? properties.get("nickname").toString() : null;
    }
}