package com.gavin.secondmodule.caller;

import android.content.Context;

import com.protocol.annotation.communication.CallbackParam;
import com.protocol.annotation.communication.Caller;

/**
 * Created by wangfei on 2016/12/20.
 */
@Caller("test")
public interface TestService {

    public void doService(Context context, String str,@CallbackParam("test") TestCallback callback);
}
