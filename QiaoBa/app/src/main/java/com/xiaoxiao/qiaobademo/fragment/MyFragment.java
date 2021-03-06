package com.xiaoxiao.qiaobademo.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xiaoxiao.qiaoba.annotation.router.FragmentLinkUri;

/**
 * Created by wangfei on 2017/3/14.
 */
@FragmentLinkUri("/app/myfragment")
public class MyFragment extends Fragment {

    String name;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            name = getArguments().getString("name");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500));
        textView.setTextSize(50);
        textView.setGravity(Gravity.CENTER);
        textView.setText(name);
        textView.setTextColor(Color.RED);
        return textView;
    }


}
