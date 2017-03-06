package com.xiaoxiao.qiaoba.interpreter;

import android.text.TextUtils;
import android.util.Log;

import com.xiaoxiao.qiaoba.annotation.communication.CallbackParam;
import com.xiaoxiao.qiaoba.annotation.communication.Caller;
import com.xiaoxiao.qiaoba.interpreter.exception.AnnotationNotFoundException;
import com.xiaoxiao.qiaoba.interpreter.exception.CallerCallbackMethodNotMatch;
import com.xiaoxiao.qiaoba.interpreter.exception.ProviderMethodNotFoundException;
import com.xiaoxiao.qiaoba.interpreter.exception.ProviderStubClassNotFoundException;
import com.xiaoxiao.qiaoba.interpreter.protocol.ProtocolCallback;
import com.xiaoxiao.qiaoba.protocol.exception.AnnotationValueNullException;
import com.xiaoxiao.qiaoba.interpreter.exception.ProviderNotFoundException;
import com.xiaoxiao.qiaoba.protocol.model.DataClassCreator;
import com.xiaoxiao.qiaoba.interpreter.factory.BeanFactory;
import com.xiaoxiao.qiaoba.interpreter.factory.DefaultBeanFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangfei on 2016/12/20.
 */

public class ProtocolInterpreter {

    private Map<Class<?>, Object> mCallerBeanMap = new HashMap<>();
    private Map<Class<?>, InvocationHandler> mInvocationHandlerMap = new HashMap<>();

    private List<BeanFactory> mBeanFactorys = new ArrayList<>();
    private DefaultBeanFactory mDefaultFactory;

    private boolean mIsEnableCheckMethod;

    private ProtocolCallback mCallback;

    static class Holder{
        static ProtocolInterpreter instance = new ProtocolInterpreter();
    }

    private ProtocolInterpreter(){
        mDefaultFactory = new DefaultBeanFactory();
        mBeanFactorys.add(mDefaultFactory);

    }

    public static ProtocolInterpreter getInstance(){
        return Holder.instance;
    }

    public <T> T create(Class<T> stubClazz){
        return create(stubClazz, null);
    }


    public <T> T create(Class<T> stubClazz, ProtocolCallback callback){
        if(mCallerBeanMap.get(stubClazz) != null){
            return (T) mCallerBeanMap.get(stubClazz);
        }
        InvocationHandler handler = null;

        handler = findHandler(stubClazz, callback);
        if(handler == null){
            return  null;
        }
        T result = (T) Proxy.newProxyInstance(stubClazz.getClassLoader(), new Class[]{stubClazz}, handler);
        mCallerBeanMap.put(stubClazz, result);
        return result;
    }



