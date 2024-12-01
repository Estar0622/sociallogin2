package com.mycom.feat_sociallogin.filter;

import com.mycom.feat_sociallogin.dto.CustomUserDetails;
import com.mycom.feat_sociallogin.entity.MemberEntity;
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
import org.springframework.security.core.userdetails.User;
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

        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setProvider(username);
        memberEntity.setProviderId(username);
        memberEntity.setNickname(username);

// CustomUserDetails 생성
        /*
         * TODO: 인증된 사용자 정보를 만드는 방법
         *  1. User 클래스를 사용하는 방법
         *  2. JPA 엔티티에 UserDetails 인터페이스를 구현한 클래스를 사용하는 방법
         *  3. 커스텀 UserDetails 클래스를 만들어 사용하는 방법 (추천)
         */
        // User customUserDetails = new User(memberEntity.getEmail(), memberEntity.getPassword(), List.of(authEntity.getRole()));
        CustomUserDetails customUserDetails = new CustomUserDetails(memberEntity);

// Authentication 객체 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(
                customUserDetails,
                null,
                customUserDetails.getAuthorities() // 기본 권한 ROLE_USER 반환
        );

// SecurityContextHolder에 인증 정보 설정
        // SecurityContext, PersistenceContext, XxxContext: Xxx
        SecurityContextHolder.getContext().setAuthentication(authToken);

// 다음 필터로 요청 전달
        filterChain.doFilter(request, response);

    }
}