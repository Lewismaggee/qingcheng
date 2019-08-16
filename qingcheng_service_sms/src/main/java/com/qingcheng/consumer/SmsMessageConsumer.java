package com.qingcheng.consumer;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.CommonResponse;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

/**
 * 监听mq消息类
 */
public class SmsMessageConsumer implements MessageListener {

    @Autowired
    private SmsUtil smsUtil;

    @Value("${smsCode}")
    private String smsCode;

    @Value("${param}")
    private String param;

    public void onMessage(Message message) {
        //1.获取mq上的消息内容 :{"phone":"139********","code":"xxxxxx"}
        String str = new String(message.getBody());
        System.out.println("jsonString ============"+str);
        //2.将mq上的json字符串转换成map
        Map<String,String> map = JSON.parseObject(str, Map.class);
        String phone = map.get("phone");
        String code =  map.get("code");

        System.out.println("手机号: "+phone +" 验证码: "+code);
        //3.调用阿里云通讯平台发送短信
        CommonResponse commonResponse = smsUtil.sendSms(phone, smsCode, param.replace("[value]", code));


    }
}
