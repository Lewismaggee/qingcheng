package com.qingcheng.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.PageResult;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.order.Order;
import com.qingcheng.pojo.order.OrderGoods;
import com.qingcheng.pojo.order.OrderItem;
import com.qingcheng.service.order.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Reference
    private OrderService orderService;

    @GetMapping("/findAll")
    public List<Order> findAll(){
        return orderService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult<Order> findPage(int page, int size){
        return orderService.findPage(page, size);
    }

    @PostMapping("/findList")
    public List<Order> findList(@RequestBody Map<String,Object> searchMap){
        return orderService.findList(searchMap);
    }

    @PostMapping("/findPage")
    public PageResult<Order> findPage(@RequestBody Map<String,Object> searchMap,int page, int size){
        return  orderService.findPage(searchMap,page,size);
    }

    @GetMapping("/findById")
    public Order findById(String id){
        return orderService.findById(id);
    }


    @PostMapping("/add")
    public Result add(@RequestBody Order order){
        orderService.add(order);
        return new Result();
    }

    @PostMapping("/update")
    public Result update(@RequestBody Order order){
        orderService.update(order);
        return new Result();
    }

    @GetMapping("/delete")
    public Result delete(String id){
        orderService.delete(id);
        return new Result();
    }

    @GetMapping("/findOrderGoodsById")
    public OrderGoods findOrderGoodsById(String id){
        return orderService.findOrderGoodsById(id);
    }

    @PostMapping("/findOrderList")
    public List<Order> findUnConsignedOrders(@RequestBody Map<String,Object> searchMap){
        return orderService.findOrderList(searchMap);
    }

    @PostMapping("/batchDeliverOrders")
    public Result batchDeleverOrders(@RequestBody List<Order> orders){
        orderService.batchDeleverOrders(orders);
        return new Result();
    }

    @GetMapping("/mergeOrders")
    public Result mergeOrders(String pid,String fid){
        orderService.mergeOrder(pid,fid);
        return new Result();
    }

    @PostMapping("/splitOrders")
    public Result splitOrders(@RequestBody List<OrderItem> orderItems,String orderId){
        orderService.splitOrder(orderItems,orderId);
        return new Result();
    }

}
