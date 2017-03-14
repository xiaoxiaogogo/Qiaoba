package com.xiaoxiao.qiaoba.interpreter.exception;

/**
 * Created by wangfei on 2017/3/14.
 */

public class FragmentLinkParamNullPointException extends RuntimeException {
    public FragmentLinkParamNullPointException(){
        super("The fragment must have parametar is null!!");
    }

    public FragmentLinkParamNullPointException(String error){
        super(error);
    }
}
