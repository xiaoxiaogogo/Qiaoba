package com.xiaoxiao.qiaoba.interpreter.exception;

/**
 * Created by wangfei on 2017/3/2.
 */

public class HandlerException extends RuntimeException {

    public HandlerException(){
        super("You have error handle!");
    }
    public HandlerException(String error){
        super(error);
    }
}
