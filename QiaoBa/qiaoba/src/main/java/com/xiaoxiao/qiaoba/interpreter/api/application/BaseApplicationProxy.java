package com.xiaoxiao.qiaoba.interpreter.api.application;

import android.app.Application;

/**
 * Created by wangfei on 2017/8/3.
 */

public abstract class BaseApplicationProxy {

    private QiaobaApplication mApplicationInstance;

    public void setApplication(QiaobaApplication instance){
        this.mApplicationInstance = instance;
    }

    public QiaobaApplication getApplication(){
        return this.mApplicationInstance;
    }

    public void onCreate(){

    }

    public void onTerminate(){

    }

    public void onLowMemory(){

    }

    public void onTrimMemory(int level){

    }

    public void registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callbacks){

    }

    public void unregisterActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callbacks){

    }
}
