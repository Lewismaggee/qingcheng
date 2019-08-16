package com.qingcheng.timer;

import com.qingcheng.dao.SeckillGoodsMapper;
import com.qingcheng.pojo.seckill.SeckillGoods;
import com.qingcheng.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillGoodsTask {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 0s开始执行,，每隔15秒再次执行一次
     */
    @Scheduled(cron = "0/15 * * * * ?")
    public void loadGoods(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //1.查询所有时间区间
        List<Date> dateList = DateUtil.getDateMenus();
        //2.循环时间区间，查询每个时间区间的秒杀商品
        for(Date startTime : dateList){

            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            //2.1 商品必须审核通过
            criteria.andEqualTo("status","1");
            //2.2 库存>0
            criteria.andGreaterThan("stockCount",0);
            //2.3 秒杀开始时间>=当前循环的时间区间的开始时间
            criteria.andGreaterThanOrEqualTo("startTime",startTime);
            //2.4 秒杀结束时间<当前循环的时间区间的开始时间+2小时
            criteria.andLessThan("endTime",DateUtil.addDateHour(startTime,2));

            //2.5 过滤redis中已经存在的该区间的秒杀商品
            Set keys = redisTemplate.boundHashOps("SeckillGoods_" + DateUtil.date2Str(startTime)).keys();
            if(keys != null && keys.size() > 0){
                //select * from table where id not in (keys)
                criteria.andNotIn("id",keys);
            }

            //2.6 从数据库查询
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);

            //将秒杀商品存入到redis缓存
            //2019090912
            for(SeckillGoods seckillGood : seckillGoodsList){
                redisTemplate.boundHashOps("SeckillGoods_"+DateUtil.date2Str(startTime)).put(seckillGood.getId(),seckillGood);
            }

        }

    }
}
