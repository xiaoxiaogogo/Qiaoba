package com.xiaoxiao.qiaoba.protocol.exception;

/**
 * Created by wangfei on 2017/1/11.
 */

public class CallerNotMatchProviderException extends RuntimeException {

    public CallerNotMatchProviderException(){
        super("Caller's value can't find it's match Provider");
    }
    public CallerNotMatchProviderException(String message){
        super("Caller's value can't find it's match Provider. " + message);
    }
}
