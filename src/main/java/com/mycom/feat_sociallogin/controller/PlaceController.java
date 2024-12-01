package com.mycom.feat_sociallogin.controller;

import com.mycom.feat_sociallogin.dto.PlaceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlaceController {

    @GetMapping("/places/{placeId}")
    public ResponseEntity<PlaceResponse> getPlaceBy(@PathVariable Long placeId) {
        return ResponseEntity.ok(new PlaceResponse(placeId));
    }

}
