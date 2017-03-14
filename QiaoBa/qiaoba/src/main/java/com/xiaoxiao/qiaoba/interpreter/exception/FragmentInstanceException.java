package com.xiaoxiao.qiaoba.interpreter.exception;

/**
 * Created by wangfei on 2017/3/14.
 */

public class FragmentInstanceException extends RuntimeException {

    public FragmentInstanceException(){
        super("Create Fragment Instance fail!");
    }
    public FragmentInstanceException(String error){
        super(error);
    }
}
