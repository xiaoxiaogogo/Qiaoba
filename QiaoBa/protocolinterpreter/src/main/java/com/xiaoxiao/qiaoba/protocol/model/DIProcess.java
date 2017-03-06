package com.xiaoxiao.qiaoba.protocol.model;

import com.google.auto.service.AutoService;
import com.google.common.collect.Sets;
import com.xiaoxiao.qiaoba.annotation.di.Dependency;
import com.xiaoxiao.qiaoba.protocol.exception.AnnotationTargetUseException;
import com.xiaoxiao.qiaoba.protocol.exception.AnnotationValueNullException;
import com.xiaoxiao.qiaoba.protocol.utils.Constant;
import com.xiaoxiao.qiaoba.protocol.utils.Logger;
import com.xiaoxiao.qiaoba.protocol.utils.StringUtils;

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
 * Created by wangfei on 2017/2/28.
 */
@AutoService(Processor.class)
public class DIProcess extends AbstractProcessor {

    private Filer mFiler;//文件相关的辅助类
    private Elements mElementUtils;//元素相关的辅助类
    private Messager mMessager;//日志相关的辅助类
    private Logger mLogger;
    private String moduleName;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();
        mElementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
        mLogger = new Logger(mMessager);
        Map<String, String> options = processingEnv.getOptions();
        if(options != null && options.size() > 0){
            moduleName = options.get(Constant.KEY_MODULE_NAME);
        }
        mLogger.info("module name  is : " + moduleName);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(Dependency.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedOptions() {
        return Sets.newHashSet(Constant.KEY_MODULE_NAME);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, DiElementHolder> denpendencyHolders = collectClassInfo(roundEnv, Dependency.class, ElementKind.CLASS);
        if(denpendencyHolders != null && denpendencyHolders.keySet().size() > 0) {
            new DataClassCreator(mLogger).generateDenpendencyCode(mElementUtils, mFiler, denpendencyHolders.values(), moduleName);
        }
        return true;
    }


    private Map<String , DiElementHolder> collectClassInfo(RoundEnvironment roundEnv, Class<? extends Annotation> clazz, ElementKind kind){
        Map<String, DiElementHolder> map = new HashMap<>();
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(clazz);
        for (Element element : elements){
            if(element.getKind() != kind){
                throw new AnnotationTargetUseException(element.getSimpleName() + "'s annotation Denpendency must be on a " + kind.name());
            }

            try {
                TypeElement typeElement = (TypeElement) element;
                Annotation annotation = typeElement.getAnnotation(clazz);
                Method method = clazz.getDeclaredMethod("value");
                method.setAccessible(true);
                Object value =  method.invoke(annotation);
                Method isSingleInstanceMethod = clazz.getDeclaredMethod("isSingleInstance");
                isSingleInstanceMethod.setAccessible(true);
                Object isSingleInstance = isSingleInstanceMethod.invoke(annotation);
                String clazzName = typeElement.getQualifiedName().toString();
                String simpleName = typeElement.getSimpleName().toString();
                if(Dependency.class.equals(clazz)){
                    if (!(value instanceof String) || StringUtils.isEmpty((String)value)) {
                        throw new AnnotationValueNullException(element.getSimpleName() + "'s " + clazz.getSimpleName() + " annotation's value is null!!");
                    }
                    map.put((String) value, new DiElementHolder(typeElement, (String) value, (Boolean) isSingleInstance, clazzName, simpleName));
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
}
