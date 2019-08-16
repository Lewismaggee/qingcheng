package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.pojo.goods.Category;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.order.OrderItem;
import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.service.order.CartService;
import com.qingcheng.service.order.PreferentialService;
import com.qingcheng.util.CacheKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 购物车接口实现类
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Reference
    private SkuService skuService;

    @Reference
    private CategoryService categoryService;

    @Autowired
    private PreferentialService preferentialService;

    /**
     * 从redis中根据用户名查询购物车商品列表
     * @param username 用户名
     * @return
     */
    @Override
    public List<Map<String, Object>> findCartList(String username) {
        List<Map<String,Object>> cartList = (List<Map<String, Object>>) redisTemplate.boundHashOps(CacheKey.CART_LIST).get(username);
        if(cartList==null){
            cartList = new ArrayList<Map<String, Object>>();
        }
        return cartList;
    }

    /**
     * 根据用户名往购物车列表添加、删除商品
     * @param username 用户名
     * @param skuId 商品id
     * @param num 商品数量 num：正 添加  负 删除
     */
    @Override
    public void addItem(String username, String skuId, Integer num) {
        //1.购物车有该商品
        //1.1 根据用户名获取购物车
        List<Map<String, Object>> cartList = findCartList(username);
        //1.2 遍历购物车
        boolean flag = false; //标记购物车是否存在该商品
        for(Map<String,Object> map : cartList){
            //1.3 获取每一条商品
            OrderItem item = (OrderItem) map.get("item");
            if(item.getSkuId().equals(skuId)){ //购物车存在该商品
                if(item.getNum() <= 0){ //如果数量小于等于0，从购物车移除
                    cartList.remove(map);
                    flag = true;
                    break;
                }
                //1.4 更新
                //单个商品重量
                int weight = item.getWeight()/item.getNum();
                item.setNum(item.getNum()+num);//数量累加
                //1.5 总重量
                item.setWeight(weight*item.getNum());
                //1.6 总金额
                item.setMoney(item.getPrice()*item.getNum());
                if(item.getNum() <= 0){
                    cartList.remove(map);
                }
                flag = true;
                break;
            }
        }


        //2.购物车没有该商品,添加商品到购物车
        if(!flag){
            Sku sku = skuService.findById(skuId);
            if(sku == null){
                throw new RuntimeException("该商品不存在");
            }
            if(!"1".equals(sku.getStatus())){
                throw new RuntimeException("该商品状态不合法");
            }
            if(num <=0 ){
                throw new RuntimeException("商品数量不合法");
            }
            OrderItem item = new OrderItem();
            item.setNum(num);
            item.setSkuId(skuId);
            item.setMoney(sku.getPrice()*num);
            item.setSpuId(sku.getSpuId());
            item.setImage(sku.getImage());
            item.setName(sku.getName());
            item.setPrice(sku.getPrice());
            if(sku.getWeight() == null){
                item.setWeight(0);
            }
            item.setWeight(sku.getWeight()*num);

            //添加商品分类
            item.setCategoryId3(sku.getCategoryId());
            //根据三级分类找二级分类
            Category category3 = (Category) redisTemplate.boundHashOps(CacheKey.CATEGORY).get(sku.getCategoryId());
            if(category3 == null){
                //缓存中查不到，再查数据库
                category3 = categoryService.findById(sku.getCategoryId());
                redisTemplate.boundHashOps(CacheKey.CATEGORY).put(sku.getCategoryId(),category3);
            }
            item.setCategoryId2(category3.getParentId());
            //根据二级分类找一级分类
            Category category2 = (Category) redisTemplate.boundHashOps(CacheKey.CATEGORY).get(category3.getParentId());
            if(category2 == null){
                category2 = categoryService.findById(category3.getParentId());
                redisTemplate.boundHashOps(CacheKey.CATEGORY).put(category3.getParentId(),category2);
            }
            item.setCategoryId1(category2.getParentId());

            Map map = new HashMap();
            map.put("item",item);
            map.put("checked",true);//默认选中
            cartList.add(map);
        }
        redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,cartList);
    }

    @Override
    public boolean updateChecked(String username, String skuId, boolean checked) {
        //获取购物车
        List<Map<String,Object>> cartList = findCartList(username);
        //遍历购物车
        boolean isOk = false; //判断缓存中是否含有已购商品
        for(Map<String,Object> map : cartList){
            OrderItem orderItem = (OrderItem) map.get("item");
            if(orderItem.getSkuId().equals(skuId)){
                map.put("checked",checked);
                isOk = true;//执行成功
                break;
            }
        }
        if(isOk){
            //更新缓存
            redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,cartList);
        }

        return isOk;
    }

    @Override
    public void deleteCheckedCart(String username) {
        /**
         * 思路: 获取购物车中未选中的item  ,覆盖现有的购物车
         */
        List<Map<String, Object>> cartList = findCartList(username);
        //获取未选中的购物车
        List<Map<String, Object>> unChecked
                = cartList.stream().filter(cart -> (boolean) cart.get("checked") == false).collect(Collectors.toList());
        //更新缓存
        redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,unChecked);
    }

    /**
     * 获取用户购物车中商品总优惠金额
     * @param username
     * @return
     */
    @Override
    public int preferential(String username) {
        /**
         * 思路：获取购物车; 将购物车中商品按categoryId分类; 统计出各个分类的总金额
         */
        List<Map<String, Object>> cartList = findCartList(username);
        //筛选出选中的购物车
        List<OrderItem> orderItemList
                = cartList.stream().filter(cart -> (boolean) cart.get("checked") == true).
                map(cart -> (OrderItem) cart.get("item")).collect(Collectors.toList());
        //按类型分组统计：包含每一类以及每一类商品总金额
        Map<Integer, IntSummaryStatistics> cartMap
                = orderItemList.stream().collect(Collectors.groupingBy(OrderItem::getCategoryId3,
                Collectors.summarizingInt(OrderItem::getMoney)));
        int allPreMoney = 0; //总优惠金额
        //循环结果，统计每个分类的优惠金额，并累加
        for(Integer categoryId : cartMap.keySet()){
            //获取品类的消费金额
            int totalMoney = (int)cartMap.get(categoryId).getSum();
            //获取每个品类的优惠金额
            int preMoney = preferentialService.findPreMoneyByCategoryId(categoryId, totalMoney);
            allPreMoney += preMoney;
        }
        return allPreMoney;
    }

    @Override
    public List<Map<String, Object>> findNewOrderItemList(String username) {
        //获取购物车
        List<Map<String, Object>> cartList = findCartList(username);
        //刷新购物车
        for(Map<String,Object> cart : cartList){
            OrderItem orderItem =(OrderItem) cart.get("item");
            Sku sku = skuService.findById(orderItem.getSkuId());
            orderItem.setPrice(sku.getPrice()); //更新价格
            orderItem.setMoney(sku.getPrice()*orderItem.getNum()); //更新金额
        }

        //保存最新购物车
        redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,cartList);
        return cartList;
    }
}
