package com.xiaoxiao.qiaoba.protocol.factory;

import com.xiaoxiao.qiaoba.annotation.model.DependencyInfo;

import java.util.Map;

/**
 * Created by wangfei on 2017/3/1.
 */

public interface DenpendencyFactory {
    void loadDenpendency(Map<String, DependencyInfo> datas);
}
