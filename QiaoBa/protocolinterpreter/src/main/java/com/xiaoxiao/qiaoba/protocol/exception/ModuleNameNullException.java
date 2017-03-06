package com.xiaoxiao.qiaoba.protocol.exception;

/**
 * Created by wangfei on 2017/3/1.
 */

public class ModuleNameNullException extends RuntimeException {
    public ModuleNameNullException(){
        super("module name can't be null, please check set apt module name!");
    }
}
