package com.qingcheng.consumer;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

public class RemoveEsConsumer implements MessageListener {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public void onMessage(Message message) {
        System.out.println(new String(message.getBody()));


    }
}
