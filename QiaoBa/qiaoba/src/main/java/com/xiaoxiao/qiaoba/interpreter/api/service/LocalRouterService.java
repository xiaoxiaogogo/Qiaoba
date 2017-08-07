package com.xiaoxiao.qiaoba.interpreter.api.service;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.xiaoxiao.qiaoba.interpreter.api.ILocalRouterInterface;
import com.xiaoxiao.qiaoba.interpreter.api.IRemoteRouterInterface;
import com.xiaoxiao.qiaoba.interpreter.api.router.ActionResult;
import com.xiaoxiao.qiaoba.interpreter.api.router.LocalRouter;

/**
 * Created by wangfei on 2017/7/23.
 */

public class LocalRouterService extends Service {

    private ServiceConnection mRemoteServiceConnection;
    private IRemoteRouterInterface mRemoteRouterAIDL;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    private class MyBinder extends ILocalRouterInterface.Stub{

        @Override
        public boolean responseConnect(String uuid) throws RemoteException {
            return LocalRouter.getInstance().responseConnect(uuid);
        }

        @Override
        public void responseInvoke(String uuid) throws RemoteException {
            LocalRouter.getInstance().responseInvoke(uuid);
        }

        @Override
        public void responseData(ActionResult result) throws RemoteException {
            LocalRouter.getInstance().responseData(result);
        }

        @Override
        public void resolveRouter(String uuid, String originDomain, String router, String jsonData) throws RemoteException {
            // 有两种方式， 直接通过此方法返回执行的结果； 但是这是一个同步的过程； 可能会有阻塞情况
            // 建立连接， 通过响应式的方式来响应，不会产生阻塞； （同互联网的方式）

            // 第一种： 直接返回
            LocalRouter.getInstance().resolveRouter(uuid, originDomain, router, jsonData);
//            if(mRemoteRouterAIDL != null){
//                mRemoteRouterAIDL.responseData(actionResult);
//            }else {
//                // 需要重新链接remote service
//            }
        }
    }

}
