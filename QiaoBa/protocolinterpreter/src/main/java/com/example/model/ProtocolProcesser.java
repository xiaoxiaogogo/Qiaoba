package com.example.model;

import com.example.annotation.Caller;
import com.example.annotation.Provider;
import com.google.auto.service.AutoService;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
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

        return true;
    }
}
