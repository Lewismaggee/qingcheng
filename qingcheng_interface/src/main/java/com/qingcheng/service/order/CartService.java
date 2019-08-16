package com.qingcheng.service.order;

import java.util.List;
import java.util.Map;

public interface CartService {
    /**
     * 根据用户名查询购物车列表
     * @param username
     * @return
     */
    public List<Map<String,Object>> findCartList(String username);

    /**
     * 添加或删除购物车数据
     * @param username
     * @param skuId
     * @param num
     */
    public void addItem(String username,String skuId,Integer num);

    /**
     * 更新复选框勾选状态
     * @param username
     * @param skuId
     * @param checked
     * @return
     */
    public boolean updateChecked(String username,String skuId,boolean checked);


    /**
     * 删除选中的购物车
     * @param username
     */
    public void deleteCheckedCart(String username);

    /**
     * 计算当前购物车优惠金额
     * @param username
     * @return
     */
    public int preferential(String username);

    /**
     * 获取最新的购物车列表
     * @param username
     * @return
     */
    public List<Map<String,Object>> findNewOrderItemList(String username);
}
