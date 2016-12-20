package com.protocol.model;

import com.protocol.annotation.Caller;
import com.protocol.annotation.Provider;
import com.google.auto.service.AutoService;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Created by wangfei on 2016/12/6.
 */

@AutoService(Processor.class)//使用 Google 的 auto-service 库可以自动生成 META-INF/services/javax.annotation.processing.Processor 文件
public class ProtocolProcesser extends AbstractProcessor {

    private Filer mFiler;//文件相关的辅助类
    private Elements mElementUtils;//元素相关的辅助类
    private Messager mMessager;//日志相关的辅助类

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();
        mElementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(Provider.class.getCanonicalName());
        types.add(Caller.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //create caller stub class
        Map<String, ElementHolder> callerMap = collectClassInfo(roundEnv, Caller.class, ElementKind.INTERFACE);
        DataClassCreator classCreator = new DataClassCreator();
        if(callerMap.keySet().size() > 0) {
            for (String value : callerMap.keySet()) {
                classCreator.generateCode(mElementUtils, mFiler, callerMap.get(value), true);
            }
        }else {
            System.out.println("caller size is 0");
        }

        //create provider stub class
        Map<String, ElementHolder> providerMap = collectClassInfo(roundEnv, Provider.class, ElementKind.CLASS);
        if(providerMap.keySet().size() > 0){
            for (String value : providerMap.keySet()){
                classCreator.generateCode(mElementUtils, mFiler, providerMap.get(value), false);
            }
        }else {
            System.out.println("provider size is 0");
        }

        return true;
    }

    private Map<String , ElementHolder> collectClassInfo(RoundEnvironment roundEnv, Class<? extends Annotation> clazz, ElementKind kind){
        Map<String, ElementHolder> map = new HashMap<>();
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(clazz);
        for (Element element : elements){
            if(element.getKind() != kind){
                throw new IllegalArgumentException(element.getSimpleName() + "'s annotation must be on a " + kind.name());
            }


            try {
                TypeElement typeElement = (TypeElement) element;
                Annotation annotation = typeElement.getAnnotation(clazz);
                Method method = clazz.getDeclaredMethod("value");
                method.setAccessible(true);
                String value = (String) method.invoke(annotation);
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
