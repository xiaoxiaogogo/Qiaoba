package com.gavin.secondmodule;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.gavin.secondmodule.caller.TestCallback;

/**
 * Created by wangfei on 2017/2/13.
 */

public class TestCallbackDemo implements TestCallback {
    private Context mContext;
    public TestCallbackDemo(Context context){
        mContext = context;
    }

    @Override
    public void showHello(final String msg) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, "say hello : " + msg + " in second activity", Toast.LENGTH_SHORT).show();
            }
        }, 3000);
    }
    @Override
    public int getNum() {
        return 99;
    }
}
