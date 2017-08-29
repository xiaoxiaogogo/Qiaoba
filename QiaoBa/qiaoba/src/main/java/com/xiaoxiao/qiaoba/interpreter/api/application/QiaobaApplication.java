package com.xiaoxiao.qiaoba.interpreter.api.application;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.xiaoxiao.qiaoba.interpreter.api.router.LocalRouter;
import com.xiaoxiao.qiaoba.interpreter.api.service.RemoteRouterService;
import com.xiaoxiao.qiaoba.interpreter.utils.ProcessUtils;
import com.xiaoxiao.qiaoba.protocol.utils.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangfei on 2017/8/3.
 */

public abstract class QiaobaApplication extends Application {

    private ArrayList<BaseApplicationProxy> mApplicationProxys = new ArrayList<>();

    private Map<String, Class<? extends BaseApplicationProxy>> mApplicationProxyMap = new HashMap<>();

    private BaseApplicationProxy mApplicationProxyInstance;

    private static QiaobaApplication mInstance;


    public static QiaobaApplication getInstance(){
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 注意： 这个是每个进程都会执行到的
        mInstance = this;

        // 初始化LocalRouter
        initRouter();
        // 如果支持多进程， 启动RemoteRouterService, 并创建注册 RemoteApplicationProxy
        startRemoteService();

        // 注意： 注册的操作每个进程都会执行，下面的dispatch操作会找到当前进程的ApplicatioProxy进行初始化
        // 注册Application的代理类
        initApplicationProxys();

        // 筛选出当前进程的ApplicationProxy， 创建其实例，并
        // 回调对应进程的 Application的代理类
        dispatchLocalProxy();
    }


    protected abstract boolean needMuliteProcess();

    protected abstract void initApplicationProxys();

    public abstract void initLocalRouterServices();

    private void dispatchLocalProxy(){
        String processName = ProcessUtils.getProcessName(getApplicationContext(), ProcessUtils.getMyProcessId());
        Class applicationProxyClazz = mApplicationProxyMap.get(processName);
        if(applicationProxyClazz == null){
            return;
        }
        Log.e("mytest", applicationProxyClazz.getCanonicalName());
        try {
            Object proxyInstance = applicationProxyClazz.newInstance();
//            Method methodClazz = applicationProxyClazz.getDeclaredMethod("setApplication", QiaobaApplication.class);
//            methodClazz.setAccessible(true);
//            methodClazz.invoke(proxyInstance, this);
            mApplicationProxyInstance = (BaseApplicationProxy) proxyInstance;
            mApplicationProxyInstance.setApplication(this);
            // 回调onCreate()生命周期回调
            mApplicationProxyInstance.onCreate();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    protected void registApplicationProxy(String domain, Class<? extends BaseApplicationProxy> applicationProxyClazz){
        this.mApplicationProxyMap.put(domain, applicationProxyClazz);
    }


    private void startRemoteService(){
        String processName = ProcessUtils.getProcessName(this, ProcessUtils.getMyProcessId());
        if(needMuliteProcess()){ // 只要只有在 RemoteApplicationProxy中才会注册此Proxy，然后执行
            mApplicationProxyMap.put("remote", RemoteApplicationProxy.class);
        }
        if(needMuliteProcess() && !"remote".equals(processName)){
            Intent intent = new Intent(getApplicationContext(), RemoteRouterService.class);
            startService(intent); // 开启远程服务，也是启动进程
        }
    }

    private void initRouter(){
        LocalRouter.init(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mApplicationProxyInstance.onTerminate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mApplicationProxyInstance.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        mApplicationProxyInstance.onTrimMemory(level);
    }

    @Override
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        super.registerActivityLifecycleCallbacks(callback);
        mApplicationProxyInstance.registerActivityLifecycleCallbacks(callback);
    }

    @Override
    public void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        super.unregisterActivityLifecycleCallbacks(callback);
        mApplicationProxyInstance.unregisterActivityLifecycleCallbacks(callback);
    }
}
