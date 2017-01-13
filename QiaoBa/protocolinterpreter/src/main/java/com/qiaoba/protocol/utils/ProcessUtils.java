package com.qiaoba.protocol.utils;

import com.protocol.annotation.communication.Caller;
import com.protocol.annotation.communication.Provider;
import com.protocol.annotation.router.RouterLinkUri;
import com.qiaoba.protocol.exception.AnnotationValueNullException;
import com.qiaoba.protocol.exception.AnnotationTargetUseException;
import com.qiaoba.protocol.model.ElementHolder;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

/**
 * Created by wangfei on 2017/1/11.
 */

public class ProcessUtils {

    public static Map<String , ElementHolder> collectClassInfo(RoundEnvironment roundEnv, Class<? extends Annotation> clazz, ElementKind kind){
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
                String value = (String) method.invoke(annotation);
                if(Provider.class.equals(clazz) || Caller.class.equals(clazz) || RouterLinkUri.class.equals(clazz)) {
                    if (StringUtils.isEmpty(value)) {
                        throw new AnnotationValueNullException(element.getSimpleName() + "'s " + clazz.getSimpleName() + " annotation's value is null!!");
                    }
                }
                String clazzName = typeElement.getQualifiedName().toString();
                String simpleName = typeElement.getSimpleName().toString();
                map.put(value, new ElementHolder(typeElement, value, clazzName, simpleName));
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
}
