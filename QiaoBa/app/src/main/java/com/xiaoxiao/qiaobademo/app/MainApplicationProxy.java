package com.xiaoxiao.qiaobademo.app;

import android.util.Log;

import com.gavin.secondmodule.api.ApiRouterFactory;
import com.xiaoxiao.qiaoba.interpreter.Qiaoba;
import com.xiaoxiao.qiaoba.interpreter.api.application.BaseApplicationProxy;
import com.xiaoxiao.qiaoba.interpreter.utils.ProcessUtils;

/**
 * Created by wangfei on 2017/8/4.
 */

public class MainApplicationProxy extends BaseApplicationProxy {

    @Override
    public void onCreate() {
        super.onCreate();
        Qiaoba.init(getApplication());
        new ApiRouterFactory().init();

        Log.e("mytest", "process name :" + ProcessUtils.getProcessName(getApplication(), ProcessUtils.getMyProcessId()));
    }
}
