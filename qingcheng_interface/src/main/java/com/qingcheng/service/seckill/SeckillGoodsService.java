package com.qingcheng.service.seckill;

import com.qingcheng.pojo.seckill.SeckillGoods;

import java.util.List;

public interface SeckillGoodsService {
    /**
     * 根据时间区间查询秒杀商品
     */
    List<SeckillGoods> list(String time);
}
