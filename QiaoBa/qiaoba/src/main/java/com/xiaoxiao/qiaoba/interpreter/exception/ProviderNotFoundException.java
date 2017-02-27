package com.xiaoxiao.qiaoba.interpreter.exception;

/**
 * Created by wangfei on 2017/2/27.
 */

public class ProviderNotFoundException extends RuntimeException {

    public ProviderNotFoundException(){
        super("provider real class can't be found!");
    }

    public ProviderNotFoundException(String error){
        super(error);
    }

}
