package com.xiaoxiao.qiaoba.protocol.model;

import com.xiaoxiao.qiaoba.annotation.model.DependencyInfo;
import com.xiaoxiao.qiaoba.protocol.exception.ModuleNameNullException;
import com.xiaoxiao.qiaoba.protocol.factory.DenpendencyFactory;
import com.xiaoxiao.qiaoba.protocol.utils.Constant;
import com.xiaoxiao.qiaoba.protocol.utils.Logger;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import com.xiaoxiao.qiaoba.protocol.utils.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Created by wangfei on 2016/12/7.
 */

public class DataClassCreator {


     private static final String ACTIVITY_CLASS_NAME = "android.app.Activity";
     private static final String ACTIVITY_ROUTER_INITALIZER_INTERFACE_CLASS_NAME = "com.xiaoxiao.qiaoba.interpreter.router.IActivityRouterInitalizer";


     private Logger mLogger;

     public DataClassCreator(Logger logger){
          mLogger = logger;
     }


     public static String getClassNameForPackageName(String simpleName){
          return Constant.CREATE_PROVIDER_PACKAGE_NAME + "." + simpleName;
     }

     public static String getActvivityRouterInitalizerClassName(){
          return Constant.createRouterLinkPackageName + "." + Constant.createRouterLinkSimpleName;
     }

     public void generateProviderCode(Elements elementUtils, Filer filer, ElementHolder elementHolder){

          TypeSpec providerStub = TypeSpec.classBuilder(elementHolder.getValueName())
                  .addModifiers(Modifier.PUBLIC)
                  .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build())
                  .addField(FieldSpec.builder(Class.class, "value")
                          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                          .initializer("$T.class", ClassName.get(elementHolder.getTypeElement()) )
                          .build())
                  .build();

          JavaFile javaFile = JavaFile.builder(Constant.CREATE_PROVIDER_PACKAGE_NAME, providerStub).build();
          try {
               javaFile.writeTo(filer);
          } catch (IOException e) {
               mLogger.warning("provider : " + elementHolder.getValueName() + " class write to file fail");
               e.printStackTrace();
          }
     }

     public void generateRouterLinkCode(Elements elementUtils, Filer filer, Collection<ElementHolder> elementHolders){
          if(elementHolders.size() > 0){
               TypeElement activityElement = elementUtils.getTypeElement(ACTIVITY_CLASS_NAME);
               TypeElement activityRouterInterfaceElement = elementUtils.getTypeElement(ACTIVITY_ROUTER_INITALIZER_INTERFACE_CLASS_NAME);

               //参数类型为Map<String, Class<? extend Acvtivity>>
               ParameterizedTypeName mapTypeName = ParameterizedTypeName.get(ClassName.get(Map.class),
                       ClassName.get(String.class), ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ClassName.get(activityElement))));
               ParameterSpec routerMapParam = ParameterSpec.builder(mapTypeName, "routerMap").build();
               MethodSpec.Builder initRouterTableMethodBuilder = MethodSpec.methodBuilder("initRouterTable")
                       .addModifiers(Modifier.PUBLIC)
                       .addAnnotation(Override.class)
                       .addParameter(routerMapParam);

               for(ElementHolder holder : elementHolders){
                    initRouterTableMethodBuilder.addStatement("routerMap.put($S, $T.class)", holder.getValueName(),
                            ClassName.get(holder.getTypeElement()));
               }
               TypeSpec.Builder routerLinkBuilder = TypeSpec.classBuilder(Constant.createRouterLinkSimpleName)
                       .addSuperinterface(ClassName.get(activityRouterInterfaceElement))
                       .addModifiers(Modifier.PUBLIC)
                       .addMethod(initRouterTableMethodBuilder.build());

               JavaFile javaFile = JavaFile.builder(Constant.createRouterLinkPackageName, routerLinkBuilder.build()).build();

               try {
                    javaFile.writeTo(filer);
               } catch (IOException e) {
                    System.out.println("router link write to file fail!!");
                    e.printStackTrace();
               }
          }else {
               System.out.println("router link uri activity's size is 0");
          }
     }

     public void generateCallbackImpCode(Elements elementUtils, Filer filer, ElementHolder elementHolder){
          if(elementHolder == null){
               return;
          }
          if(elementHolder.getClazzName() == null || "".equals(elementHolder.getClazzName())){

               return;
          }
          TypeSpec.Builder callbackBuilder = TypeSpec.classBuilder(elementHolder.getValueName())
                  .addModifiers(Modifier.PUBLIC)
                  .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build())
                  .addField(FieldSpec.builder(Class.class, "value")
                          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                          .initializer("$T.class", ClassName.get(elementHolder.getTypeElement()))
                          .build());

