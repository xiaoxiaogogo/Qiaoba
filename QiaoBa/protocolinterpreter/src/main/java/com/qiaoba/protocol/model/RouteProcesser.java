package com.qiaoba.protocol.model;

import com.protocol.annotation.communication.CallBack;
import com.protocol.annotation.communication.Caller;
import com.protocol.annotation.communication.Provider;
import com.protocol.annotation.router.RouterLinkUri;
import com.qiaoba.protocol.utils.Logger;
import com.qiaoba.protocol.utils.ProcessUtils;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Created by wangfei on 2017/1/11.
 */

public class RouteProcesser extends AbstractProcessor {

    private Filer mFiler;//文件相关的辅助类
    private Elements mElementUtils;//元素相关的辅助类
    private Messager mMessager;//日志相关的辅助类
    private Logger mLogger;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();
        mElementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
        mLogger = new Logger(mMessager);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(RouterLinkUri.class.getCanonicalName());
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        DataClassCreator classCreator = new DataClassCreator(mLogger);
        Map<String, ElementHolder> routerLinkUriMap = ProcessUtils.collectClassInfo(roundEnv, RouterLinkUri.class, ElementKind.CLASS);
        if(routerLinkUriMap.keySet().size() > 0){
            classCreator.generateRouterLinkCode(mElementUtils, mFiler, routerLinkUriMap.values());

        }else {
            mLogger.info("The size of Activity using RouterLinkUri annotation is 0.");
        }
        return true;
    }
}
