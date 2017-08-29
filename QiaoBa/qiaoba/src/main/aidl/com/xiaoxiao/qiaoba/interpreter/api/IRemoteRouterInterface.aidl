// IRemoteRouterInterface.aidl
package com.xiaoxiao.qiaoba.interpreter.api;
import com.xiaoxiao.qiaoba.interpreter.api.router.ActionResult;
import com.xiaoxiao.qiaoba.interpreter.api.router.ActionRequest;

// Declare any non-default types here with import statements

interface IRemoteRouterInterface {
    // 提供给调用方使用
    // 让调用方连接自己
    boolean responseConnect(in ActionRequest request);
    // 调用对应进程的命令
    void callRouter(in ActionRequest request);

    // 用于调用对应进程的命令
    void responseCall(String uuid);
    // 用于接收 提供方进程的返回结果
    void responseData(String uuid, in ActionResult result);

    boolean disconnectLocalService(String domain);
}
