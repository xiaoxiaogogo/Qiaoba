package com.xiaoxiao.qiaoba.interpreter;


import android.util.Log;

import com.xiaoxiao.qiaoba.annotation.di.DependInsert;
import com.xiaoxiao.qiaoba.annotation.di.Dependency;

/**
 * Created by wangfei on 2017/3/2.
 */
public class DenpendencyDemo {

    @DependInsert("asd")
    private String testService;

    public void test(){
//        testService = "2213";
        String demo = testService;
        String ddd= testService;
        Log.e("mytest", ddd + " ; " + demo);
    }

}
