package com.xiaoxiao.qiaobademo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.gavin.secondmodule.SecondDemoActivity;
import com.xiaoxiao.qiaoba.annotation.di.DependInsert;
import com.xiaoxiao.qiaoba.interpreter.Qiaoba;
import com.xiaoxiao.qiaoba.interpreter.interpreter.DependencyInsertInterpreter;
import com.xiaoxiao.qiaobademo.denpendency.DenpendencyDemo;
import com.xiaoxiao.qiaobademo.denpendency.IDependency;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    @DependInsert("demo")
    private IDependency dependency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DependencyInsertInterpreter.getInstance().inject(this);

        setContentView(R.layout.activity_main);
        findViewById(R.id.show_second_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SecondDemoActivity.class));

                dependency.showHello(getApplicationContext());
            }
        });
    }
}
