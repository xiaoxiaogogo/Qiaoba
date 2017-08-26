package com.xiaoxiao.qiaoba.interpreter.api.exception;

/**
 * Created by wangfei on 2017/8/9.
 */

public class LocalRouterServiceNotRegisted extends RuntimeException {

    public LocalRouterServiceNotRegisted(String originDomain) {
        super(originDomain + " process's LocalRouterService isn't registed in Remote");
    }
}
