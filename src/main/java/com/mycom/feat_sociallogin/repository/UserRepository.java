package com.mycom.feat_sociallogin.repository;

import com.mycom.feat_sociallogin.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByProviderAndProviderId(String provider, String providerId);
}
