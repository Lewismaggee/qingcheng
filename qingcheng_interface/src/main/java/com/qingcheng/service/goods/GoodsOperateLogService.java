package com.qingcheng.service.goods;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.GoodsOperateLog;

import java.util.*;

/**
 * goodsOperateLog业务逻辑层
 */
public interface GoodsOperateLogService {


    public List<GoodsOperateLog> findAll();


    public PageResult<GoodsOperateLog> findPage(int page, int size);


    public List<GoodsOperateLog> findList(Map<String,Object> searchMap);


    public PageResult<GoodsOperateLog> findPage(Map<String,Object> searchMap,int page, int size);


    public GoodsOperateLog findById(String id);

    public void add(GoodsOperateLog goodsOperateLog);


    public void update(GoodsOperateLog goodsOperateLog);


    public void delete(String id);

}
