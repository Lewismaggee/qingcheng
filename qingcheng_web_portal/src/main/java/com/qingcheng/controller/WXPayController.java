package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.order.Order;
import com.qingcheng.service.order.OrderService;
import com.qingcheng.service.order.WXPayService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/wxpay")
public class WXPayController {
    @Reference
    private WXPayService wxPayService;
    @Reference
    private OrderService orderService;
    @GetMapping("/createNative")
    public Map createNative(String orderId){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Order order = orderService.findById(orderId);
        if(order != null){
            //判断订单状态和支付状态是否符合条件: 有效订单+未支付
            if("0".equals(order.getPayStatus()) && "0".equals(order.getOrderStatus()) && username.equals(order.getUsername())){
                Map map = wxPayService.createNative(orderId, order.getPayMoney(), "http://qicheng.easy.echosite.cn/wxpay/notify.do");
                return map;
            }else{
                return null;
            }
        }else{
            return null;
        }
    }

    /**
     * 支付成功回调
     */
    @RequestMapping("/notify")
    public Map notifyLogic(HttpServletRequest request){
        System.out.println("pay success back notify");
        InputStream inputStream;
        try {
            inputStream = request.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int len = 0;
            while((len = inputStream.read(buff))!=-1){
                outputStream.write(buff,0,len);
            }
            outputStream.close();
            inputStream.close();
            String result = new String(outputStream.toByteArray(),"utf-8");
            System.out.println(result);
            wxPayService.notifyLogic(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap();
    }
}
