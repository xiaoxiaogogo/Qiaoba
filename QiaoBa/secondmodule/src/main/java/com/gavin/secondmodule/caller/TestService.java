package com.gavin.secondmodule.caller;

import android.content.Context;

import com.protocol.annotation.Caller;

/**
 * Created by wangfei on 2016/12/20.
 */
@Caller("test")
public interface TestService {

    public void doService(Context context, String str);
}
