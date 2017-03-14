package com.xiaoxiao.qiaoba.protocol.utils;

import com.xiaoxiao.qiaoba.annotation.communication.CallBack;
import com.xiaoxiao.qiaoba.annotation.router.FragmentLinkUri;
import com.xiaoxiao.qiaoba.protocol.exception.AnnotationTargetUseException;
import com.xiaoxiao.qiaoba.protocol.exception.AnnotationValueNullException;
import com.xiaoxiao.qiaoba.protocol.model.ElementHolder;
import com.xiaoxiao.qiaoba.annotation.communication.Caller;
import com.xiaoxiao.qiaoba.annotation.communication.Provider;
import com.xiaoxiao.qiaoba.annotation.router.RouterLinkUri;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Created by wangfei on 2017/1/11.
 */

public class ProcessUtils {

    public static Map<String , ElementHolder> collectClassInfo(RoundEnvironment roundEnv, Class<? extends Annotation> clazz,
                                                               ElementKind kind, Logger logger){
        Map<String, ElementHolder> map = new HashMap<>();
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(clazz);
        for (Element element : elements){
            if(element.getKind() != kind){
                throw new AnnotationTargetUseException(element.getSimpleName() + "'s annotation must be on a " + kind.name());
            }

            try {
                TypeElement typeElement = (TypeElement) element;
                Annotation annotation = typeElement.getAnnotation(clazz);
                Method method = clazz.getDeclaredMethod("value");
                method.setAccessible(true);
                Object value =  method.invoke(annotation);
                String clazzName = typeElement.getQualifiedName().toString();
                String simpleName = typeElement.getSimpleName().toString();
                if( Caller.class.equals(clazz) || RouterLinkUri.class.equals(clazz) || CallBack.class.equals(clazz) || FragmentLinkUri.class.equals(clazz)) {
                    if (!(value instanceof String) || StringUtils.isEmpty((String)value)) {
                        throw new AnnotationValueNullException(element.getSimpleName() + "'s " + clazz.getSimpleName() + " annotation's value is null!!");
                    }
                    map.put((String) value, new ElementHolder(typeElement, (String) value, clazzName, simpleName));
                }else if(Provider.class.equals(clazz)){
                    if(value == null || !(value instanceof  String[])){
                        throw new AnnotationValueNullException(element.getSimpleName() + "'s " + clazz.getSimpleName() + " annotation's value is null!!");
                    }
                    String[] vals = (String[]) value;
                    for (int i=0; i< vals.length; i++){
                        if(StringUtils.isEmpty(vals[i])){
                            throw new AnnotationValueNullException(element.getSimpleName() + "'s " + clazz.getSimpleName() +
                                " annotation's number " + i + " value is null.");
                        }
                        //过滤掉重复的值
                        if (!isRepeat(vals[i], i, vals)){
                            map.put(vals[i], new ElementHolder(typeElement, vals[i], clazzName, simpleName));
                        }
                    }
                }

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return map;
    }

    private static boolean isRepeat(String val, int currentPos, String[] vals) {
        for(int i =0; i< currentPos; i++){
            if(vals[i] == val){
                return true;
            }
        }
        return false;
    }


}