    private <T> InvocationHandler findHandler(final Class<T> stubClazz, final ProtocolCallback callback) {
        if(mInvocationHandlerMap.get(stubClazz) != null){
            return mInvocationHandlerMap.get(stubClazz);
        }
        Caller caller = stubClazz.getAnnotation(Caller.class);
        if(caller == null){
            //抛出异常， 没有此注解
            if(callback != null) {
                callback.onError(new AnnotationNotFoundException(stubClazz.getCanonicalName() + " need the Caller annotation."));
            }
            return null;
        }

        if(!TextUtils.isEmpty(caller.value())){
            Class providerStubClazz = null;
            try {
                providerStubClazz = Class.forName(DataClassCreator.getClassNameForPackageName(caller.value()));
            } catch (ClassNotFoundException e) {
                if(callback != null) {
                    callback.onError(new ProviderStubClassNotFoundException("provider stub class(" + caller.value() + ") not found！please check caller annotation's value is same as the provider annotation's value!"));
                }
            }
            if(providerStubClazz == null){
                return  null;
            }

            Class realClazz = null;
            try {
                realClazz = DataClassCreator.getValueFromClass(providerStubClazz);
            } catch (Exception e) {
                if(callback != null) {
                    callback.onError(new ProviderNotFoundException("provider real class can't be found! caller's annptation'value is " + caller.value()));
                }
            }

            final  Class finalRealClazz = realClazz;

            if(finalRealClazz == null || "".equals(finalRealClazz.getCanonicalName())){
                return  null;
            }

//            final Class realClazz = Class.forName(realClassName);
            Object realInstant = null;
            if(mBeanFactorys.size() > 0){
                for (BeanFactory beanFactory : mBeanFactorys){
                    realInstant = beanFactory.getBean(finalRealClazz);
                    if(realInstant != null){
                        break;
                    }
                }
            }
            if(realInstant == null){
                realInstant = createDefaultRealInstant(finalRealClazz);//创建无参的实例（默认的方式）
            }

            if(realInstant == null){
                if(callback != null) {
                    callback.onError(new ProviderNotFoundException("create provider instant error!"));
                }
                return null;
            }
            final Object finalInstant = realInstant;
            InvocationHandler handler = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) {
                    try {
                        Class[] callClazzParamTypes = method.getParameterTypes();
                        for (int i = 0; i < callClazzParamTypes.length; i++) {
                            if (isClassWithAnnotation(callClazzParamTypes[i], CallbackParam.class)) {
                                String callbackValue = getClassAnnotationValue(callClazzParamTypes[i], CallbackParam.class);
                                Class<?> communicationCallbackClazz = DataClassCreator.getCommunicationCallbackClassName(callbackValue);
                                //这里可以使用缓存，来提升性能
                                Object callbackProxyInstant = createCallbackProxyInstant(communicationCallbackClazz, args[i], callClazzParamTypes[i], callback);
                                callClazzParamTypes[i] = communicationCallbackClazz;
                                args[i] = callbackProxyInstant;
                                break;
                            }
                        }
                        Method realMethod = finalRealClazz.getDeclaredMethod(method.getName(), callClazzParamTypes);
                        if (realMethod == null) {
                            //方法名或者参数的类型错误
                            if(callback != null) {
                                callback.onError(new ProviderMethodNotFoundException("Please check your method name and the parameters' type!!\n" +
                                    "caller's class is " + stubClazz + "\n" +
                                    "method is " + method.toString()));
                            }
                        }else {
                            realMethod.setAccessible(true);
                            return realMethod.invoke(finalInstant, args);
                        }
                    }catch (Exception e){
                        if(callback != null) {
                            callback.onError(new ProviderMethodNotFoundException("Please check your method name and the parameters' type!!\n" +
                                    "caller's class is " + stubClazz + "\n" +
                                    "method is " + method.toString()));
                        }
                    }
                    return null;
                }
            };
            mInvocationHandlerMap.put(stubClazz, handler);
            return handler;
        }else {
            //抛出异常， Caller注解的值不能为空
            if(callback != null) {
                callback.onError(new AnnotationValueNullException(stubClazz.getCanonicalName() + "'s Caller annotation's value can't be null."));
            }
            return null;
        }
    }

    private String getClassAnnotationValue(Class callClazz, Class annoClazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if(callClazz.getAnnotation(annoClazz) != null){
            Annotation anno = callClazz.getAnnotation(annoClazz);
            Method value = annoClazz.getDeclaredMethod("value");
            value.setAccessible(true);
            return (String)value.invoke(anno);
        }else {
            for(Class c : callClazz.getInterfaces()){
                String value = getClassAnnotationValue(c, annoClazz);
                if(value != null){
                    return value;
                }
            }
            return null;
        }
    }

    /**
     * 判断
     * @param callClazz
     * @param annoClazz
     * @return
     */
    private boolean isClassWithAnnotation(Class callClazz, Class annoClazz) {
        if(callClazz.isAnnotationPresent(annoClazz)){
            return true;
        }else {
            for (Class c : callClazz.getInterfaces()){
                if(isClassWithAnnotation(c, annoClazz)){
                    return true;
                }
            }
            return false;
        }
    }

    private Object createCallbackProxyInstant(Class<?> communicationCallbackClazz, final Object callbackImp, final Class callerCallBackClazz, final ProtocolCallback callback) {
        if(callbackImp == null){
            return  null;
        }
        return Proxy.newProxyInstance(communicationCallbackClazz.getClassLoader(), new Class[]{communicationCallbackClazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                //保证方法名相同，并且参数相同才行
                Class[] argClazzs = new Class[args == null ? 0 :args.length];
                if(args != null) {
                    for (int i = 0; i < args.length; i++) {
                        argClazzs[i] = args[i].getClass();
                    }
                }
                try {
                    Method callbackImpMethod = callbackImp.getClass().getMethod(method.getName(), argClazzs);
                    if (callbackImpMethod != null) {
                        return callbackImpMethod.invoke(callbackImp, args);
                    }else {
                        if(callback != null) {
                            callback.onError(new CallerCallbackMethodNotMatch("Please check caller's callback's method name and arguments' type is right!!\n"
                                    + "caller callback class is " + callerCallBackClazz ));
                        }
                    }
                }catch (Exception e){
                    if(callback != null) {
                        callback.onError(new CallerCallbackMethodNotMatch("Please check caller's callback's method name and arguments' type is right!!\n"
                                + "caller callback class is " + callerCallBackClazz ));
                    }
                }
                return null;
            }
        });
    }

    /**
     * 创建无参的构造函数
     * @param realClassName
     * @return
     */
    private Object createDefaultRealInstant(Class realClassName) {
        try {
            return realClassName.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void checkMethod(Class stubClazz, Class targetClazz){
        if(!mIsEnableCheckMethod){
            return;
        }

        try{
            List<String> listCheckMethodResult = new ArrayList<>();
            Method[] stubMethods = stubClazz.getMethods();
            Method[] targetMethods = targetClazz.getMethods();
            if(stubMethods != null && stubMethods.length > 0){
                if(targetMethods == null || targetMethods.length <= 0){
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 添加自定义的 目标业务调用类的实例（也就是Provider具体实现类的创建方式，比如创建的时候，传入参数等等）
     * 注意： 这个必须要create()方法之前调用，也就是在创建实例之前被调用，不然就会采用默认的BeanFactory创建
     * @param beanFactory
     */
    public void addBeanFactory(BeanFactory beanFactory){
        mBeanFactorys.add(beanFactory);
    }


}
