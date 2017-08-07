package com.xiaoxiao.qiaoba.interpreter.api.provider;

import com.xiaoxiao.qiaoba.interpreter.api.action.IAction;

/**
 * Created by wangfei on 2017/7/20.
 */

public interface IProvider {
    void regiestAction(String actionName, IAction action);
    String getRouterPath();
    IAction getAction(String actionName);
}
