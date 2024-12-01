package com.mycom.feat_sociallogin.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@WebMvcTest(controllers = PlaceController.class, useDefaultFilters = false)
@Import(PlaceController.class)
class PlaceControllerTest {

    @Autowired
    MockMvc mockMvc;

    Authentication authentication;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        this.mockMvc = webAppContextSetup(context).alwaysDo(print()).build();
        this.authentication = new TestingAuthenticationToken("dkkim@gmail.com", null, "ROLE_USER");
    }

    @DisplayName("특정 장소를 조회한다.")
    @Test
    void getPlaceBy() throws Exception {
        // Given
        Long placeId = 1L;

        // When
        mockMvc.perform(get("/places/{placeId}", placeId)
                        .contentType("application/json")
                        .accept("application/json")
                        .characterEncoding("UTF-8")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placeId").value(placeId));

        // Then
        assertThat(placeId).isEqualTo(1L);
    }

}