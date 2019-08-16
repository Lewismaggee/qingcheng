package com.qingcheng.service.order;

import java.util.Map;

public interface WXPayService {
    /**
     * 生成微信支付二维码(统一下单)
     * @param orderId 订单号
     * @param money 金额
     * @param notifyUrl 回调地址
     * @return
     */
    public Map createNative(String orderId,Integer money,String notifyUrl);

    /**
     * 微信支付回调
     * @param xml
     */
    public void notifyLogic(String xml);

    /**
     * 调用微信支付接口查询订单状态
     * @param orderId 订单编号
     * @return
     */
    public Map orderQuery(String orderId);

    /**
     * 调用微信接口关闭订单
     * @param orderId 订单编号
     */
    public void closeOrder(String orderId);
}
