package com.qingcheng.service.impl;

import com.qingcheng.service.business.AdService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Init /*implements InitializingBean*/ {
    @Autowired
    private AdService adService;
    public void afterPropertiesSet() throws Exception {
        System.out.println("=============加载广告数据进缓存=======");
        adService.saveAllAdToRedis();
    }
}
