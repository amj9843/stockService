package com.zerobase.stockservice.service;

import com.zerobase.stockservice.domain.Member;
import com.zerobase.stockservice.dto.Auth;
import com.zerobase.stockservice.exception.impl.AlreadyExistUserException;
import com.zerobase.stockservice.exception.impl.IncorrectPasswordException;
import com.zerobase.stockservice.exception.impl.NoUserException;
import com.zerobase.stockservice.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.memberRepository.findByUsername(username)
                .orElseThrow(() -> new NoUserException());
    }

    @Transactional
    public Member register(Auth.SignUp member) {
        boolean exists = this.memberRepository.existsByUsername(member.getUsername());
        if (exists) {
            throw new AlreadyExistUserException();
        }

        member.setPassword(this.passwordEncoder.encode(member.getPassword()));
        return this.memberRepository.save(member.toEntity());
    }

    public Member authenticate(Auth.SignIn member) {
        Member user = this.memberRepository.findByUsername(member.getUsername())
                .orElseThrow(() -> new NoUserException());

        if (!this.passwordEncoder.matches(member.getPassword(), user.getPassword())) {
            throw new IncorrectPasswordException();
        }

        return user;
    }
}
