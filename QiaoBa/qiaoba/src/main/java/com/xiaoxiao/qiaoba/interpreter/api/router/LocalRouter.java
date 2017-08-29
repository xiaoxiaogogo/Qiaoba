package com.xiaoxiao.qiaoba.interpreter.api.router;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.xiaoxiao.qiaoba.interpreter.api.IRemoteRouterInterface;
import com.xiaoxiao.qiaoba.interpreter.api.action.IAction;
import com.xiaoxiao.qiaoba.interpreter.api.application.QiaobaApplication;
import com.xiaoxiao.qiaoba.interpreter.api.callback.ActionCallback;
import com.xiaoxiao.qiaoba.interpreter.api.callback.ResponseCallback;
import com.xiaoxiao.qiaoba.interpreter.api.provider.IProvider;
import com.xiaoxiao.qiaoba.interpreter.api.service.LocalRouterService;
import com.xiaoxiao.qiaoba.interpreter.api.service.RemoteRouterService;
import com.xiaoxiao.qiaoba.interpreter.utils.ProcessUtils;
import com.xiaoxiao.qiaoba.interpreter.utils.RouterUtils;
import com.xiaoxiao.qiaoba.protocol.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by wangfei on 2017/7/20.
 */

public class LocalRouter {

    /**
     * 对应进程名
     */
    private String mDomain;

    private Context mContext;

    private Map<String, IProvider> mProviderList;

    private Map<String, ActionRequest> mActionRequestQueue;
    private Map<String, ResponseCallback> mResponseCallbackQueue;

    // 增加一个公用的线程池（所有的LocalRouter都公用的线程池）
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors(); // 当前CPU的核数
    private static final int CORE_THREAD_COUTN = CPU_COUNT + 1;
    private static final int MAXIMUM_THREAD_COUNT = 2 * CPU_COUNT + 1;

    private static Executor THREAD_POOL = new ThreadPoolExecutor(CORE_THREAD_COUTN, MAXIMUM_THREAD_COUNT,
            1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(128));


    private LocalRouter(Context context){
        mContext = context;
        mDomain = ProcessUtils.getProcessName(context, ProcessUtils.getMyProcessId());
        mProviderList = new HashMap<>();
        mActionRequestQueue = new ConcurrentHashMap<>();
        mResponseCallbackQueue = new ConcurrentHashMap<>();
    }

    public String getDomain() {
        return this.mDomain;
    }

    private static LocalRouter mInstance;

    private ServiceConnection mRemoteServiceConnection;
    private IRemoteRouterInterface mRemoteRouterAIDL;

    // 是否支持， 为空重新初始化
    public static LocalRouter getInstance(){
        return mInstance;
    }

    public static void init(QiaobaApplication context){
        mInstance = new LocalRouter(context);
    }


    public void registeProvider(IProvider provider){
        if(provider != null){
            mProviderList.put(provider.getRouterPath(), provider);
        }else {
            throw new RuntimeException("can't registe a null as router provider!!!");
        }
    }

