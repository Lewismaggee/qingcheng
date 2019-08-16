package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.goods.BrandService;
import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SpecService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/*
    定时任务
 */
@Component
public class OrderTask {

    @Reference
    private BrandService brandService;

    @Reference
    private CategoryService categoryService;

    @Reference
    private SpecService specService;

    @Scheduled(cron = "0 0 24 * * ?")
    public void saveBrandAndSpecDataToRedis(){
        System.out.println("save brand and spec data to redis .....");
        brandService.saveAllBrandToRedis();
        specService.saveAllToRedis();
    }
}
