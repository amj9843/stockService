package com.zerobase.stockservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.stockservice.domain.Member;
import com.zerobase.stockservice.dto.Auth;
import com.zerobase.stockservice.security.TokenProvider;
import com.zerobase.stockservice.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private MemberService memberService;
    @MockBean
    private TokenProvider tokenProvider;
    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("회원가입")
    @WithMockUser
    @Test
    void signup() throws Exception {
        String username = "username";
        String password = "password";
        List<String> roles = List.of("ROLE_READ");

        Auth.SignUp signUp = new Auth.SignUp();
        signUp.setUsername(username);
        signUp.setPassword(password);
        signUp.setRoles(roles);

        Member member = new Member(username, password, roles);

        //given
        given(memberService.register(any())).willReturn(member);

        //when
        mvc.perform(
                        post("/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signUp))
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.password").value(password));

        //then
    }

    @DisplayName("로그인")
    @WithMockUser
    @Test
    void signin() throws Exception {
        String username = "username";
        String password = "password";
        List<String> roles = List.of("ROLE_READ");

        Auth.SignIn signIn = new Auth.SignIn();
        signIn.setUsername(username);
        signIn.setPassword(password);

        Member member = new Member(username, password, roles);

        //given
        given(memberService.authenticate(any())).willReturn(member);
        given(tokenProvider.generateToken(anyString(), anyList())).willReturn("token");

        //when
        mvc.perform(
                        post("/auth/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signIn))
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("token"));

        //then
    }
}