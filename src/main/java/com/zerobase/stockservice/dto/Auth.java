package com.zerobase.stockservice.dto;

import com.zerobase.stockservice.domain.Member;
import lombok.Data;

import java.util.List;

public class Auth {
    @Data
    public static class SignIn {
        private String username;
        private String password;
    }

    @Data
    public static class SignUp {
        private String username;
        private String password;
        private List<String> roles;

        public Member toEntity(){
            return new Member(this.username, this.password, this.roles);
        }
    }
}
