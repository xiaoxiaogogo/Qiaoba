package com.xiaoxiao.qiaoba.interpreter.router;

import android.app.Activity;

import java.util.Map;

/**
 * Created by wangfei on 17/1/12.
 */

public interface IActivityRouterInitalizer {
    void initRouterTable(Map<String, Class<? extends Activity>> routerMap);
}
