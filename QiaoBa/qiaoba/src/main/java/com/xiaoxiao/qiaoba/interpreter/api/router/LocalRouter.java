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
import com.xiaoxiao.qiaoba.interpreter.api.service.RemoteRouterService;
import com.xiaoxiao.qiaoba.interpreter.utils.ProcessUtils;
import com.xiaoxiao.qiaoba.interpreter.utils.RouterUtils;
import com.xiaoxiao.qiaoba.protocol.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

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
    private Map<String, ActionCallback> mActionCallbackQueue;

    private LocalRouter(Context context){
        mContext = context;
        mDomain = ProcessUtils.getProcessName(context, ProcessUtils.getMyProcessId());
        mProviderList = new HashMap<>();
        mActionRequestQueue = new HashMap<>();
        mResponseCallbackQueue = new HashMap<>();
        mActionCallbackQueue = new HashMap<>();
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
        ActionRequest request = new ActionRequest.Builder()
                .originDomain(mDomain)
                .router(router)
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

    public void responseData(ActionResult result){
        if(result.getCode() == ActionResult.CODE_SUCCESS){
            Log.e("mytest", "调用远程api success");
        }else {
            Log.e("mytest", "调用远程api faile, code : " + result.getCode());
        }
    }


    /*
     * 从本地查询router， 并执行
     */
    public void resolveRouter(String uuid, String originDomain, String router, String jsonData){
        resolveRouter(uuid, originDomain, jsonData, RouterUtils.getDomainFromRouter(router), RouterUtils.getPathnameFromRouter(router),
                RouterUtils.getActionnameFromRouter(router));
    }

    public void resolveRouter(final String uuid, String originDomain, String jsonData,
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
                IAction action = provider.getAction(actionName);
                if(action == null){
                    result = new ActionResult.Builder()
                            .code(ActionResult.CODE_ACTION_NOT_FOUND)
                            .uuid(uuid)
                            .router(routerString)
                            .build();
                }else {
                    ActionCallback callback = new ActionCallback() {
                        @Override
                        public void success(String data, String jsonData) {
                            ActionResult successResult = new ActionResult.Builder()
                                    .code(ActionResult.CODE_SUCCESS)
                                    .uuid(uuid)
                                    .data(data)
                                    .jsonData(jsonData)
                                    .build();

                            responseRemoteData(successResult);
                        }
                    };

                    action.invoke(jsonData,callback); // 其实这个值不应该由invoke直接返回， 如果需要向调用返回对应的返回值，需要做处理
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
}
