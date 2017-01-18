package com.xiaoxiao.qiaoba.interpreter.router;

import android.content.Context;

/**
 * Created by wangfei on 2017/1/16.
 */

public interface RouterInterceptor {

    boolean intercept(String pathUri, Context context);

    void onIntercepted(String pathUri, Context context);

}
