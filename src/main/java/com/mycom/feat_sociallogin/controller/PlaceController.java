package com.mycom.feat_sociallogin.controller;

import com.mycom.feat_sociallogin.dto.CustomUserDetails;
import com.mycom.feat_sociallogin.dto.PlaceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    // 인증이 필요한 API
    @GetMapping("/places/{placeId}")
    public ResponseEntity<PlaceResponse> getPlaceBy(
            @PathVariable Long placeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        /*
         * FIXME:
         *  인증이 필요한 부분은 모두 아래 주석 로직이 있는 문제 -> AOP + 커스텀 애너테이션으로 개선 (스프링 시큐리티를 사용하지 않는 프로젝트인 경우)
         *  스프링 시큐리티를 사용한다면 AuthenticationPrincipal 애너테이션 사용해서 UserDetails 객체를 받아올 수 있음
         */
        // if (!userDetails.isEnabled()) {
        //     throw new RuntimeException("사용자 정보가 비활성화 되었습니다.");
        // }

        placeService.search(placeId);

        // TODO: place 페이지에 제공할 데이터는 PlaceResponse VO(Value Object) 를 구성해서 반환
        return ResponseEntity.ok(new PlaceResponse(placeId));
    }

}
