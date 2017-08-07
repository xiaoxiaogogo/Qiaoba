package com.xiaoxiao.qiaoba.interpreter.api.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.xiaoxiao.qiaoba.interpreter.api.IRemoteRouterInterface;
import com.xiaoxiao.qiaoba.interpreter.api.router.ActionRequest;
import com.xiaoxiao.qiaoba.interpreter.api.router.ActionResult;
import com.xiaoxiao.qiaoba.interpreter.api.router.RemoteRouter;

/**
 * Created by wangfei on 2017/7/23.
 */

public class RemoteRouterService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    private class Binder extends IRemoteRouterInterface.Stub{

        @Override
        public boolean responseConnect(ActionRequest request) throws RemoteException {
            return RemoteRouter.getInstance().responseConnect(request, getApplicationContext());
        }

        @Override
        public void callRouter(ActionRequest request) throws RemoteException {
            RemoteRouter.getInstance().callRouter(request, getApplicationContext());
        }

        @Override
        public void responseCall(String uuid) throws RemoteException {
            RemoteRouter.getInstance().responseCall(uuid);
        }

        @Override
        public void responseData(String uuid, ActionResult result) throws RemoteException {
            RemoteRouter.getInstance().responseData(uuid, result);
        }
    }

}
