package com.xiaoxiao.qiaoba.interpreter.exception;

/**
 * Created by wangfei on 2017/1/25.
 */

public class RouterUriException extends RuntimeException {
    public RouterUriException(){
        super("Maybe the router uri is wrong or not use annotation RouterUri, please check!");
    }
    public RouterUriException(String message){
        super(message);
    }
}
