package com.gavin.secondmodule.router;

import com.protocol.annotation.router.RouterParam;
import com.protocol.annotation.router.RouterUri;

/**
 * Created by wangfei on 2016/12/21.
 */

public interface IRouterUri {
    @RouterUri("xl://main:8888/demo")//此注解对应的是 url的地址
    public void jumpToDemo(@RouterParam("key") String key);//RouterParam 对应的参数，里面值是参数名

    //如果增加页面，可以在这里增加对应的方法即可

}
