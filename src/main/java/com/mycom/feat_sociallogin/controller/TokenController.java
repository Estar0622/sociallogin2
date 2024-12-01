package com.mycom.feat_sociallogin.controller;

import com.mycom.feat_sociallogin.service.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/token")
public class TokenController {

    private final JWTUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);

    public TokenController(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateAccessToken(@RequestHeader("Authorization") String authorizationHeader) {
        logger.info("Access Token Validation API called");

        try {
            // Bearer Token에서 Access Token 추출
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                logger.warn("Invalid Authorization header format");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Authorization header format");
            }
            String accessToken = authorizationHeader.substring(7);

            logger.info("Access Token received: {}", accessToken);

            // Access Token 유효성 확인
            if (jwtUtil.isExpired(accessToken)) {
                logger.warn("Access token expired");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access token expired");
            }

            // Access Token에서 정보 추출
            String username = jwtUtil.getUsername(accessToken);
            String role = jwtUtil.getRole(accessToken);

            logger.info("Access Token valid for user: {}, role: {}", username, role);

            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("role", role);
            response.put("message", "Access token is valid");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error validating Access Token", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Access token");
        }
    }
}

