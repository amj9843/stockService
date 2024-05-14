package com.zerobase.stockservice.exception;

public abstract class AbstractException extends RuntimeException{
    abstract public int getStatusCode();
    abstract public String getMessage();
}
