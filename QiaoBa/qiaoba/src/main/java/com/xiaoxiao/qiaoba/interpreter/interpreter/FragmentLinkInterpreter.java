package com.xiaoxiao.qiaoba.interpreter.interpreter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.xiaoxiao.qiaoba.interpreter.Qiaoba;
import com.xiaoxiao.qiaoba.interpreter.exception.FragmentInstanceException;
import com.xiaoxiao.qiaoba.interpreter.exception.FragmentLinkParamNullPointException;
import com.xiaoxiao.qiaoba.interpreter.exception.FragmentResIdException;
import com.xiaoxiao.qiaoba.interpreter.exception.FragmentTypeException;
import com.xiaoxiao.qiaoba.interpreter.exception.ParentActivityTypeException;
import com.xiaoxiao.qiaoba.interpreter.exception.RouterUriException;
import com.xiaoxiao.qiaoba.interpreter.callback.FragmentLinkCallback;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangfei on 2017/3/14.
 */

public class FragmentLinkInterpreter {

    public static Map<String, Class> FRAGMENT_LINK_MAP = new HashMap<>();

    public Object getFragmentFromLink(String routerUri){
        return getFragmentFromLink(routerUri, null);
    }

    public Object getFragmentFromLink(String routerUri, FragmentLinkCallback callback){
        return getFragmentFromLink(new Builder(routerUri), callback);
    }

    public Object getFragmentFromLink(Builder builder){
        return getFragmentFromLink(builder, null);
    }

    public Object getFragmentFromLink(Builder builder, FragmentLinkCallback callback){
        if(builder == null){
            if(callback != null){
                //抛出异常

            }
            return null;
        }
        Class clazz = FRAGMENT_LINK_MAP.get(builder.mUri);
        if(clazz != null){
            if(Fragment.class.isAssignableFrom(clazz) ){
                try {
                    Object obj = clazz.newInstance();
                    Fragment fragment = (Fragment) obj;
                    fragment.setArguments(builder.mArgBundle);
                    return fragment;
                } catch (Exception e) {
                    e.printStackTrace();
                    if(callback != null){
                        callback.onError(new FragmentInstanceException("Create Fragment with FragmentLinkUri("+builder.mUri+") Instance fail!!" +
                                "please checkout the fragment's constructor with no parameter."));
                    }
                }
            }else if(android.app.Fragment.class.isAssignableFrom(clazz)){
                try {
                    Object obj = clazz.newInstance();
                    android.app.Fragment fragment = (android.app.Fragment) obj;
                    fragment.setArguments(builder.mArgBundle);
                    return fragment;
                } catch (Exception e) {
                    e.printStackTrace();
                    if(callback != null){
                        callback.onError(new FragmentInstanceException("Create Fragment with FragmentLinkUri("+builder.mUri+") Instance fail!!" +
                                "please checkout the fragment's constructor with no parameter."));
                    }
                }
            }else {
                if(callback != null){
                    callback.onError(new RouterUriException("The router uri can't find the fragment! please check if the fragment has " +
                            "Annotation FragmentLinkUri("+builder.mUri+")"));
                }
                return null;
            }

        }else {
            if(callback != null){
                callback.onError(new RouterUriException("The router uri can't find the fragment! please check if the fragment has " +
                        "Annotation FragmentLinkUri("+builder.mUri+")"));
            }
        }
        return null;
    }

    public void linkFragment(String routerUri,  int resId, Activity parentActivity){
        this.linkFragment(routerUri, resId, parentActivity, false);
    }
    public void linkFragment(String routerUri,  int resId, Activity parentActivity, FragmentLinkCallback callback){
        this.linkFragment(routerUri, resId, parentActivity, false, Builder.FRAGMENT_REPLACE, callback);
    }

    public void linkFragment(String routerUri,  int resId, Activity parentActivity, boolean isAllowStateLoss){
        this.linkFragment(routerUri, resId, parentActivity, isAllowStateLoss, Builder.FRAGMENT_REPLACE);
    }
    public void linkFragment(String routerUri,  int resId, Activity parentActivity, boolean isAllowStateLoss, FragmentLinkCallback callback){
        this.linkFragment(routerUri, resId, parentActivity, isAllowStateLoss, Builder.FRAGMENT_REPLACE, callback);
    }

    public void linkFragment(String routerUri,  int resId, Activity parentActivity, boolean isAllowStateLoss, @Builder.ShowStyle int showStyle){
        this.linkFragment(routerUri, resId, parentActivity, isAllowStateLoss, showStyle, null);
    }

