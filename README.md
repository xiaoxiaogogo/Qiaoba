# Qiaoba
解决Android组件化后，各组件间通信的问题。
处理问题：1、使用路由的方式启动页面  2、组件间业务逻辑的调用

``` java
//引入方式方式
compile 'com.xiaoxiao.qiaoba:qiaoba:1.0.3' //主要实现业务逻辑的模块
apt 'com.xiaoxiao.qiaoba:protocol-interpreter:1.0.3'//apt编译期处理代码
compile 'com.xiaoxiao.qiaoba:protocol-annotation:1.0.3'//apt使用的注解
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

[接入详解地址](http://blog.csdn.net/u010014658/article/details/53791067)
欢迎支持
