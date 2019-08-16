package com.qingcheng.service.goods;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.GoodsAuditLog;

import java.util.*;

/**
 * goodsAuditLog业务逻辑层
 */
public interface GoodsAuditLogService {


    public List<GoodsAuditLog> findAll();


    public PageResult<GoodsAuditLog> findPage(int page, int size);


    public List<GoodsAuditLog> findList(Map<String,Object> searchMap);


    public PageResult<GoodsAuditLog> findPage(Map<String,Object> searchMap,int page, int size);


    public GoodsAuditLog findById(String id);

    public void add(GoodsAuditLog goodsAuditLog);


    public void update(GoodsAuditLog goodsAuditLog);


    public void delete(String id);

}
