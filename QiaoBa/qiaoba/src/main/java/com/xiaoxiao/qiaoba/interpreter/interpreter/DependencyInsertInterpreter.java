package com.xiaoxiao.qiaoba.interpreter.interpreter;

import com.xiaoxiao.qiaoba.annotation.model.DependencyInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangfei on 2017/3/2.
 */

public class DependencyInsertInterpreter {

    public static Map<String, DependencyInfo> dependencyInfoMap = new HashMap<>();

    private DependencyInsertInterpreter _instance;

    private DependencyInsertInterpreter(){}

    private static class Holder{
        static DependencyInsertInterpreter instance = new DependencyInsertInterpreter();
    }

    public DependencyInsertInterpreter getInstance(){
        if(_instance == null){
            _instance = Holder.instance;
        }
        return _instance;
    }



}
