package com.xiaoxiao.qiaoba.interpreter.initalize;

import com.xiaoxiao.qiaoba.annotation.model.DependencyInfo;

import java.util.Map;

/**
 * Created by wangfei on 2017/3/1.
 */

public interface DenpendencyInitalizer {
    void loadDenpendency(Map<String, DependencyInfo> datas);
}
