package com.xiaoxiao.qiaoba;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import com.protocol.annotation.RouterParam;
import com.protocol.annotation.RouterUri;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangfei on 2016/12/21.
 */

public class RouterInterpreter {

    private static Context mContext;

    private Map<Class<?>, Object> mStubMap = new HashMap<>();
    private Map<Class<?>, InvocationHandler> mInvocationHandlerMap = new HashMap<>();

    public static void init(Context context){
        mContext = context;
    }

    private RouterInterpreter(){}

    static class Holder{
        public static RouterInterpreter instance = new RouterInterpreter();
    }

    public static RouterInterpreter getInstance(){
        return Holder.instance;
    }

    public <T> T create(Class<T> routerClazz){
        if(mStubMap.get(routerClazz) != null){
            return (T) mStubMap.get(routerClazz);
        }

        InvocationHandler handler = null;
        handler = findHandler(routerClazz);
        if(handler == null){
            throw new RuntimeException("errpr!! router uri findHandler() is null!");
        }
        Object routerStub = Proxy.newProxyInstance(routerClazz.getClassLoader(), new Class[]{routerClazz}, handler);
        mStubMap.put(routerClazz, routerStub);
        return (T) routerStub;
    }

    private InvocationHandler findHandler(Class<?> routerClazz) {
        if(mInvocationHandlerMap.get(routerClazz) != null){
            return mInvocationHandlerMap.get(routerClazz);
        }
        Method[] methods = routerClazz.getDeclaredMethods();
        int routerUriMethodNum = 0;
        for (Method method : methods){
            method.setAccessible(true);
            RouterUri anno = method.getAnnotation(RouterUri.class);
            if(anno != null){
                routerUriMethodNum++;
            }
        }
        if(routerUriMethodNum <=0){
            throw new RuntimeException("error!! can't find method with annotation RouterUri!");
        }
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if(method.getAnnotation(RouterUri.class) != null){
                    StringBuilder sb= new StringBuilder();
                    RouterUri uriAnno = method.getAnnotation(RouterUri.class);
                    String pathUri = uriAnno.value();
                    sb.append(pathUri);
                    Annotation[][] paramAnnos = method.getParameterAnnotations();
                    int pos =0;
                    for (int i=0; i < paramAnnos.length; i++){
                        Annotation[] annos = paramAnnos[i];
                        if(annos != null && annos.length > 0){
                            if(annos[0] instanceof RouterParam) {
                                if (pos == 0) {
                                    sb.append("?");
                                } else {
                                    sb.append("&");
                                }
                                pos++;
                                RouterParam paramAnno = (RouterParam) annos[0];
                                String paramKey = paramAnno.value();
                                sb.append(paramKey);
                                sb.append("=");
                                sb.append(args[i]);
                            }
                        }
                    }

                    openRouterUri(sb.toString());

                }
                return null;
            }
        };
        mInvocationHandlerMap.put(routerClazz, handler);
        return handler;
    }

    private void openRouterUri(String routerUri) {
        Uri uri = Uri.parse(routerUri);
        PackageManager packageManager = mContext.getPackageManager();
        Intent uriIntent = new Intent(Intent.ACTION_VIEW, uri);
        uriIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        List<ResolveInfo> activitys = packageManager.queryIntentActivities(uriIntent, 0);
        if(activitys.size() > 0){
            mContext.startActivity(uriIntent);
        }
    }

}
