package com.xiaoxiao.qiaobademo;

import com.protocol.annotation.communication.CallBack;

/**
 * Created by wangfei on 17/1/8.
 */

@CallBack("test")
public interface TestCallback {
    void showHello(String msg);
    int getNum();
}
