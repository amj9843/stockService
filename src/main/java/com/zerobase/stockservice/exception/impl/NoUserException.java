package com.zerobase.stockservice.exception.impl;

import com.zerobase.stockservice.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class NoUserException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "존재하지 않는 유저입니다.";
    }
}
