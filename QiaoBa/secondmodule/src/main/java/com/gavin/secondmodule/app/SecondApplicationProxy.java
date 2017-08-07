package com.gavin.secondmodule.app;

import android.util.Log;

import com.gavin.secondmodule.api.ApiRouterFactory;
import com.gavin.secondmodule.api.SecondProvider;
import com.gavin.secondmodule.service.SecondLocalRouterService;
import com.xiaoxiao.qiaoba.interpreter.api.application.BaseApplicationProxy;
import com.xiaoxiao.qiaoba.interpreter.api.router.LocalRouter;
import com.xiaoxiao.qiaoba.interpreter.utils.ProcessUtils;

/**
 * Created by wangfei on 2017/8/4.
 */

public class SecondApplicationProxy extends BaseApplicationProxy {

    private static SecondApplicationProxy mInstance;

    public static SecondApplicationProxy getInstance(){
        return mInstance;
    };


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("mytest", "process name : " + ProcessUtils.getProcessName(getApplication(), ProcessUtils.getMyProcessId()));
        initLocalRouterProvider();
        mInstance = this;
    }

    private void initLocalRouterProvider() {
        ApiRouterFactory.init();
    }
}
