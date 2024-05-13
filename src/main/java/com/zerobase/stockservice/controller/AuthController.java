package com.zerobase.stockservice.controller;

import com.zerobase.stockservice.domain.Member;
import com.zerobase.stockservice.dto.Auth;
import com.zerobase.stockservice.security.TokenProvider;
import com.zerobase.stockservice.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    //회원가입 API
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Auth.SignUp request) {
        Member result = this.memberService.register(request);
        return ResponseEntity.ok(result);
    }

    //로그인 API
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request) {
        Member member = this.memberService.authenticate(request);
        String token = this.tokenProvider.generateToken(member.getUsername(), member.getRoles());

        return ResponseEntity.ok(token);
    }
}
