package com.xiaoxiao.qiaoba.interpreter.api.application;

/**
 * Created by wangfei on 2017/8/5.
 */

public class RemoteApplicationProxy extends BaseApplicationProxy {

    @Override
    public void onCreate() {
        super.onCreate();
        getApplication().initLocalRouterServices();
    }
}
