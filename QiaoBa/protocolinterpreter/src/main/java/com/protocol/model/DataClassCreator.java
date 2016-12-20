package com.protocol.model;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;

/**
 * Created by wangfei on 2016/12/7.
 */

public class DataClassCreator {

     private static String createClassPackageName = "com.protocol.data";

     public static String getClassNameForPackageName(String simpleName){
          return createClassPackageName + "." + simpleName;
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

     public static String getValueFromClass(Class callerClazz) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
          Field valueField = callerClazz.getDeclaredField("value");
          valueField.setAccessible(true);
          return (String) valueField.get(callerClazz.newInstance());
     }
}
