package com.xiaoxiao.qiaobademo.denpendency;


import com.xiaoxiao.qiaoba.annotation.di.DependInsert;
import com.xiaoxiao.qiaoba.annotation.di.Dependency;
import com.xiaoxiao.qiaobademo.TestService;

/**
 * Created by wangfei on 2017/3/2.
 */
@Dependency("demo")
public class DenpendencyDemo {

    @DependInsert("asd")
    private TestService testService;

    public void test(){
        TestService t = testService;
        if(testService != null){
            testService.doService(null, null, null);
        }
    }

}
