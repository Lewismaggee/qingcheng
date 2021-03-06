package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.business.Ad;
import com.qingcheng.service.business.AdService;
import com.qingcheng.service.goods.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Reference
    private AdService adService;
    @Reference
    private CategoryService categoryService;

    @GetMapping("/index")
    public String index(Model model){
        //1.首页轮播图
        List<Ad> index_lbt = adService.findByPosition("web_index_lb");
        model.addAttribute("lbt",index_lbt);

        //2.查询商品分类
        List<Map> categoryList = categoryService.findCategoryTree();
        model.addAttribute("categoryList",categoryList);
        return "index";
    }
}
