package com.xiaoxiao.qiaoba.protocol.utils;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

/**
 * Created by wangfei on 2017/1/11.
 */

public class Logger {

    private Messager mMsg;

    public Logger(Messager messager){
        mMsg = messager;
    }

    public void info(CharSequence info){
        if(StringUtils.isNotEmpty(info)){
            mMsg.printMessage(Diagnostic.Kind.NOTE, Constant.PREFIX_OF_LOAGGER + info);
        }
    }

    public void error(CharSequence error){
        if(StringUtils.isNotEmpty(error)){
            mMsg.printMessage(Diagnostic.Kind.ERROR, Constant.PREFIX_OF_LOAGGER + "[" +error + "]");
        }
    }

    public void error(Throwable error){
        if(error != null){
            mMsg.printMessage(Diagnostic.Kind.ERROR, Constant.PREFIX_OF_LOAGGER + "An exception is encountered, ["
                + error.getMessage() + "]" + "\n" + formatStackTrace(error.getStackTrace()));
        }
    }


    public void warning(CharSequence warning){
        if(StringUtils.isNotEmpty(warning)){
            mMsg.printMessage(Diagnostic.Kind.WARNING, Constant.PREFIX_OF_LOAGGER + warning);
        }
    }

    /**
     * 获取错误方法栈的信息
     * @param stackTrace
     * @return
     */
    private String formatStackTrace(StackTraceElement[] stackTrace) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : stackTrace){
            sb.append("   at ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }


}
