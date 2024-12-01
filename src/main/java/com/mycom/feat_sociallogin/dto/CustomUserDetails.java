package com.mycom.feat_sociallogin.dto;

import com.mycom.feat_sociallogin.entity.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final MemberEntity memberEntity;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 기본 권한 ROLE_USER를 하드코딩
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return "ROLE_USER"; // 기본 권한 반환
            }
        });
        return collection;
    }

    @Override
    public String getPassword() {
        // 비밀번호 인증이 없으므로 null 반환
        return null;
    }

    @Override
    public String getUsername() {
        // 사용자 이름 대신 providerId를 고유 식별자로 반환
        return memberEntity.getProvider() + "_" + memberEntity.getProviderId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정이 만료되지 않았다고 간주
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정이 잠기지 않았다고 간주
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 인증 정보가 만료되지 않았다고 간주
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정이 활성화되었다고 간주
    }
}


