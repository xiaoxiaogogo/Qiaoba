package com.xiaoxiao.qiaobademo;

import android.content.Context;
import android.widget.Toast;

import com.protocol.annotation.Provider;

/**
 * Created by wangfei on 16/12/6.
 */
@Provider("test")
public class TestService {

    public void doService(Context context, String str){
        Toast.makeText(context,"come from main module : "+ str, Toast.LENGTH_SHORT).show();
    }


}
