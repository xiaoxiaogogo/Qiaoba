# Qiaoba
解决Android组件化后，各组件间通信的问题。
处理问题：1、使用路由的方式启动页面  2、组件间业务逻辑的调用

``` java
//引入方式方式
compile 'com.xiaoxiao.qiaoba:qiaoba:1.0.2' //主要实现业务逻辑的模块
apt 'com.xiaoxiao.qiaoba:protocol-interpreter:1.0.1'//apt编译期处理代码
compile 'com.xiaoxiao.qiaoba:protocol-annotation:1.0.1'//apt使用的注解
```

