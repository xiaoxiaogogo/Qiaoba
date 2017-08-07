package com.xiaoxiao.qiaoba.interpreter.api.action;

import com.xiaoxiao.qiaoba.interpreter.api.callback.ActionCallback;
import com.xiaoxiao.qiaoba.interpreter.api.router.ActionResult;

import java.util.Map;

/**
 * Created by wangfei on 2017/7/20.
 */

public interface IAction {
    void invoke(String jsonData, ActionCallback callback);
}
