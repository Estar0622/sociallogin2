package com.mycom.feat_sociallogin.oauth2;

import com.mycom.feat_sociallogin.dto.GoogleResponse;
import com.mycom.feat_sociallogin.dto.KakaoResponse;
import com.mycom.feat_sociallogin.dto.OAuth2Response;
import com.mycom.feat_sociallogin.dto.UserDTO;
import com.mycom.feat_sociallogin.entity.UserEntity;
import com.mycom.feat_sociallogin.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 소셜 로그인으로 가져온 사용자 정보
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 소셜 로그인 제공자 (google, kakao 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response;

        if ("kakao".equals(registrationId)) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else if ("google".equals(registrationId)) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }

        // UserEntity 저장 또는 업데이트
        UserEntity userEntity = saveOrUpdateUser(oAuth2Response);

        // UserDTO 생성 및 반환
        UserDTO userDTO = new UserDTO();
        userDTO.setProvider(userEntity.getProvider());
        userDTO.setProviderId(userEntity.getProviderId());
        userDTO.setNickname(userEntity.getNickname());
        userDTO.setProfileImage(userEntity.getProfileImage());
        userDTO.setEmail(userEntity.getEmail());


        return new CustomOAuth2User(userDTO);
    }

    private UserEntity saveOrUpdateUser(OAuth2Response oAuth2Response) {
        String provider = oAuth2Response.getProvider();
        String providerId = oAuth2Response.getProviderId();
        String email = oAuth2Response.getEmail();

        // 기본 이메일 처리
        if (email == null || email.isEmpty()) {
            email = provider + "-" + providerId + "@noemail.com";
        }

        log.info("Social login request: provider={}, providerId={}, email={}", provider, providerId, email);

        UserEntity userEntity = userRepository.findByProviderAndProviderId(provider, providerId);

        if (userEntity == null) {
            // 새 사용자 저장
            userEntity = new UserEntity();
            userEntity.setProvider(provider);
            userEntity.setProviderId(providerId);
            userEntity.setEmail(email);
            userEntity.setNickname(oAuth2Response.getName());

            userRepository.save(userEntity);

            log.info("New user registered: {}", userEntity);  // 새 사용자 등록 로그
        } else {
            // 기존 사용자 업데이트
            userEntity.setEmail(email);
            userEntity.setNickname(oAuth2Response.getName());
            userRepository.save(userEntity);

            log.info("Existing user updated: {}", userEntity);  // 기존 사용자 업데이트 로그
        }


        return userEntity;
    }

}