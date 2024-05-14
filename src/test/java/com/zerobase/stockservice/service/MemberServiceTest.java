package com.zerobase.stockservice.service;

import com.zerobase.stockservice.domain.Member;
import com.zerobase.stockservice.dto.Auth;
import com.zerobase.stockservice.exception.impl.AlreadyExistUserException;
import com.zerobase.stockservice.exception.impl.IncorrectPasswordException;
import com.zerobase.stockservice.exception.impl.NoUserException;
import com.zerobase.stockservice.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {
    @InjectMocks
    private MemberService memberService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MemberRepository memberRepository;

    @DisplayName("사용자 조회")
    @Test
    void loadUserByUsername() {
        String username = "username";
        String password = "password";
        List<String> roles = List.of("ROLE_READ");
        Member member = new Member(username, password, roles);

        //given
        given(memberRepository.findByUsername(anyString()))
                .willReturn(Optional.of(member));

        //when
        UserDetails userDetails = memberService.loadUserByUsername(username);

        //then
        assertThat(userDetails.getUsername()).isEqualTo(username);
        assertThat(userDetails.getPassword()).isEqualTo(password);
    }

    @DisplayName("사용자 조회 실패")
    @Test
    void failedLoadUserByUsername() {
        String username = "username";
        String password = "password";
        List<String> roles = List.of("ROLE_READ");
        Member member = new Member(username, password, roles);

        //given
        given(memberRepository.findByUsername(anyString()))
                .willThrow(new NoUserException());

        //when
        assertThatThrownBy(() -> memberService.loadUserByUsername(username))
                .isInstanceOf(NoUserException.class);

        //then
    }

    @DisplayName("회원가입")
    @Test
    void register() {
        String username = "username";
        String password = "password";
        List<String> roles = List.of("ROLE_READ");

        Auth.SignUp signUp = new Auth.SignUp();
        signUp.setUsername(username);
        signUp.setPassword(password);
        signUp.setRoles(roles);

        Member member = new Member(username, password, roles);

        //given
        given(memberRepository.existsByUsername(anyString()))
                .willReturn(false);
        given(passwordEncoder.encode(anyString()))
                .willReturn("encodePassword");
        given(memberRepository.save(any())).willReturn(member);

        //when
        Member registeredMember = memberService.register(signUp);

        //then
        assertThat(registeredMember.getUsername()).isEqualTo(username);
        assertThat(registeredMember.getPassword()).isEqualTo(password);
        assertThat(registeredMember.getRoles().contains("ROLE_READ")).isTrue();
    }

    @DisplayName("회원가입 실패")
    @Test
    void failedRegister() {
        String username = "username";
        String password = "password";
        List<String> roles = List.of("ROLE_READ");

        Auth.SignUp signUp = new Auth.SignUp();
        signUp.setUsername(username);
        signUp.setPassword(password);
        signUp.setRoles(roles);

        //given
        given(memberRepository.existsByUsername(anyString()))
                .willReturn(true);

        //when
        assertThatThrownBy(() -> memberService.register(signUp))
                .isInstanceOf(AlreadyExistUserException.class);

        //then
    }

    @DisplayName("로그인")
    @Test
    void authenticate() {
        String username = "username";
        String password = "password";
        List<String> roles = List.of("ROLE_READ");

        Auth.SignIn signIn = new Auth.SignIn();
        signIn.setUsername(username);
        signIn.setPassword(password);

        Member member = new Member(username, password, roles);

        //given
        given(memberRepository.findByUsername(anyString()))
                .willReturn(Optional.of(member));
        given(passwordEncoder.matches(anyString(), anyString()))
                .willReturn(true);

        //when
        Member loginMember = memberService.authenticate(signIn);

        //then
        assertThat(loginMember.getUsername()).isEqualTo(username);
        assertThat(loginMember.getPassword()).isEqualTo(password);
    }

    @DisplayName("회원 미존재로 로그인 실패")
    @Test
    void failedAuthenticate1() {
        String username = "username";
        String password = "password";
        List<String> roles = List.of("ROLE_READ");

        Auth.SignIn signIn = new Auth.SignIn();
        signIn.setUsername(username);
        signIn.setPassword(password);

        Member member = new Member(username, password, roles);

        //given
        given(memberRepository.findByUsername(anyString()))
                .willThrow(new NoUserException());

        //when
        assertThatThrownBy(() -> memberService.authenticate(signIn))
                .isInstanceOf(NoUserException.class);

        //then
    }

    @DisplayName("비밀번호 불일치로 로그인 실패")
    @Test
    void failedAuthenticate2() {
        String username = "username";
        String password = "password";
        List<String> roles = List.of("ROLE_READ");

        Auth.SignIn signIn = new Auth.SignIn();
        signIn.setUsername(username);
        signIn.setPassword(password);

        Member member = new Member(username, password, roles);

        //given
        given(memberRepository.findByUsername(anyString()))
                .willReturn(Optional.of(member));
        given(passwordEncoder.matches(anyString(), anyString()))
                .willReturn(false);

        //when
        assertThatThrownBy(() -> memberService.authenticate(signIn))
                .isInstanceOf(IncorrectPasswordException.class);

        //then
    }
}
