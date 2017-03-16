# Qiaoba
解决Android组件化后，各组件间通信的问题。
处理问题：1、使用路由的方式启动页面  2、组件间业务逻辑api的跨组件间调用

**支持功能**

- 支持跨组件通过路由启动页面(支持自定义路由的方式)
- 支持Builder模式设置跳转页面需要传递的参数，启动Activity的flag，设置requestCode以及启动页面回调的支持（onSucess,onError)
- 借鉴了Retrofit使用外观模式+动态代理的方式，使用这种外观模式实现解耦和java面向对象方式实现路由启动页面
- 增加Router和跨组件api调用的结果回调支持
- 增加依赖注入，降低组件之间的耦合（这里指的是可以引用到组件，引用不到的只能使用下面提供的跨组件api调用）
**下面的功能是目前业界没有实现过的，只有本框架才有**

- 支持跨组件间的业务逻辑api的调用
- 跨组件业务api的调用，支持api方法的回调参数的支持
- 支持单业务api被多个组件调用（业务提供者和调用者1对多的支持）

**引用的版本随时在更新，由于修改bug，升级功能等都可能升版本，所以如果使用本框架，请持续关心更新动态**

``` java
//引入方式方式
compile 'com.xiaoxiao.qiaoba:qiaoba:1.0.7' //主要实现业务逻辑的模块(解决了在Android5.0跨组件api调用异常)
apt 'com.xiaoxiao.qiaoba:protocol-interpreter:1.0.5'//apt编译期处理代码
compile 'com.xiaoxiao.qiaoba:protocol-annotation:1.0.5'//apt使用的注解
```

混淆规则的配置如下：
``` java
//proguard配置
-keep class com.xiaoxiao.qiaoba.**{*;}
-keep @com.xiaoxiao.qiaoba.annotation.communication.Provider class *{
     <methods>;
}
-keep @com.xiaoxiao.qiaoba.annotation.communication.Caller class *{
    <methods>;
}
-keep @com.xiaoxiao.qiaoba.annotation.communication.CallBack class *{
    <methods>;
}
-keep @com.xiaoxiao.qiaoba.annotation.communication.CallbackParam class *{
    <methods>;
}
```

[接入使用详解地址](http://blog.csdn.net/u010014658/article/details/53791067)
欢迎支持
