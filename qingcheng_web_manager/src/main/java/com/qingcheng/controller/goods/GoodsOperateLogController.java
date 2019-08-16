package com.qingcheng.controller.goods;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.PageResult;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.goods.GoodsOperateLog;
import com.qingcheng.service.goods.GoodsOperateLogService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/goodsOperateLog")
public class GoodsOperateLogController {

    @Reference
    private GoodsOperateLogService goodsOperateLogService;

    @GetMapping("/findAll")
    public List<GoodsOperateLog> findAll(){
        return goodsOperateLogService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult<GoodsOperateLog> findPage(int page, int size){
        return goodsOperateLogService.findPage(page, size);
    }

    @PostMapping("/findList")
    public List<GoodsOperateLog> findList(@RequestBody Map<String,Object> searchMap){
        return goodsOperateLogService.findList(searchMap);
    }

    @PostMapping("/findPage")
    public PageResult<GoodsOperateLog> findPage(@RequestBody Map<String,Object> searchMap,int page, int size){
        return  goodsOperateLogService.findPage(searchMap,page,size);
    }

    @GetMapping("/findById")
    public GoodsOperateLog findById(String id){
        return goodsOperateLogService.findById(id);
    }


    @PostMapping("/add")
    public Result add(@RequestBody GoodsOperateLog goodsOperateLog){
        goodsOperateLogService.add(goodsOperateLog);
        return new Result();
    }

    @PostMapping("/update")
    public Result update(@RequestBody GoodsOperateLog goodsOperateLog){
        goodsOperateLogService.update(goodsOperateLog);
        return new Result();
    }

    @GetMapping("/delete")
    public Result delete(String id){
        goodsOperateLogService.delete(id);
        return new Result();
    }

}
