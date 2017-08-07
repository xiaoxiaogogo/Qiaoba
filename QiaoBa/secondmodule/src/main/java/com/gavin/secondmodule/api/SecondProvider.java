package com.gavin.secondmodule.api;

import com.xiaoxiao.qiaoba.interpreter.api.action.IAction;
import com.xiaoxiao.qiaoba.interpreter.api.provider.IProvider;
import com.xiaoxiao.qiaoba.interpreter.api.provider.RouterProvider;

/**
 * Created by wangfei on 2017/7/30.
 */

public class SecondProvider extends RouterProvider {

    public SecondProvider(String mDomain, String mPathName) {
        super(mDomain, mPathName);
        this.regiestAction("sayhi", new SayHiAction());
    }

}
