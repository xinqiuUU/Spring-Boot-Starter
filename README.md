# 自定义Spring Boot Starter:

## 什么是Spring Boot Starter？

**Spring Boot Starter是一组提供了特定功能的依赖项集合，旨在简化Spring Boot应用的配置。每个Starter都包含了一系列相关的库和自动配置类，以便开发者可以通过引入Starter来减少繁琐的手动配置。**

## 工作原理

**当Spring Boot应用启动时，它会查找类路径中所有的`spring.factories`文件，并加载指定的自动配置类。通过使用`@EnableAutoConfiguration`注解，Spring Boot会根据类路径中存在的依赖自动配置Beans，从而简化了配置过程。**

## 本项目中包含:

**连接数据库的bean(DBHelper)和邮件的bean(JavaMailSender)**
