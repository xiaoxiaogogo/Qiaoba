package com.gavin.secondmodule;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.gavin.secondmodule.caller.Test2Service;
import com.gavin.secondmodule.caller.TestCallback;
import com.gavin.secondmodule.caller.TestService;
import com.gavin.secondmodule.router.IRouterUri;
import com.xiaoxiao.qiaoba.interpreter.ProtocolInterpreter;
import com.xiaoxiao.qiaoba.interpreter.RouterInterpreter;

/**
 * Created by wangfei on 2016/12/20.
 */

public class SecondDemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_second_demo);

        findViewById(R.id.show_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ProtocolInterpreter.getInstance().create(Test2Service.class).doService(SecondDemoActivity.this,
                            "second activity show toast", new TestCallback(){
                        @Override
                        public void showHello(final String msg) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SecondDemoActivity.this, "say hello : " + msg + " in second activity", Toast.LENGTH_SHORT).show();
                                }
                            }, 3000);
                        }
                        @Override
                        public int getNum() {
                            return 99;
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e("mytest", e.toString());
                }
            }
        });

        findViewById(R.id.start_main_demo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouterInterpreter.getInstance().create(IRouterUri.class).jumpToDemo("second module data");
            }
        });


        findViewById(R.id.start_link_demo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                RouterInterpreter.getInstance().openRouterUri("xl://main:8888/linkdemo?key=fuck&ddd=you");
                RouterInterpreter.getInstance()
                        .build("xl://main:8888/linkdemo")
                        .withString("key","fuck")
                        .withString("ddd","you")
//                        .addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                        .requestCode(12, SecondDemoActivity.this)
                        .navigation();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 12 && resultCode == Activity.RESULT_OK){
            Toast.makeText(this, "return from last page", Toast.LENGTH_SHORT).show();
        }
    }
}
