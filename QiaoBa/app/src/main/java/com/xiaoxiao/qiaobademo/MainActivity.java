package com.xiaoxiao.qiaobademo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.xiaoxiao.qiaoba.annotation.di.DependInsert;
import com.xiaoxiao.qiaoba.interpreter.Qiaoba;
import com.xiaoxiao.qiaoba.interpreter.interpreter.RouterInterpreter;
import com.xiaoxiao.qiaoba.interpreter.interpreter.DependencyInsertInterpreter;
import com.xiaoxiao.qiaoba.interpreter.interpreter.FragmentLinkInterpreter;
import com.xiaoxiao.qiaobademo.denpendency.IDependency;

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

                dependency.showHello(getApplicationContext());
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

        Qiaoba.getInstance().getFragmentLinkInterpreter().build("/second/demo")
                .withString("name", "xiaoming")
                .parentActivity(this)
                .resId(R.id.fragment)
                .linkFragment();
    }
}
