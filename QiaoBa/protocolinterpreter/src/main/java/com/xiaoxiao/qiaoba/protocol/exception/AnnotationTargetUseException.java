package com.xiaoxiao.qiaoba.protocol.exception;

/**
 * Created by wangfei on 2017/1/11.
 */

public class AnnotationTargetUseException extends RuntimeException {

    public AnnotationTargetUseException(String message){
        super("Annotation use error : " + message);
    }
}