    /*
     * 调用远程router操作
     */
    public void connectRemoteService(final ActionRequest request){
        Intent intent = new Intent(mContext, RemoteRouterService.class);
        mContext.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mRemoteRouterAIDL = IRemoteRouterInterface.Stub.asInterface(service);
                mRemoteServiceConnection = this;
                try {
                    mRemoteRouterAIDL.responseConnect(request);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mRemoteRouterAIDL = null;
                mRemoteServiceConnection = null;
            }
        }, BIND_AUTO_CREATE);
    }


    public void invokeRouter(String router){
        this.invokeRouter(router, null);
    }

    /**
     * 调用远程的router（其他进程的操作）
     * @param router
     */
    public void invokeRouter(String router, ResponseCallback callback){
        this.invokeRouter(router, "", callback);
    }

    public void invokeRouter(String router, String jsonData, ResponseCallback callback) {
        this.invokeRouter(router, jsonData, ActionRequest.TYPE_CALL_NORMAL, callback);
    }

    public void invokeRouter(String router, String jsonData, int callType, ResponseCallback callback){
        callType = callback == null ? ActionRequest.TYPE_CALL_NO_CALLBACK : callType;
        ActionRequest request = new ActionRequest.Builder()
                .originDomain(mDomain)
                .router(router)
                .json(jsonData)
                .callType(callType)
                .build();
        mActionRequestQueue.put(request.getUuid(), request);
        if(callback != null) {
            mResponseCallbackQueue.put(request.getUuid(), callback);
        }

        if(mRemoteRouterAIDL != null){
            try {
                mRemoteRouterAIDL.callRouter(request);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else {
            connectRemoteService(request);
        }
    }

    public void responseInvoke(String uuid){
        ActionRequest request = mActionRequestQueue.get(uuid);
        if(request == null){
            // 可能取消， 或者出现异常；； 需要后面做处理 （一般情况下不应该出现）
            throw new RuntimeException("ActionRequest not found in Local response invoke!!!");
        }
        mActionRequestQueue.remove(uuid);
        if(mRemoteRouterAIDL != null){
            try {
                mRemoteRouterAIDL.callRouter(request);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else {
            // 可以考虑重新连接， 或者 抛出异常（如果重新连接， 需要记录次数， 设置最大重试次数）
        }
    }

    public boolean responseConnect(final String uuid){
        Intent intent = new Intent(mContext, RemoteRouterService.class);
        mContext.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mRemoteServiceConnection = this;
                mRemoteRouterAIDL = IRemoteRouterInterface.Stub.asInterface(service);
                try {
                    mRemoteRouterAIDL.responseCall(uuid);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mRemoteRouterAIDL = null;
                mRemoteServiceConnection = null;
            }
        }, BIND_AUTO_CREATE);
        return true;
    }

    public void responseData(final ActionResult result){
        // 默认是在 Binder的线程中运行， 此处将其置于子线程中执行，不占用Binder的系统线程
        THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                if(mResponseCallbackQueue.get(result.getUUID()) != null) {
                    if (result.getCode() == ActionResult.CODE_SUCCESS) {
                        mResponseCallbackQueue.get(result.getUUID()).onSuccess(result);
                    } else {
                        mResponseCallbackQueue.get(result.getUUID()).onError(result);
                    }
                    mResponseCallbackQueue.remove(result.getUUID());
                }
            }
        });

    }


    /*
     * 从本地查询router， 并执行
     */
    public void resolveRouter(String uuid, String originDomain, String router, String jsonData, int callType){
        resolveRouter(uuid, originDomain, jsonData, callType, RouterUtils.getDomainFromRouter(router), RouterUtils.getPathnameFromRouter(router),
                RouterUtils.getActionnameFromRouter(router));
    }

    public void resolveRouter(final String uuid, String originDomain, final String jsonData, final int callType,
                              String domain, String pathname, String actionName){
        //判断是在当前进程还是在 其他进程
        if(this.mDomain.equals(originDomain)){ // 表明是在在当前进程中调用

        }else if(StringUtils.isEmpty(uuid)){ // 返回错误，没有uuid（命令调用异常）

        }else {
            String routerString = RouterUtils.getRouterString(domain, pathname);
            IProvider provider = this.mProviderList.get(routerString);
            final ActionResult result;
            if(provider == null){
                result = new ActionResult.Builder()
                        .code(ActionResult.CODE_PROVIDER_NOT_FOUND)
                        .uuid(uuid)
                        .router(routerString)
                        .build();
            }else {
                final IAction action = provider.getAction(actionName);
                if(action == null){
                    result = new ActionResult.Builder()
                            .code(ActionResult.CODE_ACTION_NOT_FOUND)
                            .uuid(uuid)
                            .router(routerString)
                            .build();
                    responseRemoteData(result);
                }else {
                    THREAD_POOL.execute(new Runnable() {
                        @Override
                        public void run() {
                            String jsonStr = action.invoke(jsonData); // 其实这个值不应该由invoke直接返回， 如果需要向调用返回对应的返回值，需要做处理
                            ActionResult successResult = new ActionResult.Builder()
                                    .code(ActionResult.CODE_SUCCESS)
                                    .uuid(uuid)
                                    .jsonData(jsonStr)
                                    .build();

                            responseRemoteData(successResult);
                        }
                    });
                    if(callType == ActionRequest.TYPE_CALL_ONEWAY){
                        ActionResult successResult = new ActionResult.Builder()
                                .code(ActionResult.CODE_SUCCESS)
                                .uuid(uuid)
                                .build();

                        responseRemoteData(successResult);
                    }
                }

            }
        }
    }

    private void responseRemoteData(ActionResult result){
        if(mRemoteRouterAIDL != null){
            try {
                mRemoteRouterAIDL.responseData(result.getUUID(), result);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else {
            // 和远程服务连接中断， 需要重新建立连接，然后返回对应的返回值

        }
    }

    /**
     * 断开和远程的连接
     */
    public void disconnectRemote() {
        if(mRemoteServiceConnection == null){
            return;
        }
        mContext.unbindService(mRemoteServiceConnection);
        mRemoteRouterAIDL = null;
    }

    public void stopRemoteService(){
        if(checkRemoteConnected()){
            try {
                mRemoteRouterAIDL.disconnectLocalService(RemoteRouter.PROCESS_NAME);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else {
            Log.e("QiaoBa", "The local router not connect remote service, so it can't stop remote service.");
        }
    }

    public boolean checkRemoteConnected(){
        if(mRemoteRouterAIDL != null){
            return  true;
        }
        return false;
    }

    public boolean stopSelf(Class<? extends LocalRouterService> clazz){
        if(checkRemoteConnected()){
            try {
                return mRemoteRouterAIDL.disconnectLocalService(mDomain);
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
