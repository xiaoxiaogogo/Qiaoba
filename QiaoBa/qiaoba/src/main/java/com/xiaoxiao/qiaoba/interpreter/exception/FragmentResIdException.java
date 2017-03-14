package com.xiaoxiao.qiaoba.interpreter.exception;

/**
 * Created by wangfei on 2017/3/14.
 */

public class FragmentResIdException extends RuntimeException {
    public FragmentResIdException(){
        super("The resId show the fragment is wrong!!");
    }

    public FragmentResIdException(String error){
        super(error);
    }
}
