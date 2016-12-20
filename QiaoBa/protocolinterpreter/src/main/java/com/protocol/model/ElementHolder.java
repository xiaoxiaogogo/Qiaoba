package com.protocol.model;

import javax.lang.model.element.TypeElement;

/**
 * Created by wangfei on 2016/12/7.
 */

public class ElementHolder {

    private TypeElement typeElement;//被注解的类的element
    private String valueName;//注解中的值
    private String clazzName;//被注解类的全名
    private String simpleName;//被注解类的 简称

    public ElementHolder(TypeElement typeElement, String valueName, String clazzName, String simpleName) {
        this.typeElement = typeElement;
        this.valueName = valueName;
        this.clazzName = clazzName;
        this.simpleName = simpleName;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public String getValueName() {
        return valueName;
    }

    public String getClazzName() {
        return clazzName;
    }

    public String getSimpleName() {
        return simpleName;
    }
}
