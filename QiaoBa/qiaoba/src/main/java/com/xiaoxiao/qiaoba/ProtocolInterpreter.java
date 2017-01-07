package com.xiaoxiao.qiaoba;

import android.text.TextUtils;

import com.protocol.annotation.communication.CallbackParam;
import com.qiaoba.protocol.model.DataClassCreator;
import com.xiaoxiao.qiaoba.factory.BeanFactory;
import com.xiaoxiao.qiaoba.factory.DefaultBeanFactory;

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
        String simpleName = stubClazz.getSimpleName();
        Class callerClazz = Class.forName(DataClassCreator.getClassNameForPackageName(simpleName));//caller stub class
        String value = DataClassCreator.getValueFromClass(callerClazz);//value存储的是provider的代理类
        Class providerStubClazz = Class.forName(DataClassCreator.getClassNameForPackageName(value));

        String realClassName = DataClassCreator.getValueFromClass(providerStubClazz);

        if(realClassName == null || "".equals(realClassName) || "null".equals(realClassName)){
            throw new RuntimeException("error, the real class is null");
        }

        final Class realClazz = Class.forName(realClassName);
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
                Class[] realClazzParamTypes = method.getParameterTypes();
                Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                for (int i =0 ; i< parameterAnnotations.length; i++){
                    Annotation[] annos = parameterAnnotations[i];
                    if(annos != null && annos.length > 0){
                        if(annos[0] instanceof CallbackParam){
                            CallbackParam callbackParamAnno = (CallbackParam) annos[0];
                            String communicationCallbackClassName = DataClassCreator.getCommunicationCallbackClassName(callbackParamAnno.value());
                            Class<?> communicationCallbackClazz = Class.forName(communicationCallbackClassName);
                            realClazzParamTypes[i] = communicationCallbackClazz;
                            //这里可以使用缓存，来提升性能
                            Object callbackProxyInstant = createCallbackProxyInstant(communicationCallbackClazz, args[i]);
                            args[i] = callbackProxyInstant;
                        }
                    }
                }
                Method realMethod = realClazz.getDeclaredMethod(method.getName(), realClazzParamTypes);
                realMethod.setAccessible(true);
                return realMethod.invoke(finalInstant, args);
            }
        };
        mInvocationHandlerMap.put(stubClazz, handler);
        return handler;
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