    public void linkFragment(String routerUri, int resId, Activity parentActivity, boolean isAllowStateLoss, @Builder.ShowStyle int showStyle, FragmentLinkCallback callback){
        build(routerUri)
                .resId(resId)
                .parentActivity(parentActivity)
                .isAllowStateLoss(isAllowStateLoss)
                .showStyle(showStyle)
                .callback(callback)
                .linkFragment();

    }


    public void linkFragment(Builder builder){
        if(builder != null){
            FragmentLinkCallback callback = builder.mCallback;
            if(builder.mParentActivity == null){
                if(callback != null){
                    callback.onError(new FragmentLinkParamNullPointException("The parent Activity is null!! It can't be null!"));
                }
                return;
            }
            if(TextUtils.isEmpty(builder.mUri)){
                if(callback != null){
                    callback.onError(new RouterUriException("The router uri is empty!! It can't be empty!"));
                }
                return;
            }
            Object fragment = getFragmentFromLink(builder, callback);
            if(fragment != null){
                if(fragment instanceof android.app.Fragment){
                    android.app.FragmentTransaction fragmentTransaction = builder.mParentActivity.getFragmentManager().beginTransaction();
                    try {
                        if (builder.mShowStyle == Builder.FRAGMENT_ADD) {
                            fragmentTransaction.add(builder.mResId, (android.app.Fragment) fragment);
                        } else {
                            fragmentTransaction.replace(builder.mResId, (android.app.Fragment) fragment);
                        }
                    }catch (Exception e){
                        if(callback != null){
                            callback.onError(new FragmentResIdException());
                        }
                    }
                    if(builder.mIsAllowStateLoss){
                        fragmentTransaction.commitAllowingStateLoss();
                    }else {
                        fragmentTransaction.commit();
                    }
                }else if(fragment instanceof Fragment){
                    if(!(builder.mParentActivity instanceof FragmentActivity)){
                        if(callback != null){
                            callback.onError(new ParentActivityTypeException("The Fragment is android.support.v4.app.Fragment," +
                                    "but the Activit starting it is not FragmentActivity!!"));
                        }
                        return;
                    }
                    FragmentTransaction fragmentTransaction = ((FragmentActivity) builder.mParentActivity).getSupportFragmentManager().beginTransaction();
                    try {
                        if (builder.mShowStyle == Builder.FRAGMENT_ADD) {
                            fragmentTransaction.add(builder.mResId, (Fragment) fragment);
                        } else {
                            fragmentTransaction.replace(builder.mResId, (Fragment) fragment);
                        }
                    }catch (Exception e){
                        if(callback != null){
                            callback.onError(new FragmentResIdException());
                        }
                    }
                    if(builder.mIsAllowStateLoss){
                        fragmentTransaction.commitAllowingStateLoss();
                    }else {
                        fragmentTransaction.commit();
                    }
                }else {
                    if(callback != null){
                        callback.onError(new FragmentTypeException("The Annotation FragmentLinkUri("+builder.mUri+") used class is not A Fragment!!!"));
                    }
                }
            }else {
                if(callback != null){
                    callback.onError(new RouterUriException("The router uri can't find the fragment! please check if the fragment has " +
                            "Annotation FragmentLinkUri("+builder.mUri+")"));
                }
            }
        }
    }

    public static Builder build(String routerUri){
        return new Builder(routerUri);
    }

    public static class Builder{
        public static final int FRAGMENT_REPLACE = 0;
        public static final int FRAGMENT_ADD = 1;

        @IntDef({FRAGMENT_ADD, FRAGMENT_REPLACE})
        @Retention(RetentionPolicy.SOURCE)
        public @interface ShowStyle{}

        private String mUri;
        private Bundle mArgBundle;
        private int mResId;
        private Activity mParentActivity;
        private boolean mIsAllowStateLoss;
        private @ShowStyle int mShowStyle = FRAGMENT_REPLACE;
        private FragmentLinkCallback mCallback;

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

        public Builder callback(FragmentLinkCallback callback){
            mCallback = callback;
            return this;
        }

        public Builder resId(int resId){
            mResId = resId;
            return this;
        }

        public Builder parentActivity(Activity activity){
            mParentActivity = activity;
            return this;
        }

        public Builder isAllowStateLoss(boolean isAllowStateLoss){
            mIsAllowStateLoss = isAllowStateLoss;
            return this;
        }

        public Builder showStyle(@ShowStyle int showStyle){
            mShowStyle = showStyle;
            return  this;
        }

        public Object getFragment(){
           return Qiaoba.getInstance().getFragmentLinkInterpreter().getFragmentFromLink(this, mCallback);
        }

        public void linkFragment(){
            Qiaoba.getInstance().getFragmentLinkInterpreter().linkFragment(this);
        }

    }

}
