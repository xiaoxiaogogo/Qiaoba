package com.xiaoxiao.qiaoba.protocol.model;

import javax.lang.model.element.TypeElement;

/**
 * Created by wangfei on 2017/2/28.
 */

/**
 * 针对Denpendancy注解生成的数据实体
 */
public class DiElementHolder {

    private TypeElement typeElement;
    private String valueName;
    private boolean isSingleInstance;
    private String className;
    private String simpleName;

    public DiElementHolder(TypeElement typeElement, String valueName, boolean isSingleInstance, String className, String simpleName) {
        this.typeElement = typeElement;
        this.valueName = valueName;
        this.isSingleInstance = isSingleInstance;
        this.className = className;
        this.simpleName = simpleName;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public String getValueName() {
        return valueName;
    }

    public boolean isSingleInstance() {
        return isSingleInstance;
    }

    public String getClassName() {
        return className;
    }

    public String getSimpleName() {
        return simpleName;
    }
}
