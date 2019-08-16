package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.*;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.*;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.service.goods.SpuService;
import com.qingcheng.util.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service(interfaceClass = SpuService.class)
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    @Autowired
    private GoodsOperateLogMapper goodsOperateLogMapper;

    @Autowired
    private SkuService skuService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 返回全部记录
     * @return
     */
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Spu> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Spu> spus = (Page<Spu>) spuMapper.selectAll();
        return new PageResult<Spu>(spus.getTotal(),spus.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Spu> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Spu> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Spu> spus = (Page<Spu>) spuMapper.selectByExample(example);
        return new PageResult<Spu>(spus.getTotal(),spus.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public Spu findById(String id) {
        return spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param spu
     */
    public void add(Spu spu) {
        spuMapper.insert(spu);
    }

    /**
     * 修改
     * @param spu
     */
    public void update(Spu spu) {
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     *  删除
     * @param id
     */
    public void delete(String id) {
        spuMapper.deleteByPrimaryKey(id);
    }

    /**
     * 新增goods
     * @param goods
     */
    @Transactional
    public void saveGoods(Goods goods) {
        //保存一个spu
        Spu spu = goods.getSpu();
        Spu spu_db = null;
        if(spu.getId() != null){
            spu_db = spuMapper.selectByPrimaryKey(spu.getId());
        }
        if(spu.getId() == null || spu_db == null ){ //新增
            spu.setId(idWorker.nextId()+"");
            spuMapper.insert(spu);
        }else{ //修改
            //1.先删除原来的sku
            String spuId = spu.getId();
            Example example = new Example(Sku.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("spuId",spuId);
            skuMapper.deleteByExample(example);

            //2.执行spu的修改
            spuMapper.updateByPrimaryKeySelective(spu);
        }

        //保存sku
        List<Sku> skuList = goods.getSkuList();
        Date date = new Date();
        //查询Category
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());
        for(Sku sku : skuList){

            sku.setSpuId(spu.getId());
            //sku name = spu name + 规格值列表
            String name = spu.getName();
            //sku.getSpec() : {"颜色":"红","机身内存":"64G"},
            if(sku.getSpec() == null || "".equals(sku.getSpec())){
                sku.setSpec("{}");
            }
            Map<String,String> map = JSON.parseObject(sku.getSpec(),Map.class);
            for(String str : map.values()){
                name+=" "+str;
            }
            sku.setName(name); //名称
            sku.setCreateTime(date);
            sku.setUpdateTime(date);
            sku.setCategoryId(spu.getCategory3Id());//分类id
            sku.setCategoryName(category.getName()); //分类名称
            //新增
            sku.setId(idWorker.nextId()+"");

            sku.setSaleNum(0); //销量数
            sku.setCommentNum(0); //评论数
            skuMapper.insert(sku);

            //重新将价格更新到缓存
            skuService.savePriceToRedisById(sku.getId(),sku.getPrice());

        }

        //新增分类与品牌的关系
        CategoryBrand categoryBrand = new CategoryBrand();
        categoryBrand.setCategoryId(spu.getCategory3Id());
        categoryBrand.setBrandId(spu.getBrandId());
        //先查询该关系是否已存在
        int count = categoryBrandMapper.selectCount(categoryBrand);
        if(count == 0){
            categoryBrandMapper.insert(categoryBrand);
        }
    }

    /**
     * 根据id查询商品
     * @param id
     * @return
     */
    public Goods findGoodsById(String id) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);

        //查询skus
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId",id);
        List<Sku> skus = skuMapper.selectByExample(example);

        Goods goods = new Goods();
        goods.setSpu(spu);
        goods.setSkuList(skus);

        return goods;
    }

    /**
     * 商品审核
     * @param goodsAudit
     */
    @Transactional
    public void audit(GoodsAudit goodsAudit) {
        Spu spu = new Spu();
        spu.setId(goodsAudit.getId());
        spu.setStatus(goodsAudit.getStatus());
        if("1".equals(goodsAudit.getStatus())){
            //审核通过,自动上架
            spu.setIsMarketable("1");
        }
        Spu spu2 = spuMapper.selectByPrimaryKey(spu.getId());
        spuMapper.updateByPrimaryKeySelective(spu);

        //记录商品日志
        GoodsOperateLog goodsOperateLog = new GoodsOperateLog();
        goodsOperateLog.setOperator("admin");
        goodsOperateLog.setOperateTime(new Date());
        goodsOperateLog.setSpuId(spu.getId());
        goodsOperateLog.setStatusOld(spu2.getStatus());
        goodsOperateLog.setStatusNow(goodsAudit.getStatus());
        int insert = goodsOperateLogMapper.insert(goodsOperateLog);

        /*//记录商品审核记录
        GoodsAuditLog goodsAuditLog = new GoodsAuditLog();
        goodsAuditLog.setId(goodsAudit.getId());
        goodsAuditLog.setAuditMessage(goodsAudit.getMessage());
        goodsAuditLog.setAuditor("admin");
        goodsAuditLog.setAuditTime(new Date());

        goodsAuditLog.setOperateLogId();*/




    }

    /**
     * 下架商品
     * @param id
     */
    public void pull(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        spu.setIsMarketable("0");
        spuMapper.updateByPrimaryKeySelective(spu);

        //下架商品后，将该商品id发送到对应的交换器
        Map spuMap = new HashMap();
        spuMap.put("spuId",id);
        rabbitTemplate.convertAndSend("exchange.fanout_goodsOut","queue.goods_out",JSON.toJSONString(spuMap));
    }

    /**
     * 上架商品
     * @param id
     */
    @Transactional
    public void put(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //判断商品是否审核通过
        if(!"1".equals(spu.getStatus())){
            throw new RuntimeException("此商品未审核通过");
        }
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);

        //上架商品后，将该商品id发送到对应交换器
        Map spuMap = new HashMap();
        spuMap.put("spuId",id);
        rabbitTemplate.convertAndSend("exchange.fanout_goodsPut","queue.goods_put",JSON.toJSONString(spuMap));

        //日志
        GoodsOperateLog goodsOperateLog = new GoodsOperateLog();
        goodsOperateLog.setOperator("admin");
        goodsOperateLog.setOperateTime(new Date());
        goodsOperateLog.setSpuId(spu.getId());
        goodsOperateLogMapper.insert(goodsOperateLog);
    }

    /**
     * 批量上架
     * @param ids
     */
    public int putMany(Long[] ids) {
        Spu spu = new Spu();
        spu.setIsMarketable("1");

        //批量修改
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));
        criteria.andEqualTo("isMarketable","0"); //下架
        criteria.andEqualTo("status","1");//审核通过的
        criteria.andEqualTo("isDelete","0");//非删除的
        return spuMapper.updateByExampleSelective(spu,example);
    }

    /**
     * 批量下架
     * @param ids
     * @return
     */
    public int pullMany(Long[] ids) {
        Spu spu = new Spu();
        spu.setStatus("0");
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id",Arrays.asList(ids));
        criteria.andEqualTo("isMarketable","0");
        criteria.andEqualTo("status","1");
        criteria.andEqualTo("isDelete","0");
        return spuMapper.updateByExampleSelective(spu,example);
    }

    /**
     * 删除商品
     * @param id
     */
    public void remove(String id) {
        //删除缓存中价格
        Map map = new HashMap();
        map.put("spuId",id);
        List<Sku> skuList = skuService.findList(map);
        for(Sku sku : skuList){
            skuService.deletePriceFromRedis(sku.getId());
        }

        //删除spu
        Spu spu = new Spu();
        spu.setId(id);
        spu.setIsDelete("1");
        spuMapper.updateByPrimaryKeySelective(spu);

        //sku列表的删除
    }

    /**
     * 批量删除商品
     * @param ids
     */
    public void removeMany(Long[] ids) {
        Spu spu = new Spu();
        spu.setIsDelete("1");
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id",Arrays.asList(ids));
        spuMapper.updateByExampleSelective(spu,example);
    }

    /**
     * 还原删除的商品
     * @param id
     */
    public void unRemove(String id) {
        Spu spu = new Spu();
        spu.setIsDelete("0");
        spu.setId(id);
        spuMapper.updateByPrimaryKey(spu);
    }

    public void unRemoveMany(Long[] ids){
        Spu spu = new Spu();
        spu.setIsDelete("0");
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id",Arrays.asList(ids));
        spuMapper.updateByExampleSelective(spu,example);

    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 主键
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andLike("id","%"+searchMap.get("id")+"%");
            }
            // 货号
            if(searchMap.get("sn")!=null && !"".equals(searchMap.get("sn"))){
                criteria.andLike("sn","%"+searchMap.get("sn")+"%");
            }
            // SPU名
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
            }
            // 副标题
            if(searchMap.get("caption")!=null && !"".equals(searchMap.get("caption"))){
                criteria.andLike("caption","%"+searchMap.get("caption")+"%");
            }
            // 图片
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
            }
            // 图片列表
            if(searchMap.get("images")!=null && !"".equals(searchMap.get("images"))){
                criteria.andLike("images","%"+searchMap.get("images")+"%");
            }
            // 售后服务
            if(searchMap.get("saleService")!=null && !"".equals(searchMap.get("saleService"))){
                criteria.andLike("saleService","%"+searchMap.get("saleService")+"%");
            }
            // 介绍
            if(searchMap.get("introduction")!=null && !"".equals(searchMap.get("introduction"))){
                criteria.andLike("introduction","%"+searchMap.get("introduction")+"%");
            }
            // 规格列表
            if(searchMap.get("specItems")!=null && !"".equals(searchMap.get("specItems"))){
                criteria.andLike("specItems","%"+searchMap.get("specItems")+"%");
            }
            // 参数列表
            if(searchMap.get("paraItems")!=null && !"".equals(searchMap.get("paraItems"))){
                criteria.andLike("paraItems","%"+searchMap.get("paraItems")+"%");
            }
            // 是否上架
            if(searchMap.get("isMarketable")!=null && !"".equals(searchMap.get("isMarketable"))){
                criteria.andLike("isMarketable","%"+searchMap.get("isMarketable")+"%");
            }
            // 是否启用规格
            if(searchMap.get("isEnableSpec")!=null && !"".equals(searchMap.get("isEnableSpec"))){
                criteria.andLike("isEnableSpec","%"+searchMap.get("isEnableSpec")+"%");
            }
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andLike("isDelete","%"+searchMap.get("isDelete")+"%");
            }
            // 审核状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andLike("status","%"+searchMap.get("status")+"%");
            }

            // 品牌ID
            if(searchMap.get("brandId")!=null ){
                criteria.andEqualTo("brandId",searchMap.get("brandId"));
            }
            // 一级分类
            if(searchMap.get("category1Id")!=null ){
                criteria.andEqualTo("category1Id",searchMap.get("category1Id"));
            }
            // 二级分类
            if(searchMap.get("category2Id")!=null ){
                criteria.andEqualTo("category2Id",searchMap.get("category2Id"));
            }
            // 三级分类
            if(searchMap.get("category3Id")!=null ){
                criteria.andEqualTo("category3Id",searchMap.get("category3Id"));
            }
            // 模板ID
            if(searchMap.get("templateId")!=null ){
                criteria.andEqualTo("templateId",searchMap.get("templateId"));
            }
            // 运费模板id
            if(searchMap.get("freightId")!=null ){
                criteria.andEqualTo("freightId",searchMap.get("freightId"));
            }
            // 销量
            if(searchMap.get("saleNum")!=null ){
                criteria.andEqualTo("saleNum",searchMap.get("saleNum"));
            }
            // 评论数
            if(searchMap.get("commentNum")!=null ){
                criteria.andEqualTo("commentNum",searchMap.get("commentNum"));
            }

        }
        return example;
    }

}
