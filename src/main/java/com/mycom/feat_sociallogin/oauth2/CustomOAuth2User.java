package com.mycom.feat_sociallogin.oauth2;


import com.mycom.feat_sociallogin.dto.UserDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final UserDTO userDTO;

    public CustomOAuth2User(UserDTO userDTO) {

        this.userDTO = userDTO;
    }

    @Override
    public Map<String, Object> getAttributes() {

        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                // 권한이 없으면 기본 ROLE_USER 반환
                return "ROLE_USER";
            }
        });
        return collection;
    }

    @Override
    public String getName() {
        // userDTO.getName() 대신 nickname을 반환
        return userDTO.getNickname() != null ? userDTO.getNickname() : "Unknown User";
    }

    public String getUsername() {
        // userDTO.getUsername() 대신 provider + providerId 반환
        return userDTO.getProvider() + "_" + userDTO.getProviderId();
    }
}