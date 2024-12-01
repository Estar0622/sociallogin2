package com.mycom.feat_sociallogin.controller;

import com.mycom.feat_sociallogin.repository.RefreshRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoutController {

    private final RefreshRepository refreshRepository;

    public LogoutController(RefreshRepository refreshRepository) {
        this.refreshRepository = refreshRepository;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // Get refresh token from cookies
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh")) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        // If no refresh token, return bad request
        if (refreshToken == null) {
            return new ResponseEntity<>("Refresh token not found in cookies", HttpStatus.BAD_REQUEST);
        }

        // Check if refresh token exists in DB
        boolean isTokenValid = refreshRepository.existsByRefresh(refreshToken);
        if (!isTokenValid) {
            return new ResponseEntity<>("Invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        // Delete refresh token from DB
        refreshRepository.deleteByRefresh(refreshToken);

        // Clear refresh token from cookies
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
    }
}
