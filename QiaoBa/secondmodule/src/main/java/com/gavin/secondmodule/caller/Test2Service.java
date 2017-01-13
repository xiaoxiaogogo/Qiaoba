package com.gavin.secondmodule.caller;

import android.content.Context;

import com.xiaoxiao.qiaoba.annotation.communication.CallbackParam;
import com.xiaoxiao.qiaoba.annotation.communication.Caller;

/**
 * Created by wangfei on 2017/1/13.
 */

@Caller("test")
public interface Test2Service {
    void doService(Context context, String str, @CallbackParam("test") TestCallback callback);
}
