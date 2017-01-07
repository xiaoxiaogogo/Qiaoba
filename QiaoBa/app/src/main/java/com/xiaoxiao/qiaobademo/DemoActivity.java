package com.xiaoxiao.qiaobademo;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.protocol.annotation.router.RouterLinkUri;

/**
 * Created by wangfei on 2016/12/21.
 */
@RouterLinkUri("xl://main:8888/demo?key=sada")
public class DemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_demo);

        Uri data = getIntent().getData();
        final String key = data.getQueryParameter("key");

        findViewById(R.id.demo_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DemoActivity.this, "Demo Activity key : " + key, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
