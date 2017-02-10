package com.xiaoxiao.qiaoba.interpreter;

import android.text.TextUtils;

import com.xiaoxiao.qiaoba.annotation.communication.CallbackParam;
import com.xiaoxiao.qiaoba.annotation.communication.Caller;
import com.xiaoxiao.qiaoba.interpreter.exception.AnnotationNotFoundException;
import com.xiaoxiao.qiaoba.interpreter.exception.ProviderMethodNotFoundException;
import com.xiaoxiao.qiaoba.protocol.exception.AnnotationValueNullException;
import com.xiaoxiao.qiaoba.protocol.model.DataClassCreator;
import com.xiaoxiao.qiaoba.interpreter.factory.BeanFactory;
import com.xiaoxiao.qiaoba.interpreter.factory.DefaultBeanFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
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
        if(mCallerBeanMap.get(stubClazz) != null){
            return (T) mCallerBeanMap.get(stubClazz);
        }
        InvocationHandler handler = null;
        try {
            handler = findHandler(stubClazz);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("error!! findHandler error!!");
        }
        T result = (T) Proxy.newProxyInstance(stubClazz.getClassLoader(), new Class[]{stubClazz}, handler);
        mCallerBeanMap.put(stubClazz, result);
        return result;
    }

    private <T> InvocationHandler findHandler(Class<T> stubClazz) throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        if(mInvocationHandlerMap.get(stubClazz) != null){
            return mInvocationHandlerMap.get(stubClazz);
        }
        Caller caller = stubClazz.getAnnotation(Caller.class);
        if(caller == null){
            //抛出异常， 没有此注解
            throw new AnnotationNotFoundException(stubClazz.getCanonicalName() + " need the Caller annotation.");
        }

        if(!TextUtils.isEmpty(caller.value())){
            Class providerStubClazz = Class.forName(DataClassCreator.getClassNameForPackageName(caller.value()));

            final Class realClazz = DataClassCreator.getValueFromClass(providerStubClazz);

            if(realClazz == null || "".equals(realClazz.getCanonicalName())){
                throw new RuntimeException("error, the real class is null");
            }

//            final Class realClazz = Class.forName(realClassName);
            Object realInstant = null;
            if(mBeanFactorys.size() > 0){
                for (BeanFactory beanFactory : mBeanFactorys){
                    realInstant = beanFactory.getBean(realClazz);
                    if(realInstant != null){
                        break;
                    }
                }
            }
            if(realInstant == null){
                realInstant = createDefaultRealInstant(realClazz);//创建无参的实例（默认的方式）
            }

            if(realInstant == null){
                throw new RuntimeException("error!! the target provider instant is null, no param construct don't have, please " +
                        "provider your customer beanFactory");
            }
            final Object finalInstant = realInstant;
            InvocationHandler handler = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    Class[] callClazzParamTypes = method.getParameterTypes();
                    //callback handle
                    Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                    for (int i =0 ; i< parameterAnnotations.length; i++){
                        Annotation[] annos = parameterAnnotations[i];
                        if(annos != null && annos.length > 0){
                            if(annos[0] instanceof CallbackParam){
                                CallbackParam callbackParamAnno = (CallbackParam) annos[0];
                                Class<?> communicationCallbackClazz = DataClassCreator.getCommunicationCallbackClassName(callbackParamAnno.value());
                                callClazzParamTypes[i] = communicationCallbackClazz;
                                //这里可以使用缓存，来提升性能
                                Object callbackProxyInstant = createCallbackProxyInstant(communicationCallbackClazz, args[i]);
                                args[i] = callbackProxyInstant;
                            }
                        }
                    }
                    Method realMethod = realClazz.getDeclaredMethod(method.getName(), callClazzParamTypes);
                    if(realMethod == null){
                        //方法名或者参数的类型错误
                        throw new ProviderMethodNotFoundException("please check your method name and the parameters' type");
                    }
                    realMethod.setAccessible(true);
                    return realMethod.invoke(finalInstant, args);
                }
            };
            mInvocationHandlerMap.put(stubClazz, handler);
            return handler;
        }else {
            //抛出异常， Caller注解的值不能为空
            throw new AnnotationValueNullException(stubClazz.getCanonicalName() + "'s Caller annotation's value can't be null.");
        }
    }

    private Object createCallbackProxyInstant(Class<?> communicationCallbackClazz, final Object callbackImp) {
        if(callbackImp == null){
            return  null;
        }
        return Proxy.newProxyInstance(communicationCallbackClazz.getClassLoader(), new Class[]{communicationCallbackClazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //保证方法名相同，并且参数相同才行
                Class[] argClazzs = new Class[args == null ? 0 :args.length];
                if(args != null) {
                    for (int i = 0; i < args.length; i++) {
                        argClazzs[i] = args[i].getClass();
                    }
                }
                Method callbackImpMethod = callbackImp.getClass().getMethod(method.getName(), argClazzs);
                if(callbackImpMethod != null){
                    return callbackImpMethod.invoke(callbackImp, args);
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
