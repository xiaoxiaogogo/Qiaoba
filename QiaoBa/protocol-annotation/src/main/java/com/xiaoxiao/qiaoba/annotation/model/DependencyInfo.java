package com.xiaoxiao.qiaoba.annotation.model;

/**
 * Created by wangfei on 2017/3/1.
 */

public class DependencyInfo {

    private String key;

    private Class denendencyClazz;

    private boolean isSingleInstance;

    public DependencyInfo(String key, Class denendencyClazz, boolean isSingleInstance) {
        this.key = key;
        this.denendencyClazz = denendencyClazz;
        this.isSingleInstance = isSingleInstance;
    }

    public boolean isSingleInstance() {
        return isSingleInstance;
    }

    public void setSingleInstance(boolean singleInstance) {
        isSingleInstance = singleInstance;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Class getDenendencyClazz() {
        return denendencyClazz;
    }

    public void setDenendencyClazz(Class denendencyClazz) {
        this.denendencyClazz = denendencyClazz;
    }

    public static DependencyInfo build(String key, Class denendencyClazz, boolean isSingleInstance){
        return new DependencyInfo(key, denendencyClazz, isSingleInstance);
    }
}
