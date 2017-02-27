package com.xiaoxiao.qiaoba.interpreter.exception;

/**
 * Created by wangfei on 2017/2/24.
 */

public class ProviderStubClassNotFoundException extends RuntimeException {
    public ProviderStubClassNotFoundException(){
        super("provider stub class not found！please check caller annotation's value is same as the provider annotation's value!");
    }
    public ProviderStubClassNotFoundException(String message){
        super(message);
    }
}
