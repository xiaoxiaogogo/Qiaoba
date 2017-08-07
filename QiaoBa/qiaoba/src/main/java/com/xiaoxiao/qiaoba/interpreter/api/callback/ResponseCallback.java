package com.xiaoxiao.qiaoba.interpreter.api.callback;

import com.xiaoxiao.qiaoba.interpreter.api.router.ActionResult;

/**
 * Created by wangfei on 2017/8/6.
 */

public interface ResponseCallback {
    void onSuccess(ActionResult response);
    void onError(ActionResult error);
}
