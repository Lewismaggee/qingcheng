package com.qingcheng.seckill.controller;

import com.qingcheng.util.DateUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = "/seckill/goods")
public class SeckillGoodsController {

    /**
     * 加载所有时间菜单
     * @return
     */
        @GetMapping("/menus")
    public List<Date> loadMenus(){
        return DateUtil.getDateMenus();
    }
}
