package com.xiaoxiao.qiaoba.interpreter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import com.xiaoxiao.qiaoba.protocol.model.DataClassCreator;
import com.xiaoxiao.qiaoba.annotation.router.RouterParam;
import com.xiaoxiao.qiaoba.annotation.router.RouterUri;
import com.xiaoxiao.qiaoba.interpreter.router.IActivityRouterInitalizer;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by wangfei on 2016/12/21.
 */

public class RouterInterpreter {
    private static final String TAG = "Qiaoba." +RouterInterpreter.class.getSimpleName();

    private static Context mContext;

    private Map<Class<?>, Object> mStubMap = new HashMap<>();
    private Map<Class<?>, InvocationHandler> mInvocationHandlerMap = new HashMap<>();

//    private static Map<String, String> mRouterLinkMap = new HashMap<>();

    private static Map<String, Class<? extends Activity>> mActivityRouterMap = new HashMap<>();

    public static void init(Context context){
        mContext = context;
        loadRouterlinkDatas();
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
        handler = findHandler(routerClazz, 0, null);
        if(handler == null){
            throw new RuntimeException("errpr!! router uri findHandler() is null!");
        }
        Object routerStub = Proxy.newProxyInstance(routerClazz.getClassLoader(), new Class[]{routerClazz}, handler);
        mStubMap.put(routerClazz, routerStub);
        return (T) routerStub;
    }

    /**
     * startActvitiyForResult()
     * @param routerClazz
     * @param requestCode
     * @param <T>
     * @return
     */
    public <T> T create(Class<T> routerClazz, int requestCode, Context context){
        if(mStubMap.get(routerClazz) != null){
            return (T) mStubMap.get(routerClazz);
        }

        InvocationHandler handler = null;
        handler = findHandler(routerClazz, requestCode, context);
        if(handler == null){
            throw new RuntimeException("error!! router uri findHandler() is null!");
        }
        Object routerStub = Proxy.newProxyInstance(routerClazz.getClassLoader(), new Class[]{routerClazz}, handler);
        mStubMap.put(routerClazz, routerStub);
        return (T) routerStub;
    }

    private InvocationHandler findHandler(Class<?> routerClazz, final int requestCode, final Context context) {
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

                    openRouterUri(sb.toString(), requestCode, context instanceof Activity ? (Activity)context : null);

                }
                return null;
            }
        };
        mInvocationHandlerMap.put(routerClazz, handler);
        return handler;
    }


    public void openRouterUri(String routerUri){
        openRouterUri(routerUri, 0, null);
    }


    public void openRouterUri(String routerUri, int requestCode, Activity activity) {
        Uri uri = Uri.parse(routerUri);
        PackageManager packageManager = mContext.getPackageManager();
        Intent uriIntent = new Intent(Intent.ACTION_VIEW, uri);
        uriIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        List<ResolveInfo> activitys = packageManager.queryIntentActivities(uriIntent, 0);
        if(activitys.size() > 0){
            if(requestCode > 0 && activity != null){
                activity.startActivityForResult(uriIntent,requestCode);
            }else {
                mContext.startActivity(uriIntent);
            }
        }else {
            openFromRouterLinkUri(routerUri, requestCode, activity);
        }
    }

    private void openFromRouterLinkUri(String routerUri, int requestCode, Activity activity) {
        Uri uri = Uri.parse(routerUri);
        if(uri != null){
            String routerKey = routerUri;
            if(routerUri.contains("?")){
                routerKey = routerKey.substring(0, routerKey.indexOf("?"));
            }
            Class activityClazz = mActivityRouterMap.get(routerKey);
            if(activityClazz != null) {
                Intent intent = new Intent(mContext, activityClazz);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Set<String> queryParameterNames = uri.getQueryParameterNames();
                if (queryParameterNames != null && queryParameterNames.size() > 0) {
                    for (String key : queryParameterNames) {
                        intent.putExtra(key, uri.getQueryParameter(key));
                    }
                }
                if(requestCode > 0 && activity != null){
                    activity.startActivityForResult(intent, requestCode);
                }else {
                    mContext.startActivity(intent);
                }
            }else {
                Log.e(TAG, "the router uri can't find it's Activity, please check the router uri!!");
            }
//            if(!TextUtils.isEmpty(activityClassName)){
//                try {
//                    Class activityClazz = Class.forName(activityClassName);
//
//                } catch (ClassNotFoundException e) {
//                    Log.e(TAG, "qiaoba create the activity class name is wrong!!");
//                    e.printStackTrace();
//                }
//            }else {
//                Log.e(TAG, "the router uri can't find it's Activity, please check the router uri!!");
//            }
        }else {
            Log.e(TAG, "the router uri is not the right uri, please check your router uri!!");
        }
    }


    /**
     * 加载router link 数据
     */
    private static void loadRouterlinkDatas() {

        try {
            Class routerLinkUtilClazz = Class.forName(DataClassCreator.getActvivityRouterInitalizerClassName());
            if(routerLinkUtilClazz != null){
                IActivityRouterInitalizer activityRouterInitalizer = (IActivityRouterInitalizer) routerLinkUtilClazz.getConstructor().newInstance();
                activityRouterInitalizer.initRouterTable(mActivityRouterMap);
                if(mActivityRouterMap.size() <= 0){
                    Log.e(TAG, "router link activity's is 0");
                }

            }
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "router link activity's is 0");
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {//创建实例异常
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }catch (ClassCastException e){//类型转化异常 （自动生成的类不是 对应的接口实现类）
            e.printStackTrace();
        }

    }

    private static boolean isValidURI(String uri) {
        if (uri == null || uri.indexOf(' ') >= 0 || uri.indexOf('\n') >= 0) {
            return false;
        }
        String scheme = Uri.parse(uri).getScheme();
        if (scheme == null) {
            return false;
        }

        // Look for period in a domain but followed by at least a two-char TLD
        // Forget strings that don't have a valid-looking protocol
        int period = uri.indexOf('.');
        if (period >= uri.length() - 2) {
            return false;
        }
        int colon = uri.indexOf(':');
        if (period < 0 && colon < 0) {
            return false;
        }
        if (colon >= 0) {
            if (period < 0 || period > colon) {
                // colon ends the protocol
                for (int i = 0; i < colon; i++) {
                    char c = uri.charAt(i);
                    if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z')) {
                        return false;
                    }
                }
            } else {
                // colon starts the port; crudely look for at least two numbers
                if (colon >= uri.length() - 2) {
                    return false;
                }
                for (int i = colon + 1; i < colon + 3; i++) {
                    char c = uri.charAt(i);
                    if (c < '0' || c > '9') {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
