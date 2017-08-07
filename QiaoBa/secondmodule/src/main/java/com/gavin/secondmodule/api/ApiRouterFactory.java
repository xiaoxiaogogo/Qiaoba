package com.gavin.secondmodule.api;

import com.xiaoxiao.qiaoba.interpreter.api.router.LocalRouter;

/**
 * Created by wangfei on 2017/7/30.
 */

public class ApiRouterFactory {
    public static void init(){
        LocalRouter.getInstance().registeProvider(new SecondProvider("second", "demo"));
    }
}
