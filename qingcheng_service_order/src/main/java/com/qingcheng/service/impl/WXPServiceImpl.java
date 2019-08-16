package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.Config;
import com.github.wxpay.sdk.WXPayRequest;
import com.github.wxpay.sdk.WXPayUtil;
import com.qingcheng.service.order.OrderService;
import com.qingcheng.service.order.WXPayService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
@Service
public class WXPServiceImpl implements WXPayService {
    @Autowired
    private Config config;
    @Autowired
    private OrderService orderService;
    @Override
    public Map createNative(String orderId, Integer money, String notifyUrl) {
        try {
            //封装请求参数
            Map<String,String> map = new HashMap();
            map.put("appid",config.getAppID()); //公众账号id
            map.put("mch_id",config.getMchID()); //商户号
            map.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
            map.put("body","青橙"); //商品描述
            map.put("out_trade_no",orderId); //订单号
            map.put("total_fee",money+""); //金额
            map.put("spbill_create_ip","127.0.0.1"); //终端ip
            map.put("notify_url",notifyUrl);//回调地址
            map.put("trade_type","NATIVE"); //交易类型

            //将map转换成xml
            String xmlParam = null; //xml格式的参数
            xmlParam = WXPayUtil.generateSignedXml(map, config.getKey());
            //发送请求
            WXPayRequest wxPayRequest = new WXPayRequest(config);
            //调用接口返回结果字符串
            String xmlResult = wxPayRequest.requestWithCert("/pay/unifiedorder", null, xmlParam, false);
            System.out.println(xmlResult);
            //解析返回结果

            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlResult); //将xml解析成map
            Map m = new HashMap();
            m.put("code_url",resultMap.get("code_url"));
            m.put("total_fee",money+"");
            m.put("out_trade_no",orderId);
            return m;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 支付成功后回调逻辑
     * @param xml
     */
    @Override
    public void notifyLogic(String xml) {
        try {
            //解析微信支付返回的xml string
            Map<String, String> map = WXPayUtil.xmlToMap(xml);
            //2.验证签名
            boolean isSigned = WXPayUtil.isSignatureValid(map,config.getKey());

            String out_trade_no = map.get("out_trade_no");
            String transactionId = map.get("transactionId");
            //更新订单状态
            if(isSigned){
                if("SUCCESS".equals(map.get("result_code"))){
                    orderService.updatePayStatus(out_trade_no,transactionId);
                    //发送订单号给mq
                    rabbitTemplate.convertAndSend("paynotify","",out_trade_no);
                }else{
                    //记录日志
                }
            }else{
                //记录日志

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据订单号调用微信接口查询订单状态
     * @param orderId
     * @return
     */
    public Map orderQuery(String orderId){

        try {
            Map map = new HashMap();
            map.put("appid",config.getAppID());
            map.put("mch_id",config.getMchID());
            map.put("out_trade_no",orderId);
            map.put("nonce_str",WXPayUtil.generateNonceStr());

            String signedXml = null;
            signedXml = WXPayUtil.generateSignedXml(map,config.getKey());
            System.out.println("参数: "+signedXml);

            WXPayRequest request = new WXPayRequest(config);
            String xmlResult = request.requestWithCert("/pay/orderquery", null, signedXml, false);
            System.out.println("结果: "+xmlResult);
            Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult);
            Map<String,String> retMap = new HashMap<>();
            retMap.put("trade_state",mapResult.get("trade_state"));
            retMap.put("trade_state_desc",mapResult.get("trade_state_desc"));
            return retMap;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }


    /**
     * 调用微信接口关闭订单
     * @param orderId
     */
    public void closeOrder(String orderId){
        try {
            Map map = new HashMap();
            map.put("appid",config.getAppID());
            map.put("mch_id",config.getMchID());
            map.put("out_trade_no",orderId);
            map.put("nonce_str",WXPayUtil.generateNonceStr());

            String signedXml = WXPayUtil.generateSignedXml(map,config.getKey());
            System.out.println("参数: "+signedXml);

            WXPayRequest request = new WXPayRequest(config);
            String xmlResult = request.requestWithCert("/pay/closeorder", null, signedXml, false);
            System.out.println("结果: "+xmlResult);
            Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
