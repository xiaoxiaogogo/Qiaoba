package com.xiaoxiao.qiaoba.interpreter.api.provider;

import com.xiaoxiao.qiaoba.interpreter.api.action.IAction;
import com.xiaoxiao.qiaoba.interpreter.utils.RouterUtils;
import com.xiaoxiao.qiaoba.protocol.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangfei on 2017/7/20.
 */

public class RouterProvider implements IProvider {

    private String mDomain;
    private String mPathName;
    private Map<String, IAction> mActionList;

    public RouterProvider(String mDomain, String mPathName) {
        this.mDomain = mDomain;
        this.mPathName = mPathName;
        mActionList = new HashMap<>();
    }

    @Override
    public void regiestAction(String actionName, IAction action) {
        this.mActionList.put(actionName, action);
    }

    @Override
    public String getRouterPath() {
        return RouterUtils.getRouterString(this.mDomain, this.mPathName);
    }

    @Override
    public IAction getAction(String actionName) {
        return mActionList.get(actionName);
    }
}
