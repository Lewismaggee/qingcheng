package com.qingcheng.service.goods;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.Goods;
import com.qingcheng.pojo.goods.GoodsAudit;
import com.qingcheng.pojo.goods.Spu;

import java.util.*;

/**
 * spu业务逻辑层
 */
public interface SpuService {


    public List<Spu> findAll();


    public PageResult<Spu> findPage(int page, int size);


    public List<Spu> findList(Map<String,Object> searchMap);


    public PageResult<Spu> findPage(Map<String,Object> searchMap,int page, int size);


    public Spu findById(String id);

    public void add(Spu spu);


    public void update(Spu spu);


    public void delete(String id);

    public void saveGoods(Goods goods);

    public Goods findGoodsById(String id);

    public void audit(GoodsAudit goodsAudit);

    public void pull(String id);

    public void put(String id);

    public int putMany(Long[] ids);

    public int pullMany(Long[] ids);

    public void remove(String id);

    public void removeMany(Long[] ids);

    public void unRemove(String id);

    public void unRemoveMany(Long[] ids);

}
