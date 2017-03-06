package com.xiaoxiao.qiaoba.interpreter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.xiaoxiao.qiaoba.interpreter.exception.AnnotationNotFoundException;
import com.xiaoxiao.qiaoba.interpreter.exception.RouterUriException;
import com.xiaoxiao.qiaoba.interpreter.router.RouterCallback;
import com.xiaoxiao.qiaoba.protocol.model.DataClassCreator;
import com.xiaoxiao.qiaoba.annotation.router.RouterParam;
import com.xiaoxiao.qiaoba.annotation.router.RouterUri;
import com.xiaoxiao.qiaoba.interpreter.router.IActivityRouterInitalizer;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
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

    private Map<String, Object> mStubMap = new HashMap<>();
    private Map<String, InvocationHandler> mInvocationHandlerMap = new HashMap<>();

    private static Map<String, Class<? extends Activity>> mActivityRouterMap = new HashMap<>();

    public static void init(Context context){
        mContext = context;
        DenpendencyDemo demo = new DenpendencyDemo();
        demo.test();
        loadRouterlinkDatas();
    }

    private RouterInterpreter(){}

    static class Holder{
        public static RouterInterpreter instance = new RouterInterpreter();
    }

    public static RouterInterpreter getInstance(){
        return Holder.instance;
    }

    public <T> T create(@NonNull Class<T> routerClazz){
        return create(routerClazz, 0, null);
    }

    public <T> T create(@NonNull Class<T> routerClazz, int requestCode, Context context){
        return create(routerClazz, requestCode, context, null);
    }

    /**
     * startActvitiyForResult()
     * @param routerClazz
     * @param requestCode
     * @param <T>
     * @return
     */
    public <T> T create(@NonNull Class<T> routerClazz, int requestCode, Context context, RouterCallback callback){
        if(routerClazz == null){
            if(callback != null){
                callback.onError(new NullPointerException("routerClazz can't be null!"));
            }
            return null;
        }
        if(mStubMap.get(requestCode >0 ?(routerClazz.getCanonicalName() + requestCode) : routerClazz.getCanonicalName()) != null){
            return (T) mStubMap.get(requestCode >0 ?(routerClazz.getCanonicalName() + requestCode) : routerClazz.getCanonicalName());
        }

        InvocationHandler handler = null;
        handler = findHandler(routerClazz, requestCode, context, callback);
        if(handler == null){
            if(callback != null){
                callback.onError(new AnnotationNotFoundException("please check whether you use the annotation RouterUri!"));
            }
            return null;
        }
        Object routerStub = Proxy.newProxyInstance(routerClazz.getClassLoader(), new Class[]{routerClazz}, handler);
        mStubMap.put(requestCode >0 ? (routerClazz.getCanonicalName() + requestCode) : routerClazz.getCanonicalName(), routerStub);
        return (T) routerStub;
    }

    private InvocationHandler findHandler(Class<?> routerClazz, final int requestCode, final Context context, final RouterCallback callback) {
        if(mInvocationHandlerMap.get(requestCode >0 ?(routerClazz.getCanonicalName() + requestCode) : routerClazz.getCanonicalName()) != null){
            return mInvocationHandlerMap.get(requestCode >0 ?(routerClazz.getCanonicalName() + requestCode) : routerClazz.getCanonicalName());
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
            if(callback != null){
                callback.onError(new AnnotationNotFoundException("error!! can't find method with annotation RouterUri!"));
            }
            return  null;
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

                    openRouterUri(sb.toString(), requestCode, context instanceof Activity ? (Activity)context : null, callback);

                }
                return null;
            }
        };
        mInvocationHandlerMap.put(requestCode >0 ?(routerClazz.getCanonicalName() + requestCode) : routerClazz.getCanonicalName(), handler);
        return handler;
    }


    public void openRouterUri(String routerUri){
        openRouterUri(routerUri, 0, null, null);
    }

    public void openRouterUri(String routerUri, RouterCallback callback){
        openRouterUri(routerUri, 0, null, callback);
    }

    public void openRouterUri(String routerUri, int requestCode, Activity activity, RouterCallback callback) {
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
            openFromRouterLinkUri(build(routerUri).requestCode(requestCode, activity),callback);
        }
    }

    public void openRouterUri(Builder builder){
        openFromRouterLinkUri(builder, null);
    }

    public void openRouterUri(Builder builder, RouterCallback callback){
        openFromRouterLinkUri(builder, callback);
    }

    private void openFromRouterLinkUri(Builder builder, RouterCallback callback) {
        if(builder == null){
            if(callback != null){
                callback.onError(new NullPointerException("Router Builder is null, please check."));
            }
            return;
        }
        Uri uri = Uri.parse(builder.mUri);
        if(uri != null){
            String routerKey = builder.mUri;
            if(builder.mUri.contains("?")){
                routerKey = routerKey.substring(0, routerKey.indexOf("?"));
            }
            Class activityClazz = mActivityRouterMap.get(routerKey);
            if(activityClazz != null) {
                Intent intent = new Intent(mContext, activityClazz);
                //因为NewTask 对startActivityForResult()有影响
                if(builder.mRequsetCode > 0 && builder.mSourceActivity != null && builder.mFlags == Intent.FLAG_ACTIVITY_NEW_TASK){
                    intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                }else {
                    intent.setFlags(builder.mFlags);
                }
                Bundle bundle = builder.mArgBundle == null ? new Bundle() : builder.mArgBundle;
                Set<String> queryParameterNames = uri.getQueryParameterNames();
                if (queryParameterNames != null && queryParameterNames.size() > 0) {
                    for (String key : queryParameterNames) {
                        bundle.putString(key, uri.getQueryParameter(key));
                    }
                }
                intent.putExtras(bundle);
                if(builder.mRequsetCode > 0 && builder.mSourceActivity != null){
                    builder.mSourceActivity.startActivityForResult(intent, builder.mRequsetCode);
                }else {
                    mContext.startActivity(intent);
                }
                if(callback != null){
                    callback.onSuccess();
                }
            }else {
                if(callback != null){
                    callback.onError(new RouterUriException("the router uri can't find it's Activity, please check the router uri!!"));
                }
            }

        }else {
            if(callback != null){
                callback.onError(new RouterUriException("the router uri is not a valid uri, please check your router uri!!"));
            }
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


    public Builder build(String uri){
        return new Builder(uri);
    }

    public class Builder{
        private String mUri;
        private Bundle mArgBundle;
        private int mFlags = Intent.FLAG_ACTIVITY_NEW_TASK;
        private int mRequsetCode;
        private Activity mSourceActivity;
        private RouterCallback mCallback;

        public Builder(String uri){
            mUri = uri;
        }

        public Builder with(Bundle bundle){
            mArgBundle = bundle;
            return this;
        }

        public Builder withString(String key, String val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putString(key, val);
            return this;
        }

        public Builder withInt(String key, int val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putInt(key, val);
            return this;
        }

        public Builder withBoolean(String key, boolean val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putBoolean(key, val);
            return this;
        }

        public Builder withLong(String key, long val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putLong(key, val);
            return this;
        }

        public Builder withShort(String key, short val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putShort(key, val);
            return this;
        }

        public Builder withSerializable(String key, Serializable val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putSerializable(key, val);
            return this;
        }

        public Builder withByte(String key, byte val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putByte(key, val);
            return this;
        }

        public Builder withByteArray(String key, byte[] val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putByteArray(key, val);
            return this;
        }

        public Builder withChar(String key, char val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putChar(key, val);
            return this;
        }

        public Builder withCharArray(String key, char[] val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putCharArray(key, val);
            return this;
        }

        public Builder withCharSequence(String key, CharSequence val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putCharSequence(key, val);
            return this;
        }

        public Builder withCharSequenceArray(String key, CharSequence[] val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putCharSequenceArray(key, val);
            return this;
        }

        public Builder withIntegerArrayList(String key, ArrayList<Integer> val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putIntegerArrayList(key, val);
            return this;
        }

        public Builder withFloatArray(String key, float[] val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putFloatArray(key,val);
            return this;
        }

        public Builder withShortArray(String key, short[] val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putShortArray(key, val);
            return this;
        }

        public Builder withParcelable(String key, Parcelable val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putParcelable(key,val);
            return this;
        }

        public Builder withParcelableArray(String key, Parcelable[] val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putParcelableArray(key, val);
            return this;
        }

        public Builder withStringArrayList(String key, ArrayList<String> val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putStringArrayList(key, val);
            return this;
        }

        public Builder withBooleanArray(String key, boolean[] val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putBooleanArray(key, val);
            return this;
        }

        public Builder withFloat(String key, float val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putFloat(key, val);
            return this;
        }

        public Builder withDouble(String key, double val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putDouble(key, val);
            return this;
        }

        public Builder withDoubleArray(String key, double[] val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putDoubleArray(key, val);
            return this;
        }

        public Builder withIntArray(String key, int[] val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putIntArray(key, val);
            return this;
        }

        public Builder withLongArray(String key, long[] val){
            if(mArgBundle == null){
                mArgBundle = new Bundle();
            }
            mArgBundle.putLongArray(key, val);
            return this;
        }

        public Builder addFlags(int flags){
            mFlags = flags;
            return this;
        }

        public Builder requestCode(int requestCode, Activity sourceActivity){
            mRequsetCode = requestCode;
            mSourceActivity = sourceActivity;
            return this;
        }

        public Builder callback(RouterCallback callback){
            mCallback = callback;
            return this;
        }

        public void navigation(){
            RouterInterpreter.getInstance().openRouterUri(this, mCallback);
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
