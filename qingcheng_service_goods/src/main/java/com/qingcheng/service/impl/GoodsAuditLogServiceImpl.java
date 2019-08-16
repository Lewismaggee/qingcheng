package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.GoodsAuditLogMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.GoodsAuditLog;
import com.qingcheng.service.goods.GoodsAuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

@Service
public class GoodsAuditLogServiceImpl implements GoodsAuditLogService {

    @Autowired
    private GoodsAuditLogMapper goodsAuditLogMapper;

    /**
     * 返回全部记录
     * @return
     */
    public List<GoodsAuditLog> findAll() {
        return goodsAuditLogMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<GoodsAuditLog> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<GoodsAuditLog> goodsAuditLogs = (Page<GoodsAuditLog>) goodsAuditLogMapper.selectAll();
        return new PageResult<GoodsAuditLog>(goodsAuditLogs.getTotal(),goodsAuditLogs.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<GoodsAuditLog> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return goodsAuditLogMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<GoodsAuditLog> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<GoodsAuditLog> goodsAuditLogs = (Page<GoodsAuditLog>) goodsAuditLogMapper.selectByExample(example);
        return new PageResult<GoodsAuditLog>(goodsAuditLogs.getTotal(),goodsAuditLogs.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public GoodsAuditLog findById(String id) {
        return goodsAuditLogMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param goodsAuditLog
     */
    public void add(GoodsAuditLog goodsAuditLog) {
        goodsAuditLogMapper.insert(goodsAuditLog);
    }

    /**
     * 修改
     * @param goodsAuditLog
     */
    public void update(GoodsAuditLog goodsAuditLog) {
        goodsAuditLogMapper.updateByPrimaryKeySelective(goodsAuditLog);
    }

    /**
     *  删除
     * @param id
     */
    public void delete(String id) {
        goodsAuditLogMapper.deleteByPrimaryKey(id);
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(GoodsAuditLog.class);
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
            // 审核人
            if(searchMap.get("auditor")!=null && !"".equals(searchMap.get("auditor"))){
                criteria.andLike("auditor","%"+searchMap.get("auditor")+"%");
            }
            // 审核意见
            if(searchMap.get("auditMessage")!=null && !"".equals(searchMap.get("auditMessage"))){
                criteria.andLike("auditMessage","%"+searchMap.get("auditMessage")+"%");
            }
            // 商品日志ID
            if(searchMap.get("operateLogId")!=null && !"".equals(searchMap.get("operateLogId"))){
                criteria.andLike("operateLogId","%"+searchMap.get("operateLogId")+"%");
            }


        }
        return example;
    }

}
