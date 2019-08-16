package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.SkuMapper;
import com.qingcheng.dao.StockBackMapper;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.goods.StockBack;
import com.qingcheng.pojo.order.OrderItem;
import com.qingcheng.service.goods.StockBackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
@Service(interfaceClass = StockBackService.class)
public class StockBackServiceImpl implements StockBackService {
    @Autowired
    private StockBackMapper stockBackMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Override
    @Transactional
    public void addList(List<OrderItem> orderItemList) {
        for(OrderItem orderItem : orderItemList){
            StockBack stockBack = new StockBack();
            stockBack.setOrderId(orderItem.getOrderId());
            stockBack.setCreateTime(new Date());
            stockBack.setSkuId(orderItem.getSkuId());
            stockBack.setNum(orderItem.getNum());
            stockBack.setStatus("0");
            stockBackMapper.insert(stockBack);
        }
    }


    @Override
    @Transactional
    public void doBack() {
        System.out.println("hui gun task begin =======");
        //查询
        StockBack stockBack = new StockBack();
        stockBack.setStatus("0"); //未回滚
        List<StockBack> stockBackList = stockBackMapper.select(stockBack);
        for(StockBack sb : stockBackList){
            //添加库存
            skuMapper.deductionStock(sb.getSkuId(),-sb.getNum());
            //减少销量
            skuMapper.addSaleNum(sb.getSkuId(),-sb.getNum());
            //更新回滚表status状态为已回滚
            sb.setStatus("1");
            sb.setBackTime(new Date()); //回滚时间
            stockBackMapper.updateByPrimaryKeySelective(sb);
        }
        System.out.println("hui gun task end ========");
    }
}
