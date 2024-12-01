package com.mycom.feat_sociallogin.filter;


import com.mycom.feat_sociallogin.dto.CustomUserDetails;
import com.mycom.feat_sociallogin.entity.UserEntity;
import com.mycom.feat_sociallogin.service.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 헤더에서 access키에 담긴 토큰을 꺼냄
        // String accessToken = request.getHeader("access");
        // String accessToken = request.getHeader("Authorization");
        String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.info("{}{} {}{}Authorization: {}", System.lineSeparator(), request.getMethod(), request.getRequestURI(),
                System.lineSeparator(), accessToken);

        // 토큰이 없다면 다음 필터로 넘김
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 만료 여부 확인, 만료시 다음 필터로 넘기지 않음
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {

            //response body
            PrintWriter writer = response.getWriter();
            writer.print("access token expired");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰이 access인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(accessToken);

        if (!category.equals("access")) {

            //response body
            PrintWriter writer = response.getWriter();
            writer.print("invalid access token");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // username, role 값을 획득
        String username = jwtUtil.getUsername(accessToken);
        String role = jwtUtil.getRole(accessToken);

        UserEntity userEntity = new UserEntity();
        userEntity.setProvider(username);
        userEntity.setProviderId(username);
        userEntity.setNickname(username);

// CustomUserDetails 생성
        CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);

// Authentication 객체 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(
                customUserDetails,
                null,
                customUserDetails.getAuthorities() // 기본 권한 ROLE_USER 반환
        );

// SecurityContextHolder에 인증 정보 설정
        SecurityContextHolder.getContext().setAuthentication(authToken);

// 다음 필터로 요청 전달
        filterChain.doFilter(request, response);

    }
}