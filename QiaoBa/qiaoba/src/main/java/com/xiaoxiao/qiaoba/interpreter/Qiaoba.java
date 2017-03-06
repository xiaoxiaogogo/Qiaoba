package com.xiaoxiao.qiaoba.interpreter;

import android.content.Context;
import android.content.pm.PackageManager;

import com.xiaoxiao.qiaoba.interpreter.exception.HandlerException;
import com.xiaoxiao.qiaoba.interpreter.interpreter.DependencyInsertInterpreter;
import com.xiaoxiao.qiaoba.interpreter.utils.ClassUtils;
import com.xiaoxiao.qiaoba.protocol.factory.DenpendencyFactory;
import com.xiaoxiao.qiaoba.protocol.model.DataClassCreator;

import java.io.IOException;
import java.util.List;

/**
 * Created by wangfei on 2017/3/2.
 */

public class Qiaoba {

    private static Qiaoba _instance;

    private Qiaoba(){}

    static class Holder{
        public static Qiaoba instance = new Qiaoba();
    }

    public static Qiaoba getInstance(){
        if(_instance == null){
            _instance = Holder.instance;
        }
        return _instance;
    }

    private static boolean mIsInit = false;

    public static void init(Context context){
        if(!mIsInit){
            //进行init操作，从当前apk中读取我们需要的生成的class
            try {
                List<String> classNames = ClassUtils.getClassNameByPackageName(context, DataClassCreator.getCreateClassPackageName());
                for (String className : classNames){
                    if(className.startsWith(DataClassCreator.getDependencyUtilsStartName())){
                        ((DenpendencyFactory)(Class.forName(className).getConstructor().newInstance())).loadDenpendency(DependencyInsertInterpreter.dependencyInfoMap);
                    }
                }
            } catch (Exception e) {
                throw new HandlerException("Qiaoba init center exception!["+e.getMessage()+"]");
            }
        }
        mIsInit = true;
    }


}
