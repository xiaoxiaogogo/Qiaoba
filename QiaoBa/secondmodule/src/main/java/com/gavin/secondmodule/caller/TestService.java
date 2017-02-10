package com.gavin.secondmodule.caller;

import android.content.Context;

import com.xiaoxiao.qiaoba.annotation.communication.CallbackParam;
import com.xiaoxiao.qiaoba.annotation.communication.Caller;
import com.xiaoxiao.qiaoba.annotation.communication.CommuApiMethod;

/**
 * Created by wangfei on 2016/12/20.
 */
@Caller("test")
public interface TestService {
    //@CallbackParam("test") TestCallback callback
    @CommuApiMethod
    public void doService(Context context, String str);
}
