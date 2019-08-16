package com.qingcheng.controller.goods;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.PageResult;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.goods.GoodsAuditLog;
import com.qingcheng.service.goods.GoodsAuditLogService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/goodsAuditLog")
public class GoodsAuditLogController {

    @Reference
    private GoodsAuditLogService goodsAuditLogService;

    @GetMapping("/findAll")
    public List<GoodsAuditLog> findAll(){
        return goodsAuditLogService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult<GoodsAuditLog> findPage(int page, int size){
        return goodsAuditLogService.findPage(page, size);
    }

    @PostMapping("/findList")
    public List<GoodsAuditLog> findList(@RequestBody Map<String,Object> searchMap){
        return goodsAuditLogService.findList(searchMap);
    }

    @PostMapping("/findPage")
    public PageResult<GoodsAuditLog> findPage(@RequestBody Map<String,Object> searchMap,int page, int size){
        return  goodsAuditLogService.findPage(searchMap,page,size);
    }

    @GetMapping("/findById")
    public GoodsAuditLog findById(String id){
        return goodsAuditLogService.findById(id);
    }


    @PostMapping("/add")
    public Result add(@RequestBody GoodsAuditLog goodsAuditLog){
        goodsAuditLogService.add(goodsAuditLog);
        return new Result();
    }

    @PostMapping("/update")
    public Result update(@RequestBody GoodsAuditLog goodsAuditLog){
        goodsAuditLogService.update(goodsAuditLog);
        return new Result();
    }

    @GetMapping("/delete")
    public Result delete(String id){
        goodsAuditLogService.delete(id);
        return new Result();
    }

}
