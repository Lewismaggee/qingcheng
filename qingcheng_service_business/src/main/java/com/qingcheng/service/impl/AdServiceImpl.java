package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.AdMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.business.Ad;
import com.qingcheng.service.business.AdService;
import com.qingcheng.util.CacheKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class AdServiceImpl implements AdService {

    @Autowired
    private AdMapper adMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 返回全部记录
     * @return
     */
    public List<Ad> findAll() {
        return adMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Ad> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Ad> ads = (Page<Ad>) adMapper.selectAll();
        return new PageResult<Ad>(ads.getTotal(),ads.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Ad> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return adMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Ad> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Ad> ads = (Page<Ad>) adMapper.selectByExample(example);
        return new PageResult<Ad>(ads.getTotal(),ads.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public Ad findById(Integer id) {
        return adMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param ad
     */
    public void add(Ad ad) {
        adMapper.insert(ad);
        //重新加载所有广告进缓存
//        saveAllAdToRedis();  //效率低下
        //重新加载该位置广告进缓存
        saveAdToRedisByPosition(ad.getPosition());
    }

    /**
     * 修改
     * @param ad
     */
    public void update(Ad ad) {
        //1.获取之前的广告位置
        String beforePosition = adMapper.selectByPrimaryKey(ad.getId()).getPosition();

        //2.获取更新后的广告位置
        String afterPosition = ad.getPosition();
        //3.更新数据库
        adMapper.updateByPrimaryKeySelective(ad);
        //4.更新缓存
        saveAdToRedisByPosition(beforePosition);
        //5.如果更新了广告位置,就需要更新新广告数据进缓存
        if(!beforePosition.equals(afterPosition)){
            saveAdToRedisByPosition(afterPosition);
        }
    }

    /**
     *  删除
     * @param id
     */
    public void delete(Integer id) {
        String position = adMapper.selectByPrimaryKey(id).getPosition();
        //1.删除
        adMapper.deleteByPrimaryKey(id);
        //2.根据位置重新加载进缓存
        saveAdToRedisByPosition(position);
    }

    /**
     * 根据位置查询广告列表
     * @param position
     * @return
     */
    public List<Ad> findByPosition(String position) {
        //从缓存中查询广告列表
        return (List<Ad>) redisTemplate.boundHashOps(CacheKey.AD).get(position);
    }

    /**
     * 获取所有广告的位置
     * @return
     */
    private List<String> getAdPositionList(){
        List<String> position = new ArrayList<String>();

        position.add("web_index_lb");
        //有更多的广告位置,就继续add

        return position;
    }

    /**
     * 将全部的广告数据存入缓存
     */
    public void saveAllAdToRedis() {
        List<String> positionList = getAdPositionList();
        for(String position : positionList){
            saveAdToRedisByPosition(position);
        }
    }

    /**
     * 将某个位置的广告存入缓存
     * @param position
     */
    public void saveAdToRedisByPosition(String position) {
        //1.根据位置查询该位置广告列表
        Example example = new Example(Ad.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("position",position);
        //开始时间小于等于当前时间
        criteria.andLessThanOrEqualTo("startTime",new Date());
        //结束时间大于等于当前时间
        criteria.andGreaterThanOrEqualTo("endTime",new Date());
        criteria.andEqualTo("status","1");
        List<Ad> adsList = adMapper.selectByExample(example);
        //2.装入缓存 :
        // hash 大key：广告   小key: 广告位置
        redisTemplate.boundHashOps(CacheKey.AD).put(position,adsList);
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Ad.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 广告名称
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
            }
            // 广告位置
            if(searchMap.get("position")!=null && !"".equals(searchMap.get("position"))){
                criteria.andLike("position","%"+searchMap.get("position")+"%");
            }
            // 状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andLike("status","%"+searchMap.get("status")+"%");
            }
            // 图片地址
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
            }
            // URL
            if(searchMap.get("url")!=null && !"".equals(searchMap.get("url"))){
                criteria.andLike("url","%"+searchMap.get("url")+"%");
            }
            // 备注
            if(searchMap.get("remarks")!=null && !"".equals(searchMap.get("remarks"))){
                criteria.andLike("remarks","%"+searchMap.get("remarks")+"%");
            }

            // ID
            if(searchMap.get("id")!=null ){
                criteria.andEqualTo("id",searchMap.get("id"));
            }

        }
        return example;
    }

}
