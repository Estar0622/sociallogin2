package com.mycom.feat_sociallogin.oauth2;

import com.mycom.feat_sociallogin.entity.RefreshEntity;
import com.mycom.feat_sociallogin.repository.RefreshRepository;
import com.mycom.feat_sociallogin.service.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

@Slf4j
@Component
@RequiredArgsConstructor
// public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // OAuth2User 정보를 가져옴
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String role = authorities.iterator().next().getAuthority();

        // Access와 Refresh 토큰 생성
        String accessToken = jwtUtil.createJwt("access", username, role, 600000L); // 10분
        String refreshToken = jwtUtil.createJwt("refresh", username, role, 2592000000L); // 30일

        // Refresh 토큰 저장
        addRefreshEntity(username, refreshToken, 2592000000L);
        log.info("accessToken: {}", accessToken);
        log.info("refreshToken: {}", refreshToken);

        // 응답 설정: Access는 헤더에, Refresh는 쿠키에 저장
        // FIXME: 문자열 하드코딩보다는 상수로 관리하는 것이 좋음
        // response.setHeader("Authorization", accessToken);
        response.setHeader(HttpHeaders.AUTHORIZATION, accessToken);
        response.addCookie(createCookie("refresh", refreshToken));
        response.setStatus(HttpStatus.OK.value());

        /*
         * TODO:
         *  예를 들어 인덱스 페이지 접근 (미인증 상태) -> 로그인 시도 -> 로그인 성공 -> 인덱스 페이지로 이동
         *  쇼핑몰에서 마이페이지 URL 을 알고 있을 때 바로 접근 (미인증 상태) -> 인증 성공 -> 요청했던 마이페이지 URL 로 이동
         */
        RequestCache requestCache = new HttpSessionRequestCache();
        String redirectUrl = Optional.ofNullable(requestCache.getRequest(request, response))
                .map(SavedRequest::getRedirectUrl)
                .orElse("/");

        /*
         * FIXME #1: access Token 을 로그인 인증 성공하면 FE 에 전달하는 방법 -> 이후 요청 시 헤더에 access Token 을 넣어서 요청
         *  Next.js FE -> /places/1 API 를 호출할 때, Authorization 헤더에 인증 토큰을 세팅해서 요청해주세요.
         *  FE -> 인증이 필요한 API -> 헤더에 인증 정보를 세팅해야 한다.
         *
         * FIXME #2: 프론트가 /places/1 API, 쿠키에 저장되어 있는 refresh Token 을 API 전달
         *  헤더에 refreshToken 을 꺼내서 만료 안됐다 -> 만료 기간 갱신도 새로 해주고, 새로운 access Token 발급해줄 수 있음 (정하기 나름)
         *  API 에서는 refresh 토큰을 DB 로 조회해서 만료 여부 확인
         */

        response.sendRedirect(redirectUrl);
    }

    private void addRefreshEntity(String username, String refreshToken, Long expiredMs) {
        // Access Token, Refresh Token
        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refreshToken);
        refreshEntity.setExpiration(new Date(System.currentTimeMillis() + expiredMs).toString());
        refreshRepository.save(refreshEntity);
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(2592000); // 30일
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        return cookie;
    }


}
