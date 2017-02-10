package com.gavin.secondmodule.caller;

import com.gavin.secondmodule.DemoAnno;
import com.xiaoxiao.qiaoba.annotation.communication.CallBack;
import com.xiaoxiao.qiaoba.annotation.communication.CallbackParam;

/**
 * Created by wangfei on 17/1/8.
 */
@CallbackParam("test")
public interface TestCallback {
    void showHello(String msg);
    int getNum();
}
