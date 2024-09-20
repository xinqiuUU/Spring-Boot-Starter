package com.yc.commons;


import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * 此DbProperties继承 自  Properties,所以它也是  Map,也是一个键值对
 * 但增的功能是 ,此DbProperties必须是单例
 */
@ConfigurationProperties(prefix = "db")
public class DbProperties{
    private String driver;
    private String url;
    private String username;
    private String password;

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
