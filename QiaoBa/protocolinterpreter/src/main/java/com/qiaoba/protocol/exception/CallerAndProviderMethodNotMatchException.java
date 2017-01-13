package com.qiaoba.protocol.exception;

/**
 * Created by wangfei on 2017/1/11.
 */

public class CallerAndProviderMethodNotMatchException extends RuntimeException {
    public CallerAndProviderMethodNotMatchException(){
        super("Caller's method can't match Provider's method.");
    }

    public CallerAndProviderMethodNotMatchException(String message){
        super("method not match : ");
    }
}
