package com.xiaoxiao.qiaoba.interpreter.utils;

import java.util.UUID;

/**
 * Created by wangfei on 2017/8/6.
 */

public class UUIDUtils {

    public static String generateUUID(){
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        str = str.replace("-", "");
        return str;
    }
}
