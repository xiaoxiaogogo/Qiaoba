package com.xiaoxiao.qiaobademo.denpendency;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.xiaoxiao.qiaoba.annotation.di.DependInsert;
import com.xiaoxiao.qiaoba.annotation.di.Dependency;
import com.xiaoxiao.qiaoba.interpreter.interpreter.DependencyInsertInterpreter;
import com.xiaoxiao.qiaobademo.TestService;

/**
 * Created by wangfei on 2017/3/2.
 */
@Dependency("demo")
public class DenpendencyDemo implements IDependency {

    @Override
    public void showHello(Context context) {
        Toast.makeText(context, "show hello!!!", Toast.LENGTH_SHORT).show();
    }
}
