package com.xiaoxiao.qiaobademo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.xiaoxiao.qiaoba.annotation.di.DependInsert;
import com.xiaoxiao.qiaoba.interpreter.Qiaoba;
import com.xiaoxiao.qiaoba.interpreter.api.callback.ResponseCallback;
import com.xiaoxiao.qiaoba.interpreter.api.router.ActionRequest;
import com.xiaoxiao.qiaoba.interpreter.api.router.ActionResult;
import com.xiaoxiao.qiaoba.interpreter.api.router.LocalRouter;
import com.xiaoxiao.qiaoba.interpreter.interpreter.RouterInterpreter;
import com.xiaoxiao.qiaoba.interpreter.interpreter.DependencyInsertInterpreter;
import com.xiaoxiao.qiaoba.interpreter.interpreter.FragmentLinkInterpreter;
import com.xiaoxiao.qiaobademo.denpendency.IDependency;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @DependInsert("demo")
    private IDependency dependency;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Qiaoba.getInstance().getDIInterpreter().inject(this);

        setContentView(R.layout.activity_main);
        findViewById(R.id.show_second_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, SecondDemoActivity.class));

                Qiaoba.getInstance().getRouterInterpreter().build("/second/demo")
                        .navigation();
                // 依赖注入失败
               // dependency.showHello(getApplicationContext());
            }
        });
//        FragmentLinkInterpreter.Builder builder = new FragmentLinkInterpreter.Builder("/second/demo");
//        builder.withString("name", "xiaoming");
//        Object fragment = FragmentLinkInterpreter.getInstance().getFragmentFromLink(builder, null);
//        if(fragment != null){
//            getSupportFragmentManager().beginTransaction().add(R.id.fragment, (Fragment) fragment).commitAllowingStateLoss();
//        }else {
//            Toast.makeText(getApplicationContext(), "fragment is null", Toast.LENGTH_SHORT).show();
//        }

        findViewById(R.id.call_say_hi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("key", "test");
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put("a");
                    jsonArray.put("b");
                    jsonArray.put("c");
                    jsonObject.put("arr", jsonArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                LocalRouter.getInstance().invokeRouter("second/demo/sayhi?userid=zu123&passwd=kakajiji", jsonObject.toString(), ActionRequest.TYPE_CALL_ONEWAY, new ResponseCallback() {
                    @Override
                    public void onSuccess(ActionResult response) {
                        Log.e("mytest", "response success : " + response.getJsonData() + "  :  " + response.getCode());
                        Log.e("mytest", "thread : " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(ActionResult error) {
                        Log.e("mytest", "reponse error : error : " + error.getCode() + " : " + error.getJsonData());
                    }
                });
            }
        });


        Qiaoba.getInstance().getFragmentLinkInterpreter().build("/second/demo/sayhi")
                .withString("name", "xiaoming")
                .parentActivity(this)
                .resId(R.id.fragment)
                .linkFragment();
    }
}
