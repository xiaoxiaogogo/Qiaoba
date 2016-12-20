package com.protocol.model;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;

/**
 * Created by wangfei on 2016/12/7.
 */

public class DataClassCreator {


     public void generateCode(Elements elementUtils, Filer filer, ElementHolder elementHolder, boolean isCaller){
          if(isCaller) {
               TypeSpec callStub = TypeSpec.classBuilder(elementHolder.getSimpleName())
                       .addModifiers(Modifier.PUBLIC)
                       .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build())
                       .addField(FieldSpec.builder(String.class, "value")
                               .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                               .initializer(elementHolder.getValueName())
                               .build()).build();

               JavaFile javaFile = JavaFile.builder("com.protocol.data", callStub).build();
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
                               .initializer(elementHolder.getClazzName())
                               .build())
                       .build();

               JavaFile javaFile = JavaFile.builder("com.protocol.data", providerStub).build();
               try {
                    javaFile.writeTo(filer);
               } catch (IOException e) {
                    System.out.println("provider : " + elementHolder.getValueName() + " class write to file fail");
                    e.printStackTrace();
               }
          }
     }
}
