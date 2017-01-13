package com.qiaoba.protocol.utils;

import com.qiaoba.protocol.model.ElementHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangfei on 2017/1/12.
 */

public class ElementCacheUtils {

    private static ElementCacheUtils _instance;

    public static Map<String, ElementHolder> provider = new HashMap<>();
    public static Map<String, ElementHolder> caller = new HashMap<>();

    private ElementCacheUtils(){}

    static class InnerHolder{
        public static ElementCacheUtils instance = new ElementCacheUtils();
    }

    public static ElementCacheUtils getInstance(){
        if(_instance == null){
            _instance = InnerHolder.instance;
        }
        return _instance;
    }



}
