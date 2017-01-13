package com.xiaoxiao.qiaoba.protocol.model;

import com.xiaoxiao.qiaoba.protocol.exception.CallerAndProviderMethodNotMatchException;
import com.xiaoxiao.qiaoba.annotation.communication.CallBack;
import com.xiaoxiao.qiaoba.annotation.communication.Caller;
import com.xiaoxiao.qiaoba.annotation.communication.Provider;
import com.google.auto.service.AutoService;
import com.xiaoxiao.qiaoba.protocol.exception.CallerNotMatchProviderException;
import com.xiaoxiao.qiaoba.protocol.utils.Logger;
import com.xiaoxiao.qiaoba.protocol.utils.ProcessUtils;

import java.util.LinkedHashSet;
import java.util.List;
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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * Created by wangfei on 2016/12/6.
 */

@AutoService(Processor.class)//使用 Google 的 auto-service 库可以自动生成 META-INF/services/javax.annotation.processing.Processor 文件
public class ProtocolProcesser extends AbstractProcessor {

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
        types.add(Provider.class.getCanonicalName());
        types.add(Caller.class.getCanonicalName());
        types.add(CallBack.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // (下面需要进行判断 caller 和 provider的接口原形是否相同的校验，不同的话，需要在编译器爆出异常)
        Map<String, ElementHolder> providerMap = ProcessUtils.collectClassInfo(roundEnv, Provider.class, ElementKind.CLASS);

        //create caller stub class(校验 caller 的value不能为null)
        Map<String, ElementHolder> callerMap = ProcessUtils.collectClassInfo(roundEnv, Caller.class, ElementKind.INTERFACE);
//
//
        DataClassCreator classCreator = new DataClassCreator(mLogger);
//        if(callerMap.keySet().size() > 0) {
//            for (String value : callerMap.keySet()) {
//                classCreator.generateCode(mElementUtils, mFiler, callerMap.get(value), true);
//            }
//        }else {
//            mLogger.info("The size of interface using Caller annotation is 0.");
//        }

        //create provider stub class
        if(providerMap.keySet().size() > 0){
            for (String value : providerMap.keySet()){
                classCreator.generateProviderCode(mElementUtils, mFiler, providerMap.get(value));
            }
        }else {
            mLogger.info("The size of class using Provider annotation is 0.");
        }


        Map<String, ElementHolder> callbackMap = ProcessUtils.collectClassInfo(roundEnv, CallBack.class, ElementKind.INTERFACE);
        if(callbackMap.size() > 0){
            for(String key : callbackMap.keySet()){
                classCreator.generateCallbackImpCode(mElementUtils, mFiler, callbackMap.get(key));
            }
        }else {
            mLogger.info("communication of mudules 's callback interface's size is 0");
        }

        return true;
    }

    private void validProviderAndCallerMethod(Map<String, ElementHolder> providerMap, Map<String, ElementHolder> callerMap) {
        for (Map.Entry<String, ElementHolder> entry : callerMap.entrySet()){
            mLogger.error("provider size : " + providerMap.size());
            for (String key : providerMap.keySet()){
                mLogger.error("provider value : " + key);
            }
            if(providerMap.get(entry.getKey()) == null){
                throw new CallerNotMatchProviderException("The Caller's value is " + entry.getKey() + ", it's target is "
                    + entry.getValue().getClazzName());
            }
            TypeElement callerElement = entry.getValue().getTypeElement();
            TypeElement providerElement = providerMap.get(entry.getKey()).getTypeElement();
            for (Element element : callerElement.getEnclosedElements()){
                if(element instanceof ExecutableElement){
                    ExecutableElement methodElement = (ExecutableElement) element;
                    for (Element pElement : providerElement.getEnclosedElements()){
                        if(pElement instanceof ExecutableElement){
                            ExecutableElement pMethodElement = (ExecutableElement) pElement;
                            if(pMethodElement.getSimpleName().equals(methodElement.getSimpleName())){
                                List<? extends TypeParameterElement> typeParameters = methodElement.getTypeParameters();
                                List<? extends TypeParameterElement> pTypeParameters = pMethodElement.getTypeParameters();
                                List<? extends VariableElement> parameters = methodElement.getParameters();
                                StringBuilder sb = new StringBuilder();
                                if(typeParameters.size() != pTypeParameters.size()){//param's size not equal
                                    sb.append("Caller's target : " + callerElement.getQualifiedName() + ", Provider's target is " +
                                        providerElement.getQualifiedName() + "; their's method [" + methodElement.getSimpleName() +
                                            "]'s param's size not match. \n" );
                                    sb.append("Caller's method is " +methodElement.getSimpleName() + "(");
                                    for (int i=0; i< typeParameters.size(); i++){
                                        TypeParameterElement tpe = typeParameters.get(i);
                                        sb.append(tpe.getSimpleName());
                                        if(i < typeParameters.size() -1){
                                            sb.append(", ");
                                        }
                                    }
                                    sb.append(").\n");
                                    sb.append("Provider's method is " + pMethodElement.getSimpleName() + "(");
                                    for (int i=0; i< pTypeParameters.size(); i++){
                                        TypeParameterElement tpe = pTypeParameters.get(i);
                                        sb.append(tpe.getSimpleName());
                                        if(i < pTypeParameters.size() -1){
                                            sb.append(", ");
                                        }
                                    }
                                    sb.append(").");
                                    throw new CallerAndProviderMethodNotMatchException(sb.toString());
                                }
                                for (int i=0; i< typeParameters.size(); i++){
                                    mLogger.error(typeParameters.get(i).getSimpleName());

                                }
                            }
                        }
                    }
                }
            }
        }
    }


}
