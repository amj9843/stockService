package com.zerobase.stockservice.exception.impl;

import com.zerobase.stockservice.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class IncorrectPasswordException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "입력한 비밀번호와 등록된 비밀번호가 다릅니다.";
    }
}
