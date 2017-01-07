package com.qiaoba.protocol.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * Created by wangfei on 2016/12/7.
 */

public class DataClassCreator {

     private static final String createClassPackageName = "com.qiaoba.protocol.data";
     private static final String createRouterLinkSimpleName = "RouterLinkUtils";
     private static final String createRouterLinkPackageName = "com.qiaoba.protocol.data.routerlink";
     private static final String CREATE_CALLBACK_OF_COMMUNICATION_PACKAGE_NAME = "com.qiaoba.protocol.data.commucation.callback";

     public static String getClassNameForPackageName(String simpleName){
          return createClassPackageName + "." + simpleName;
     }

     public static String getRouterLinkClassName(){
          return createRouterLinkPackageName + "." + createRouterLinkSimpleName;
     }

     public void generateCode(Elements elementUtils, Filer filer, ElementHolder elementHolder, boolean isCaller){
          if(isCaller) {
               TypeSpec callStub = TypeSpec.classBuilder(elementHolder.getSimpleName())
                       .addModifiers(Modifier.PUBLIC)
                       .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build())
                       .addField(FieldSpec.builder(String.class, "value")
                               .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                               .initializer("$S", elementHolder.getValueName())
                               .build()).build();

               JavaFile javaFile = JavaFile.builder(createClassPackageName, callStub).build();
               try {
                    javaFile.writeTo(filer);
               } catch (IOException e) {
                    System.out.println("caller : " + elementHolder.getSimpleName() + " class write to file fail");
                    e.printStackTrace();
               }
          }else {
               TypeSpec providerStub = TypeSpec.classBuilder(elementHolder.getValueName())
                       .addModifiers(Modifier.PUBLIC)
                       .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build())
                       .addField(FieldSpec.builder(String.class, "value")
                               .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                               .initializer("$S", elementHolder.getClazzName())
                               .build())
                       .build();

               JavaFile javaFile = JavaFile.builder(createClassPackageName, providerStub).build();
               try {
                    javaFile.writeTo(filer);
               } catch (IOException e) {
                    System.out.println("provider : " + elementHolder.getValueName() + " class write to file fail");
                    e.printStackTrace();
               }
          }
     }

     public void generateRouterLinkCode(Elements elementUtils, Filer filer, Collection<ElementHolder> elementHolders){
          if(elementHolders.size() > 0){
               TypeSpec.Builder routerLinkBuilder = TypeSpec.classBuilder(createRouterLinkSimpleName)
                       .addModifiers(Modifier.PUBLIC)
                       .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build());
               for(ElementHolder holder : elementHolders){
                    routerLinkBuilder.addField(FieldSpec.builder(String.class, holder.getSimpleName())
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .initializer("$S+\"|@|\"+$S", holder.getValueName(), holder.getClazzName())
                            .build());
               }
               JavaFile javaFile = JavaFile.builder(createRouterLinkPackageName, routerLinkBuilder.build()).build();
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
                  .addField(FieldSpec.builder(String.class, "value")
                          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                          .initializer("$S", elementHolder.getClazzName())
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

          JavaFile javaFile = JavaFile.builder(CREATE_CALLBACK_OF_COMMUNICATION_PACKAGE_NAME, callbackBuilder.build()).build();
          try {
               javaFile.writeTo(filer);
          } catch (IOException e) {
               System.out.println("Callback imp class write to file fail!!");
               e.printStackTrace();
          }
     }


     public static String getValueFromClass(Class callerClazz) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
          Field valueField = callerClazz.getDeclaredField("value");
          valueField.setAccessible(true);
          return (String) valueField.get(callerClazz.newInstance());
     }

     public static String getCommunicationCallbackClassName(String simpleName) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException {
          Class stubClazz = Class.forName(CREATE_CALLBACK_OF_COMMUNICATION_PACKAGE_NAME + "." + simpleName);
          Field valueField = stubClazz.getDeclaredField("value");
          valueField.setAccessible(true);
          return (String) valueField.get(stubClazz.newInstance());
     }
}
