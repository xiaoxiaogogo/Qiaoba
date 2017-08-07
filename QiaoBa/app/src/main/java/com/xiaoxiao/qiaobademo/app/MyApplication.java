package com.xiaoxiao.qiaobademo.app;

import android.app.Application;
import android.util.Log;

import com.gavin.secondmodule.api.ApiRouterFactory;
import com.gavin.secondmodule.app.SecondApplicationProxy;
import com.gavin.secondmodule.service.SecondLocalRouterService;
import com.xiaoxiao.qiaoba.interpreter.Qiaoba;
import com.xiaoxiao.qiaoba.interpreter.api.application.QiaobaApplication;
import com.xiaoxiao.qiaoba.interpreter.api.router.RemoteRouter;

/**
 * Created by wangfei on 2016/12/21.
 */

public class MyApplication extends QiaobaApplication {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected boolean needMuliteProcess() {
        return true;
    }

    @Override
    protected void initApplicationProxys() {
        this.registApplicationProxy("main", MainApplicationProxy.class);
        this.registApplicationProxy("second", SecondApplicationProxy.class);
    }

    @Override
    public void initLocalRouterServices() {
        RemoteRouter.getInstance().regisitLocalService("main", MainLocalRouterService.class);
        RemoteRouter.getInstance().regisitLocalService("second", SecondLocalRouterService.class);
        Log.e("mytest", "init local router servie");
    }
}
