package com.qiaoba.protocol.model;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;

/**
 * Created by wangfei on 2016/12/7.
 */

public class DataClassCreator {

     private static String createClassPackageName = "com.qiaoba.protocol.data";
     private static String createRouterLinkSimpleName = "RouterLinkUtils";
     private static String createRouterLinkPackageName = "com.qiaoba.protocol.data.routerlink";

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


     public static String getValueFromClass(Class callerClazz) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
          Field valueField = callerClazz.getDeclaredField("value");
          valueField.setAccessible(true);
          return (String) valueField.get(callerClazz.newInstance());
     }
}
