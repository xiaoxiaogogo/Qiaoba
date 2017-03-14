package com.xiaoxiao.qiaoba.interpreter;

import android.content.Context;

import com.xiaoxiao.qiaoba.interpreter.exception.HandlerException;
import com.xiaoxiao.qiaoba.interpreter.initalize.FragmentLinkInitalizer;
import com.xiaoxiao.qiaoba.interpreter.interpreter.DependencyInsertInterpreter;
import com.xiaoxiao.qiaoba.interpreter.interpreter.FragmentLinkInterpreter;
import com.xiaoxiao.qiaoba.interpreter.interpreter.ProtocolInterpreter;
import com.xiaoxiao.qiaoba.interpreter.interpreter.RouterInterpreter;
import com.xiaoxiao.qiaoba.interpreter.initalize.IActivityRouterInitalizer;
import com.xiaoxiao.qiaoba.interpreter.utils.ClassUtils;
import com.xiaoxiao.qiaoba.interpreter.initalize.DenpendencyInitalizer;
import com.xiaoxiao.qiaoba.protocol.model.DataClassCreator;

import java.util.List;

/**
 * Created by wangfei on 2017/3/2.
 */

public class Qiaoba {

    private RouterInterpreter mRouterInterpreter;
    private ProtocolInterpreter mProtocolInterpreter;
    private FragmentLinkInterpreter mFragmentLinkInterpreter;
    private DependencyInsertInterpreter mDIInterpreter;
    private static boolean mIsInit = false;


    private Qiaoba(){
        mRouterInterpreter = new RouterInterpreter();
        mProtocolInterpreter =new ProtocolInterpreter();
        mFragmentLinkInterpreter = new FragmentLinkInterpreter();
        mDIInterpreter = new DependencyInsertInterpreter();
    }

    private static Qiaoba _instance;

    static class Holder{
        public static Qiaoba instance = new Qiaoba();
    }

    public static Qiaoba getInstance(){
        if(_instance == null){
            _instance = Holder.instance;
        }
        return _instance;
    }

    public RouterInterpreter getRouterInterpreter() {
        return mRouterInterpreter;
    }

    public ProtocolInterpreter getProtocolInterpreter() {
        return mProtocolInterpreter;
    }

    public FragmentLinkInterpreter getFragmentLinkInterpreter() {
        return mFragmentLinkInterpreter;
    }

    public DependencyInsertInterpreter getDIInterpreter() {
        return mDIInterpreter;
    }

    public static void init(Context context){
        if(!mIsInit){
            //进行init操作，从当前apk中读取我们需要的生成的class
            try {
                List<String> classNames = ClassUtils.getClassNameByPackageName(context, DataClassCreator.getCreateClassPackageName());
                for (String className : classNames){
                    if(className.startsWith(DataClassCreator.getDependencyUtilsStartName())){
                        ((DenpendencyInitalizer)(Class.forName(className).getConstructor().newInstance())).loadDenpendency(DependencyInsertInterpreter.dependencyInfoMap);
                    }else if(className.startsWith(DataClassCreator.getFragmentLinkUtilsStartName())){
                        ((FragmentLinkInitalizer)(Class.forName(className).getConstructor().newInstance())).loadFragmentLink(FragmentLinkInterpreter.FRAGMENT_LINK_MAP);
                    }else if(className.startsWith(DataClassCreator.getRouterLinkUtilsStartName())){
                        ((IActivityRouterInitalizer)(Class.forName(className).getConstructor().newInstance())).initRouterTable(RouterInterpreter.mActivityRouterMap);
                    }
                }
            } catch (Exception e) {
                throw new HandlerException("Qiaoba init center exception!["+e.getMessage()+"]");
            }
            RouterInterpreter.init(context);
        }
        mIsInit = true;
    }


}
