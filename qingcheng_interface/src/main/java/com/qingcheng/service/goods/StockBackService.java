package com.qingcheng.service.goods;

import com.qingcheng.pojo.order.OrderItem;

import java.util.List;

public interface StockBackService {
    /**
     * 将插入订单表失败的数据保存到订单回滚表
     * @param orderItemList
     */
    public void addList(List<OrderItem> orderItemList);

    /**
     * 执行库存回滚
     */
    public void doBack();
}
