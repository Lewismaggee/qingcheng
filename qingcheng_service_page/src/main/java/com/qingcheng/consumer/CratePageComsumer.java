package com.qingcheng.consumer;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import java.util.Map;

public class CratePageComsumer implements MessageListener {
    public void onMessage(Message message) {
        String jsonString = new String(message.getBody());

        Map map = JSON.parseObject(jsonString, Map.class);
        String spuId = (String) map.get("spuId");
        System.out.println("spuId======"+spuId);
        //根据id生成thymeleaf静态页面
        //调用
    }
}
