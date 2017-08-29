package com.xiaoxiao.qiaoba.interpreter.api.router;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.xiaoxiao.qiaoba.interpreter.api.ILocalRouterInterface;
import com.xiaoxiao.qiaoba.interpreter.api.application.QiaobaApplication;
import com.xiaoxiao.qiaoba.interpreter.api.exception.LocalRouterServiceNotRegisted;
import com.xiaoxiao.qiaoba.interpreter.api.service.LocalRouterService;
import com.xiaoxiao.qiaoba.interpreter.api.service.RemoteRouterService;
import com.xiaoxiao.qiaoba.interpreter.utils.RouterUtils;
import com.xiaoxiao.qiaoba.protocol.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangfei on 2017/7/23.
 */

public class RemoteRouter {

    public static final String PROCESS_NAME = "remote";

    private Map<String, Class<? extends LocalRouterService>> mLocalServiceMap;
    private Map<String, ServiceConnection> mLocalServiceConnectionMap;
    private Map<String, ILocalRouterInterface> mLocalServiceAIDLMap;
    private Map<String, ActionRequest> mActionRequestQueue;
    private static RemoteRouter mInstance;
    private Context mContext;
    private Boolean mIsStoping = false; // 当前service是否停止的标志，

    private RemoteRouter(){
        mLocalServiceMap = new HashMap<>();
        // why thread security？ 因为他们都有添加和删除操作，并且是在不同的Binder线程中，因此要保证线程安全
        mLocalServiceConnectionMap = new ConcurrentHashMap<>();
        mLocalServiceAIDLMap = new ConcurrentHashMap<>();
        mActionRequestQueue = new ConcurrentHashMap<>();
    }

    public void init(QiaobaApplication application) {
        mContext = application;
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
            // 常规情况下不应该出现
            throw new RuntimeException("ActionRequest is null in remote responseConnect!!!");
        }
        mActionRequestQueue.put(request.getUuid(), request);
        final String domain = request.getOriginDomain();
        if(mLocalServiceMap.get(domain) == null){
            // 抛出异常： 没有对应的 router，请检查
            throw new LocalRouterServiceNotRegisted(domain);
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
        if(mIsStoping){ // 在RemoteService停止期间的请求， 直接返回错误
            ActionResult result = new ActionResult.Builder()
                    .code(ActionResult.CODE_REMOTE_SERVICE_STOPING)
                    .build();
            responseData(uuid, result);
            return false;
        }
        if(mLocalServiceMap.get(domain) == null){
            // 抛出异常： 没有对应的 router，请检查
            responseData(uuid, new ActionResult.Builder()
                    .code(ActionResult.CODE_INVOKE_ROUTER_ROUTER_SERVICE_NOT_REGIESTED)
                    .uuid(uuid).router(router)
                    .data("invoke router["+ router +"]'s domain["+ domain +"]'s LocalRouterService isn't registed in remote!!!").build());
            removeActionRequest(uuid);
            return false;
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
            // 一般情况下，不应该出现
            throw new RuntimeException("ActionRequest is null in remote responseConnect!!!");
        }

        if(mIsStoping){
            ActionResult result = new ActionResult.Builder()
                    .code(ActionResult.CODE_REMOTE_SERVICE_STOPING)
                    .build();
            responseData(request.getUuid(), result);
            return;
        }

        // 1. 从router中解析出 对应的 domain
        final String domain = RouterUtils.getDomainFromRouter(request.getRouter());
        if(StringUtils.isEmpty(domain)){
            // 没有找到对应的进程, router不是标准router
            responseData(request.getUuid(),
                    new ActionResult.Builder().uuid(request.getUuid()).router(request.getRouter()).data("invoke router's domain is empty, please check your router:["+request.getRouter()+"]").build());
            return;
        }

        mActionRequestQueue.put(request.getUuid(), request);

        // 2. 通过domain，查找对应的 service，发送call命令
        if(mLocalServiceAIDLMap.get(domain) != null){
            try {
                mLocalServiceAIDLMap.get(domain).resolveRouter(request.getUuid(), request.getOriginDomain(), request.getRouter(), request.getJsonData(), request.getCallType());
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
            throw new RuntimeException("Action request can't be found in remote responseCall");
        }
        String domain = RouterUtils.getDomainFromRouter(request.getRouter());
        if(mLocalServiceAIDLMap.get(domain) != null){
            try {
                mLocalServiceAIDLMap.get(domain).resolveRouter(uuid, request.getOriginDomain(), request.getRouter(), request.getJsonData(), request.getCallType());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else {
            // 重新链接， 设置最大重试次数 (一般也不应该出现，因为基本上responseConnect都是立即同步执行，基本没有延迟，除非出现远程连接)
        }
    }

    public void responseData(String uuid, ActionResult result){
        ActionRequest actionRequest = mActionRequestQueue.get(uuid);
        if(actionRequest == null){
            // 抛出异常 或者不做处理
        }else {
            removeActionRequest(uuid); // 将完成的action requset移除
            if(actionRequest.getCallType() == ActionRequest.TYPE_CALL_NO_CALLBACK){
                return;
            }
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

    private void removeActionRequest(String uuid){
        mActionRequestQueue.remove(uuid);
    }

    /**
     * 断开远程和 local的binder aidl的链接
     * @param domain
     */
    public boolean disconnectLocalService(String domain){
        if(StringUtils.isEmpty(domain)){
            return false;
        }else if("remote".equals(domain)){
            stopSelf();
            return true;
        }else if(mLocalServiceConnectionMap.get(domain) == null){
            return false;
        } else {
            ILocalRouterInterface localRouterAIDL = mLocalServiceAIDLMap.get(domain);
            if(localRouterAIDL != null){
                try {
                    localRouterAIDL.disconnectRemote();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            mLocalServiceAIDLMap.remove(domain);
            if(mLocalServiceConnectionMap.get(domain) != null){
                mContext.unbindService(mLocalServiceConnectionMap.get(domain));
                mLocalServiceConnectionMap.remove(domain);
            }
            return true;
        }
    }

    /**
     * 杀掉自己的进程
     * 需要先断开所有的链接，然后杀掉自己以及自己的进程
     */
    private void stopSelf() {
        mIsStoping = true;
        // 开辟子线程执行， 是为了不阻塞binder线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> localKeys = new ArrayList<String>();
                localKeys.addAll(mLocalServiceAIDLMap.keySet());
                for (String key : localKeys){
                    ILocalRouterInterface localRouterAIDL = mLocalServiceAIDLMap.get(key);
                    if(localRouterAIDL != null){
                        try {
                            localRouterAIDL.disconnectRemote();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    mContext.unbindService(mLocalServiceConnectionMap.get(key));
                    mLocalServiceConnectionMap.remove(key);
                    mLocalServiceAIDLMap.remove(key);
                }
                mActionRequestQueue.clear();
                try {
                    Thread.sleep(1000); // 睡眠一秒， 等上面的所有local router 都和remote断开链接
                    mContext.stopService(new Intent(mContext, RemoteRouterService.class));
                    Thread.sleep(1000); // 睡眠一秒， 等RemoteRouterService被系统杀死； 然后退出系统
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(0); // 退出系统， 关掉进程
            }
        }){}.start();

    }


}
