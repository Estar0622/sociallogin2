package com.mycom.feat_sociallogin.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * TODO:
 *  인증 - 입장권 (인증 실패 시 HTTP status code 401), 인가 - 권한 보유 여부 확인 (권한 미보유 시 HTTP status code 403)
 *  cf. (서블릿) 필터와 (스프링) 인터셉터
 */
@Slf4j
@Component
public class AuthorizationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("Authorization interceptor URL: {} ", request.getRequestURL());

        String accessToken = request.getHeader("Authorization");
        if (Objects.isNull(accessToken)) {
            throw new IllegalStateException("인증이 필요합니다.");
        }

        // TODO: TokenController 의 validateAccessToken 메서드 로직을 여기에 추가해서 활용 가능
        // (...)

        return true;
    }

}
