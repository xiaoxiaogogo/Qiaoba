package com.xiaoxiao.qiaoba.interpreter.exception;

/**
 * Created by wangfei on 2017/1/16.
 */

public class ProviderMethodNotFoundException extends RuntimeException {

    public ProviderMethodNotFoundException(){
        super("provider's method not found.");
    }

    public ProviderMethodNotFoundException(String message){
        super("provider's method not found. " +message);
    }
}
