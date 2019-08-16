package com.qingcheng.service.order;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.Order;
import com.qingcheng.pojo.order.OrderGoods;
import com.qingcheng.pojo.order.OrderItem;

import java.util.*;

/**
 * order业务逻辑层
 */
public interface OrderService {


    public List<Order> findAll();


    public PageResult<Order> findPage(int page, int size);


    public List<Order> findList(Map<String,Object> searchMap);


    public PageResult<Order> findPage(Map<String,Object> searchMap,int page, int size);


    public Order findById(String id);

    public Map<String,Object> add(Order order);


    public void update(Order order);


    public void delete(String id);

    public OrderGoods findOrderGoodsById(String id);

    public List<Order> findOrderList(Map<String,Object> searchMap);

    public void batchDeleverOrders(List<Order> orders);

    public void orderTimeOutLogic();

    public void mergeOrder(String pid,String fid);

    public void splitOrder(List<OrderItem> orderItems,String orderId);

    /**
     * 更新订单支付状态
     * @param orderId 订单号
     * @param transactionId 交易流水号
     */
    public void updatePayStatus(String orderId,String transactionId);

    /**
     * 调用微信支付关闭订单
     * @param orderId
     */
    public void closeOrderLogic(String orderId);

}
