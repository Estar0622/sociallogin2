package com.mycom.feat_sociallogin.oauth2;

import com.mycom.feat_sociallogin.dto.GoogleResponse;
import com.mycom.feat_sociallogin.dto.KakaoResponse;
import com.mycom.feat_sociallogin.dto.OAuth2Response;
import com.mycom.feat_sociallogin.dto.UserDTO;
import com.mycom.feat_sociallogin.entity.MemberEntity;
import com.mycom.feat_sociallogin.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    public CustomOAuth2UserService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
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
        MemberEntity memberEntity = saveOrUpdateUser(oAuth2Response);

        // UserDTO 생성 및 반환
        UserDTO userDTO = new UserDTO();
        userDTO.setProvider(memberEntity.getProvider());
        userDTO.setProviderId(memberEntity.getProviderId());
        userDTO.setNickname(memberEntity.getNickname());
        userDTO.setProfileImage(memberEntity.getProfileImage());
        userDTO.setEmail(memberEntity.getEmail());


        return new CustomOAuth2User(userDTO);
    }

    private MemberEntity saveOrUpdateUser(OAuth2Response oAuth2Response) {
        String provider = oAuth2Response.getProvider();
        String providerId = oAuth2Response.getProviderId();
        String email = oAuth2Response.getEmail();

        // 기본 이메일 처리
        if (email == null || email.isEmpty()) {
            email = provider + "-" + providerId + "@noemail.com";
        }

        log.info("Social login request: provider={}, providerId={}, email={}", provider, providerId, email);

        MemberEntity memberEntity = memberRepository.findByProviderAndProviderId(provider, providerId);

        if (memberEntity == null) {
            // 새 사용자 저장
            memberEntity = new MemberEntity();
            memberEntity.setProvider(provider);
            memberEntity.setProviderId(providerId);
            memberEntity.setEmail(email);
            memberEntity.setNickname(oAuth2Response.getName());

            memberRepository.save(memberEntity);

            log.info("New user registered: {}", memberEntity);  // 새 사용자 등록 로그
        } else {
            // 기존 사용자 업데이트
            memberEntity.setEmail(email);
            memberEntity.setNickname(oAuth2Response.getName());
            memberRepository.save(memberEntity);

            log.info("Existing user updated: {}", memberEntity);  // 기존 사용자 업데이트 로그
        }


        return memberEntity;
    }

}