package com.xiaoxiao.qiaobademo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.protocol.annotation.RouterLinkUri;

/**
 * Created by wangfei on 2016/12/22.
 */

@RouterLinkUri("xl://main:8888/linkdemo")
public class RouterLinkDemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_router_link_demo);

        Intent intent = getIntent();
        String val1 = intent.getStringExtra("key");
        String val2 = intent.getStringExtra("ddd");
        Toast.makeText(this, "the data from last page : " + val1 + " " + val2, Toast.LENGTH_SHORT).show();

    }


}
