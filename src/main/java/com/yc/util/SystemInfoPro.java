package com.yc.util;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yc")
public class SystemInfoPro {
    private String copyrigeht;
    private String author;

    public String getCopyrigeht() {
        return copyrigeht;
    }

    public String getAuthor() {
        return author;
    }

    public void setCopyrigeht(String copyrigeht) {
        this.copyrigeht = copyrigeht;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
