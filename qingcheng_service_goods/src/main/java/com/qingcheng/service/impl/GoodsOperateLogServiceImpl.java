package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.GoodsOperateLogMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.GoodsOperateLog;
import com.qingcheng.service.goods.GoodsOperateLogService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

@Service
public class GoodsOperateLogServiceImpl implements GoodsOperateLogService {

    @Autowired
    private GoodsOperateLogMapper goodsOperateLogMapper;

    /**
     * 返回全部记录
     * @return
     */
    public List<GoodsOperateLog> findAll() {
        return goodsOperateLogMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<GoodsOperateLog> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<GoodsOperateLog> goodsOperateLogs = (Page<GoodsOperateLog>) goodsOperateLogMapper.selectAll();
        return new PageResult<GoodsOperateLog>(goodsOperateLogs.getTotal(),goodsOperateLogs.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<GoodsOperateLog> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return goodsOperateLogMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<GoodsOperateLog> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<GoodsOperateLog> goodsOperateLogs = (Page<GoodsOperateLog>) goodsOperateLogMapper.selectByExample(example);
        return new PageResult<GoodsOperateLog>(goodsOperateLogs.getTotal(),goodsOperateLogs.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public GoodsOperateLog findById(String id) {
        return goodsOperateLogMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param goodsOperateLog
     */
    public void add(GoodsOperateLog goodsOperateLog) {
        goodsOperateLogMapper.insert(goodsOperateLog);
    }

    /**
     * 修改
     * @param goodsOperateLog
     */
    public void update(GoodsOperateLog goodsOperateLog) {
        goodsOperateLogMapper.updateByPrimaryKeySelective(goodsOperateLog);
    }

    /**
     *  删除
     * @param id
     */
    public void delete(String id) {
        goodsOperateLogMapper.deleteByPrimaryKey(id);
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(GoodsOperateLog.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 主键
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andLike("id","%"+searchMap.get("id")+"%");
            }
            // SPUID
            if(searchMap.get("spuId")!=null && !"".equals(searchMap.get("spuId"))){
                criteria.andLike("spuId","%"+searchMap.get("spuId")+"%");
            }
            // 操作员
            if(searchMap.get("operater")!=null && !"".equals(searchMap.get("operater"))){
                criteria.andLike("operater","%"+searchMap.get("operater")+"%");
            }
            // 审核记录ID
            if(searchMap.get("auditId")!=null && !"".equals(searchMap.get("auditId"))){
                criteria.andLike("auditId","%"+searchMap.get("auditId")+"%");
            }
            // 原：是否上架
            if(searchMap.get("isMarketableOld")!=null && !"".equals(searchMap.get("isMarketableOld"))){
                criteria.andLike("isMarketableOld","%"+searchMap.get("isMarketableOld")+"%");
            }
            // 现：是否上架
            if(searchMap.get("isMarketableNow")!=null && !"".equals(searchMap.get("isMarketableNow"))){
                criteria.andLike("isMarketableNow","%"+searchMap.get("isMarketableNow")+"%");
            }
            // 原：是否删除
            if(searchMap.get("isDeleteOld")!=null && !"".equals(searchMap.get("isDeleteOld"))){
                criteria.andLike("isDeleteOld","%"+searchMap.get("isDeleteOld")+"%");
            }
            // 现：是否删除
            if(searchMap.get("isDeleteNow")!=null && !"".equals(searchMap.get("isDeleteNow"))){
                criteria.andLike("isDeleteNow","%"+searchMap.get("isDeleteNow")+"%");
            }
            // 原：审核状态
            if(searchMap.get("statusOld")!=null && !"".equals(searchMap.get("statusOld"))){
                criteria.andLike("statusOld","%"+searchMap.get("statusOld")+"%");
            }
            // 现：审核状态
            if(searchMap.get("statusNow")!=null && !"".equals(searchMap.get("statusNow"))){
                criteria.andLike("statusNow","%"+searchMap.get("statusNow")+"%");
            }


        }
        return example;
    }

}
