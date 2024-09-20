package com.yc.util;

import com.yc.commons.DbProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;

public class SystemInfoUtil implements ISystemInfoUtil{

    @Autowired
    private SystemInfoPro systemInfoPro;

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired
    private DbProperties p;


    @Override
    public String getAppInfo() {
        return systemInfoPro.getCopyrigeht()+"\n" + systemInfoPro.getAuthor();
    }

    @Override
    public String getSystemInfo() {
        return environment.getProperty("os.name")+"\t" + environment.getProperty("user.home");
    }

//    @Override
//    public DBHelper getDBHelper() {
//        return new DBHelper(p);
//    }

    //@Autowired

}
