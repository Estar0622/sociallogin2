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
        response.setHeader(HttpHeaders.AUTHORIZATION, accessToken);
        response.addCookie(createCookie("refresh", refreshToken));
        response.setStatus(HttpStatus.OK.value());

        RequestCache requestCache = new HttpSessionRequestCache();
        String redirectUrl = Optional.ofNullable(requestCache.getRequest(request, response))
                .map(SavedRequest::getRedirectUrl)
                .orElse("/");

        response.sendRedirect(redirectUrl);
    }

    private void addRefreshEntity(String username, String refreshToken, Long expiredMs) {
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
