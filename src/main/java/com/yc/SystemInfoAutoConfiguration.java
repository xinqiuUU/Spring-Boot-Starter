package com.yc;


import com.yc.commons.DBHelper;
import com.yc.commons.DbProperties;
import com.yc.email.EmailProperties;
import com.yc.util.ISystemInfoUtil;
import com.yc.util.SystemInfoPro;
import com.yc.util.SystemInfoUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties({SystemInfoPro.class ,
        DbProperties.class ,
        EmailProperties.class}) //开启配置文件的自动配置
@ConditionalOnClass({SystemInfoPro.class, ISystemInfoUtil.class}) //条件注解 jvm启动时检查是否有SystemInfoPro、ISystemInfoUtil这个类 有就托管
public class SystemInfoAutoConfiguration {

    @Value("${yc.mail.host}")
    public  String host;
    @Value("${yc.mail.port}")
    public int port;
    @Value("${yc.mail.auth}")
    public String auth;
    @Value("${yc.mail.username}")
    public String username;
    @Value("${yc.mail.password}")
    public String password;
    @Value("${yc.mail.from}")
    public String from;

    // 定义一个 JavaMailSender 类型的 Bean，用于发送邮件
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host); // 替换为您的 SMTP 服务器地址
        mailSender.setPort(port); // 替换为您的 SMTP 服务器端口
        mailSender.setUsername(username); // 替换为您的邮箱账号
        mailSender.setPassword(password); // 替换为您的邮箱密码

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        if(port == 587) {
            props.put("mail.smtp.starttls.enable", "true");
        }
        if(port == 465) {
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        }
        //        props.put("mail.debug", "true");
        return mailSender;
    }

    //数据库的bean
    @Bean
    @ConditionalOnClass({java.sql.Driver.class}) //只要有 数据库的驱动程序才托管
    public DBHelper dbHelper(DbProperties p){
        return new DBHelper(p);
    }
    //业务类的bean
    @Bean
    @ConditionalOnMissingBean({ISystemInfoUtil.class}) //如果没有ISystemInfoUtil这个bean,则创建一个SystemInfoUtil对象
    public ISystemInfoUtil systemInfoUtil(){
        System.out.println("实例化SystemInfoUtil对象，并注入到spring容器中");
        return new SystemInfoUtil();
    }

}
