package com.xiaoxiao.qiaoba.interpreter.api.router;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.xiaoxiao.qiaoba.interpreter.api.ILocalRouterInterface;
import com.xiaoxiao.qiaoba.interpreter.api.service.LocalRouterService;
import com.xiaoxiao.qiaoba.interpreter.utils.RouterUtils;
import com.xiaoxiao.qiaoba.protocol.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangfei on 2017/7/23.
 */

public class RemoteRouter {

    private Map<String, Class<? extends LocalRouterService>> mLocalServiceMap;
    private Map<String, ServiceConnection> mLocalServiceConnectionMap;
    private Map<String, ILocalRouterInterface> mLocalServiceAIDLMap;
    private Map<String, ActionRequest> mActionRequestQueue;
    private static RemoteRouter mInstance;

    private RemoteRouter(){
        mLocalServiceMap = new HashMap<>();
        mLocalServiceConnectionMap = new HashMap<>();
        mLocalServiceAIDLMap = new HashMap<>();
        mActionRequestQueue = new HashMap<>();
    }

    private static class Holder{
        public static RemoteRouter mRemoteRouter = new RemoteRouter();
    }

    public static RemoteRouter getInstance(){
        if(mInstance == null){
            mInstance = Holder.mRemoteRouter;
        }
        return mInstance;
    }

    /**
     * 注册 localService（和指定进程通信的service）
     * @param domain ： 进程名
     * @param localService
     */
    public void regisitLocalService(String domain, Class<? extends LocalRouterService> localService){
        mLocalServiceMap.put(domain, localService);
    }

    public boolean responseConnect(final ActionRequest request, Context context){
        if(request == null){
            // 可以发送error response
            return false;
        }
        mActionRequestQueue.put(request.getUuid(), request);
        final String domain = request.getOriginDomain();
        if(mLocalServiceMap.get(domain) == null){
            // 抛出异常： 没有对应的 router，请检查
            return  false;
        }else {
            ServiceConnection connection;
            connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    ILocalRouterInterface localRouterInterface = ILocalRouterInterface.Stub.asInterface(service);
                    mLocalServiceAIDLMap.put(domain, localRouterInterface);
                    mLocalServiceConnectionMap.put(domain, this);
                    try {
                        localRouterInterface.responseInvoke(request.getUuid());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mLocalServiceConnectionMap.remove(domain);
                    mLocalServiceAIDLMap.remove(domain);
                }
            };
            Class<? extends LocalRouterService> localServiceClazz = mLocalServiceMap.get(domain);
            Intent intent = new Intent(context, localServiceClazz);
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
            return true;
        }
    }

    public boolean connectRouter(final String domain, final String uuid, final String router, Context context){
        if(mLocalServiceMap.get(domain) == null){
            // 抛出异常： 没有对应的 router，请检查
            return  false;
        }else {
            ServiceConnection connection;
            connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    ILocalRouterInterface localRouterInterface = ILocalRouterInterface.Stub.asInterface(service);
                    mLocalServiceAIDLMap.put(domain, localRouterInterface);
                    mLocalServiceConnectionMap.put(domain, this);
                    try {
                        localRouterInterface.responseConnect(uuid);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        // local router 连接RemoteRouter失败
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mLocalServiceConnectionMap.remove(domain);
                    mLocalServiceAIDLMap.remove(domain);
                }
            };
            Class<? extends LocalRouterService> localServiceClazz = mLocalServiceMap.get(domain);
            Intent intent = new Intent(context, localServiceClazz);
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
            return true;
        }
    }

    public void callRouter(ActionRequest request, final Context context){
        if(request == null){
            // 直接返回错误
            return;
        }
        mActionRequestQueue.put(request.getUuid(), request);

        // request 中 router， originDomain都不能没有，需要做非空判断

        // 1. 从router中解析出 对应的 domain
        final String domain = RouterUtils.getDomainFromRouter(request.getRouter());
        if(StringUtils.isEmpty(domain)){
            // 没有找到对应的进程, router不是标准router

            return;
        }
        // 2. 通过domain，查找对应的 service，发送call命令
        if(mLocalServiceAIDLMap.get(domain) != null){
            try {
                mLocalServiceAIDLMap.get(domain).resolveRouter(request.getUuid(), request.getOriginDomain(), request.getRouter(), request.getJsonData());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else {
            // 启动要调用的local service， 然后再执行此方法
            connectRouter(domain, request.getUuid(), request.getRouter(), context);
        }
    }

    public void responseCall(String uuid){
        ActionRequest request = mActionRequestQueue.get(uuid);
        if(request == null){
            // 直接返回 response error
            return;
        }
        String domain = RouterUtils.getDomainFromRouter(request.getRouter());
        if(mLocalServiceAIDLMap.get(domain) != null){
            try {
                mLocalServiceAIDLMap.get(domain).resolveRouter(uuid, request.getOriginDomain(), request.getRouter(), request.getJsonData());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else {
            // 重新链接， 设置最大重试次数
        }
    }

    public void responseData(String uuid, ActionResult result){
        ActionRequest actionRequest = mActionRequestQueue.get(uuid);
        if(actionRequest == null){
            // 抛出异常 或者不做处理
        }else {
            ILocalRouterInterface responseAIDL = mLocalServiceAIDLMap.get(actionRequest.getOriginDomain());
            if(responseAIDL != null){
                try {
                    responseAIDL.responseData(result);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }else {
                // 连接断开了， 需要重新建立连接； 然后再返回数据
            }
        }
    }

}
