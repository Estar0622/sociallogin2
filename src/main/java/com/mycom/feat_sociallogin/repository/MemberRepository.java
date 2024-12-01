package com.mycom.feat_sociallogin.repository;

import com.mycom.feat_sociallogin.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    MemberEntity findByProviderAndProviderId(String provider, String providerId);
}
