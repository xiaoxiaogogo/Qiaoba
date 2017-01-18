package com.xiaoxiao.qiaoba.interpreter.router;

/**
 * Created by wangfei on 2017/1/16.
 */

public interface RouterCallback {

    void onSuccess();

    void onError(Throwable error);

}
