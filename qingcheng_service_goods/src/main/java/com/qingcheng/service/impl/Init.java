package com.qingcheng.service.impl;

import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SkuService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 该类实现InitializingBean: 项目启动时会自动调用
 * */
@Component
public class Init /*implements InitializingBean*/ {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SkuService skuService;

    public void afterPropertiesSet() throws Exception {
        System.out.println("..............缓存预热,加载商品分类数据进缓存..............");
        categoryService.saveCategoryTreeToRedis(); //加载商品分类导航数据
        skuService.saveAllPriceToRedis(); //加载价格数据
    }
}