//          List<? extends Element> enclosedElements = elementHolder.getTypeElement().getEnclosedElements();//获取子元素
//          for (Element element : enclosedElements) {
//               if(element instanceof ExecutableElement) {
//                    ExecutableElement executableElement = (ExecutableElement) element;
//                    MethodSpec.Builder builder = MethodSpec.methodBuilder(executableElement.getSimpleName().toString())
//                            .addModifiers(Modifier.PUBLIC)
//                            .returns(ClassName.get(executableElement.getReturnType()));
//                    for (VariableElement variableElement : executableElement.getParameters()) {
//                         builder.addParameter(ClassName.get(variableElement.asType()), variableElement.getSimpleName().toString());
//                    }
//                    callbackBuilder.addMethod(builder.build());
//               }
//          }

          JavaFile javaFile = JavaFile.builder(Constant.CREATE_CALLBACK_OF_COMMUNICATION_PACKAGE_NAME, callbackBuilder.build()).build();
          try {
               javaFile.writeTo(filer);
          } catch (IOException e) {
               mLogger.warning("Callback imp class write to file fail!!");
               e.printStackTrace();
          }
     }

     public void generateDenpendencyCode(Elements elements, Filer filer, Collection<DiElementHolder> elementHolders, String moduleName){
          if(elementHolders == null && elementHolders.size() <= 0){
               return;
          }
          if(StringUtils.isEmpty(moduleName)){
               throw new ModuleNameNullException();
          }

          ParameterizedTypeName paramTypeName = ParameterizedTypeName.get(ClassName.get(Map.class),
                  ClassName.get(String.class), ClassName.get(DependencyInfo.class));
          MethodSpec.Builder loadMethodBuilder = MethodSpec.methodBuilder("loadDenpendency")
                  .addModifiers(Modifier.PUBLIC)
                  .addAnnotation(Override.class)
                  .addParameter(ParameterSpec.builder(paramTypeName, "datas").build());
          for (DiElementHolder elementHolder : elementHolders){
               loadMethodBuilder.addStatement("datas.put($S,$T.build($S, $T.class, "+String.valueOf(elementHolder.isSingleInstance())+"))", elementHolder.getValueName(),ClassName.get(DependencyInfo.class),
                       elementHolder.getValueName(), ClassName.get(elementHolder.getTypeElement()));
          }
          TypeSpec typeSpec = TypeSpec.classBuilder(Constant.DENPENDENCY_CLASS_NAME + Constant.CLASS_NAME_SEPARATE + moduleName)
                  .addModifiers(Modifier.PUBLIC)
                  .addSuperinterface(ClassName.get(DenpendencyFactory.class))
                  .addMethod(loadMethodBuilder.build())
                  .build();
          JavaFile javaFile = JavaFile.builder(Constant.DENPENDENCY_PACKAGE_NAME, typeSpec).build();
          try {
               javaFile.writeTo(filer);
          } catch (IOException e) {
               e.printStackTrace();
          }
     }


     public static Class getValueFromClass(Class callerClazz) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
          Field valueField = callerClazz.getDeclaredField("value");
          valueField.setAccessible(true);
          return (Class) valueField.get(callerClazz.newInstance());
     }

     public static Class getCommunicationCallbackClassName(String simpleName) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException {
          Class stubClazz = Class.forName(Constant.CREATE_CALLBACK_OF_COMMUNICATION_PACKAGE_NAME + "." + simpleName);
          Field valueField = stubClazz.getDeclaredField("value");
          valueField.setAccessible(true);
          return (Class) valueField.get(stubClazz.newInstance());
     }

     public static String getCreateClassPackageName(){
          return Constant.CREATE_CLASS_PACKAGE_NAME;
     }

     public static String getDependencyUtilsStartName(){
          return Constant.DENPENDENCY_PACKAGE_NAME;
     }
}
