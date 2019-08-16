package com.qingcheng.consumer;

import com.alibaba.fastjson.JSON;
import com.qingcheng.pojo.order.OrderItem;
import com.qingcheng.service.goods.StockBackService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class BackMessageConsumer implements MessageListener {

    @Autowired
    private StockBackService stockBackService;

    @Override
    public void onMessage(Message message) {
        System.out.println("back message consumer begin +++++++++");
        //读取mq中数据
        try{
            String jsonStr = new String(message.getBody()); //orderItemList
            List<OrderItem> orderItems = JSON.parseArray(jsonStr, OrderItem.class);
            stockBackService.addList(orderItems);
        }catch (Exception e){
            e.printStackTrace();
            //
        }

    }
}
