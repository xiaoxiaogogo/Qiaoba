// ILocalRouterInterface.aidl
package com.xiaoxiao.qiaoba.interpreter.api;
import com.xiaoxiao.qiaoba.interpreter.api.router.ActionResult;

// Declare any non-default types here with import statements

interface ILocalRouterInterface {
    // 调用方
    void responseInvoke(String uuid);
    // 返回数据
    void responseData(in ActionResult result);

    // 提供方（被调用方）
    // 用于链接远程进程服务（）
    boolean responseConnect(String uuid);
    void resolveRouter(String uuid, String originDomain, String router, String jsonData);
}
