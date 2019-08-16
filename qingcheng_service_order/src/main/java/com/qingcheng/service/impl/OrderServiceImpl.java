package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.OrderConfigMapper;
import com.qingcheng.dao.OrderItemMapper;
import com.qingcheng.dao.OrderLogMapper;
import com.qingcheng.dao.OrderMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.*;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.service.order.CartService;
import com.qingcheng.service.order.OrderService;
import com.qingcheng.service.order.WXPayService;
import com.qingcheng.util.CacheKey;
import com.qingcheng.util.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service(interfaceClass = OrderService.class)
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderLogMapper orderLogMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private OrderConfigMapper orderConfigMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Reference
    private SkuService skuService;

    @Autowired
    private CartService cartService;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    /**
     * 返回全部记录
     *
     * @return
     */
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     * 分页查询
     *
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Order> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        Page<Order> orders = (Page<Order>) orderMapper.selectAll();
        return new PageResult<Order>(orders.getTotal(), orders.getResult());
    }

    /**
     * 条件查询
     *
     * @param searchMap 查询条件
     * @return
     */
    public List<Order> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return orderMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     *
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Order> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        Page<Order> orders = (Page<Order>) orderMapper.selectByExample(example);
        return new PageResult<Order>(orders.getTotal(), orders.getResult());
    }

    /**
     * 根据Id查询
     *
     * @param id
     * @return
     */
    public Order findById(String id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 保存订单
     *
     * @param order
     */
    public Map<String,Object> add(Order order) {
        //刷新购物车 (刷新单价)
        List<Map<String, Object>> cartList = cartService.findNewOrderItemList(order.getUsername());
        //过滤出选中的商品
        List<OrderItem> orderItemList = cartList.stream().filter(cart -> (boolean) cart.get("checked") == true)
                .map(cart -> (OrderItem) cart.get("item")).collect(Collectors.toList());
        //扣减库存
        boolean b = skuService.deductionStock(orderItemList);
        if(!b){
            throw new RuntimeException("库存扣减失败");
        }

        try{
            //保存订单主表
            order.setId(idWorker.nextId()+"");
            int sum = orderItemList.stream().mapToInt(OrderItem::getNum).sum();
            order.setTotalNum(sum);  //总数量
            int totalMoney = orderItemList.stream().mapToInt(OrderItem::getMoney).sum();
            order.setTotalMoney(totalMoney); //订单总金额


            int preferential = cartService.preferential(order.getUsername()); //购物车中商品总优惠金额
            order.setPreMoney(preferential); //优惠金额
            order.setPayMoney(totalMoney-preferential); //付款金额=总订单金额-优惠金额

            order.setOrderStatus("0"); //订单状态 : 订单状态
            order.setPayStatus("0"); //支付状态 ：未支付
            order.setConsignStatus("0"); //发货状态: 未发货

            order.setCreateTime(new Date());
            order.setPayTime(new Date());
            orderMapper.insert(order);

            double proportion = order.getPayMoney()/totalMoney;

            //保存订单明细表
            for(OrderItem orderItem : orderItemList){
                orderItem.setOrderId(order.getId()); //订单id
                orderItem.setId(idWorker.nextId()+"");
                orderItem.setPayMoney((int) (orderItem.getMoney()*proportion)); //支付金额
                orderItemMapper.insert(orderItem);
            }
            //创建订单后，将该订单号发送到rabbitmq  (超时关闭处理)
            Map orderIdMap = new HashMap();
            orderIdMap.put("orderId",order.getId());
            rabbitTemplate.convertAndSend("","queue.order",JSON.toJSONString(orderIdMap));
        }catch (Exception e){
            //发送回滚消息
            rabbitTemplate.convertAndSend("","queue.skuback", JSON.toJSONString(orderItemList));
            throw new RuntimeException("创建订单失败");
        }


        //清除选中购物车
        cartService.deleteCheckedCart(order.getUsername());

        //返回订单号和支付的金额
        Map<String,Object> map = new HashMap<>();
        map.put("ordersn",order.getId());
        map.put("money",order.getPayMoney());
        return map;
    }

    /**
     * 修改
     *
     * @param order
     */
    public void update(Order order) {
        orderMapper.updateByPrimaryKeySelective(order);
    }

    /**
     * 删除
     *
     * @param id
     */
    public void delete(String id) {
        orderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 根据id查询订单列表与详情
     *
     * @param id
     * @return
     */
    public OrderGoods findOrderGoodsById(String id) {
        //1.查询订单
        Order order = orderMapper.selectByPrimaryKey(id);

        //2.查询订单明细
        Example example = new Example(OrderItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderId", id);
        List<OrderItem> orderItems = orderItemMapper.selectByExample(example);

        //3.封装订单对象
        OrderGoods orderGoods = new OrderGoods();
        orderGoods.setOrder(order);
        orderGoods.setOrderItemList(orderItems);
        return orderGoods;
    }

    /**
     * 根据ids查询未发货订单
     *
     * @param searchMap
     * @return
     */
    public List<Order> findOrderList(Map<String, Object> searchMap) {
        /*JSONArray idsJsonArray = (JSONArray) searchMap.get("ids");
        if(idsJsonArray != null){
            List<String> idsStringArray = new ArrayList<String>();
            for(int i = 0 ; i < idsJsonArray.size() ; i++){
                idsStringArray.add(idsJsonArray.getString(i));

            }
            searchMap.put("ids",idsStringArray);
        }*/
        List<Order> orderList = new ArrayList<Order>();
        //查询未发货订单
        Example example = createExample(searchMap);
        orderList = orderMapper.selectByExample(example);
        return orderList;
    }

    /**
     * 批量发货
     *
     * @param orders
     */
    @Transactional
    public void batchDeleverOrders(List<Order> orders) {
        for (Order order : orders) {
            Order order_db = orderMapper.selectByPrimaryKey(order.getId());
            if (!"1".equals(order_db.getPayStatus())) {
                throw new RuntimeException("该订单未支付,不能发货");
            }

            if (order.getShippingCode() == null || order.getShippingName() == null) {
                throw new RuntimeException("请选择快递公司和填写快递单号");
            }
            //发货
            order.setOrderStatus("2"); //已发货
            order.setConsignStatus("1"); //已发货
            order.setConsignTime(new Date());//发货时间
            orderMapper.updateByPrimaryKeySelective(order);

            //记录日志
            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId() + "");
            orderLog.setConsignStatus("1");
            orderLog.setOperater("admin");
            orderLog.setOperateTime(new Date());
            orderLog.setOrderId(Long.parseLong(order.getId()));
            orderLog.setPayStatus(order.getPayStatus());
            orderLog.setOrderStatus(order.getOrderStatus());
            orderLog.setRemarks(order.getBuyerMessage());
            orderLogMapper.insert(orderLog);
        }
    }

    /**
     * 订单超时逻辑处理
     */
    @Transactional
    public void orderTimeOutLogic() {
        //超时订单未关闭,自动关闭
        //1.查询超时时间
        OrderConfig orderConfig = orderConfigMapper.selectByPrimaryKey(1);
        Integer orderTimeout = orderConfig.getOrderTimeout();

        //2.得到超时的时间点
        LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(orderTimeout);

        //3.设置查询条件
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        //订单创建时间小于超时时间
        criteria.andLessThan("createTime", localDateTime);
        criteria.andEqualTo("orderStatus", "0");//未付款的
        criteria.andEqualTo("isDelete", "0");//未删除的

        //查询超时订单
        List<Order> orders = orderMapper.selectByExample(example);
        for (Order order : orders) {
            order.setOrderStatus("4");//订单状态为: 已关闭
            order.setCloseTime(new Date());//关闭时间
            orderMapper.updateByPrimaryKeySelective(order);

            //记录日志
            OrderLog orderLog = new OrderLog();
            orderLog.setOrderStatus("4");
            orderLog.setOperateTime(new Date());
            orderLog.setOperater("system");
            orderLog.setId(idWorker.nextId() + "");
            orderLog.setOrderId(Long.parseLong(order.getId()));
            orderLog.setPayStatus(order.getPayStatus());
            orderLog.setConsignStatus(order.getConsignStatus());
            orderLog.setRemarks("超时订单,自动关闭");
            orderLogMapper.insert(orderLog);
        }
    }

    /**
     * 合并订单
     *
     * @param pid
     * @param fid
     */
    @Override
    @Transactional
    public void mergeOrder(String pid, String fid) {
        //1.查询主订单
        Order p_order = orderMapper.selectByPrimaryKey(pid);
        //2.查询从订单
        Order f_order = orderMapper.selectByPrimaryKey(fid);
        //3.主订单合并
        p_order.setTotalNum(p_order.getTotalNum() + f_order.getTotalNum());//订单数量
        p_order.setTotalMoney(p_order.getTotalMoney() + f_order.getTotalMoney()); //订单金额
        p_order.setPreMoney(p_order.getPreMoney() + f_order.getPreMoney());//优惠金额
        p_order.setPostFee(p_order.getPostFee() + f_order.getPostFee());//邮费
        p_order.setPayMoney(p_order.getPayMoney() + f_order.getPayMoney());
        p_order.setUpdateTime(new Date());
        orderMapper.updateByPrimaryKeySelective(p_order);

        //4.从订单的订单明细归属于主订单
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(p_order.getId());
        Example example = new Example(OrderItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderId", f_order.getId());
        orderItemMapper.updateByExampleSelective(orderItem, example);

        //5.将从订单逻辑删除
        Order order = new Order();
        order.setId(f_order.getId());
        order.setIsDelete("1");
        orderMapper.updateByPrimaryKeySelective(order);

        //6.日志记录
        OrderLog orderLog = new OrderLog();
        orderLog.setId(idWorker.nextId() + "");
        orderLog.setRemarks("主订单" + p_order.getId() + "合并了从订单" + f_order.getId());
        orderLog.setOperater("admin");
        orderLog.setOperateTime(new Date());
        orderLog.setOrderStatus(p_order.getOrderStatus());
        orderLog.setPayStatus(p_order.getPayStatus());
        orderLog.setConsignStatus(p_order.getConsignStatus());
        orderLog.setOrderId(Long.parseLong(f_order.getId())); //主订单id
        orderLogMapper.insert(orderLog);
    }

    /**
     * 拆分订单
     *
     * @param orderItems
     * @param orderId
     */
    @Override
    @Transactional
    public void splitOrder(List<OrderItem> orderItems, String orderId) {

        /*[{id:1,num:10},{id:2,num:5}]*/
        //1.查询订单
        Order new_order = new Order();
        Order old_order = orderMapper.selectByPrimaryKey(orderId);
        if (old_order == null) {
            throw new RuntimeException("要拆分的订单不存在");
        }
        //将old_order对象的属性赋值到新的order对象中
        BeanUtils.copyProperties(old_order, new_order);
        Date now = new Date();
        new_order.setId(idWorker.nextId() + "");
        new_order.setCreateTime(now);
        new_order.setUpdateTime(now);
        new_order.setTotalNum(0);
        new_order.setTotalMoney(0);
        new_order.setPayMoney(0);
        new_order.setPreMoney(0);

        //遍历要拆分的订单明细
        for (OrderItem orderItem : orderItems) {
            OrderItem entireOrderItem = orderItemMapper.selectByPrimaryKey(orderItem.getId());
            //切分数量
            Integer splitNum = orderItem.getNum();
            //明细数量
            Integer totalNum = entireOrderItem.getNum();

            if (splitNum < 1) {
                continue;
            }
            if (splitNum > totalNum) {
                throw new RuntimeException("拆分的数量大于该订单明细的总数量");
            } else {
                //更新新旧订单统计数据
                new_order.setTotalNum(new_order.getTotalNum() + splitNum);
                new_order.setTotalMoney(new_order.getTotalMoney() + entireOrderItem.getMoney() * splitNum/totalNum);
                new_order.setPreMoney(new_order.getPreMoney() + (entireOrderItem.getMoney() - entireOrderItem.getPayMoney())*splitNum/totalNum);
                new_order.setPayMoney(new_order.getPayMoney() + entireOrderItem.getPayMoney() * splitNum/totalNum);
                new_order.setPostFee(new_order.getPostFee() + entireOrderItem.getPostFee() * splitNum / totalNum);

                //更新旧订单统计数据
                old_order.setTotalNum(old_order.getTotalNum()-splitNum);
                old_order.setTotalMoney(old_order.getTotalMoney()-(entireOrderItem.getMoney()-entireOrderItem.getMoney()*splitNum/totalNum));
                old_order.setPreMoney(old_order.getPreMoney()-(entireOrderItem.getMoney()-entireOrderItem.getPayMoney())*splitNum/totalNum);
                old_order.setPostFee(old_order.getPostFee()-(entireOrderItem.getPostFee()-entireOrderItem.getPostFee()*splitNum/totalNum));
                old_order.setPayMoney(old_order.getPayMoney()-(entireOrderItem.getPayMoney()-entireOrderItem.getPayMoney()*splitNum/totalNum));

                //切割数量和该订单明细数量相同，只需更新该明细订单id为新订单id即可
                if(splitNum == totalNum){
                    OrderItem updateOrderItem = new OrderItem();
                    updateOrderItem.setId(orderItem.getId());
                    updateOrderItem.setOrderId(new_order.getId());
                    orderItemMapper.updateByPrimaryKeySelective(updateOrderItem);
                }else{
                    //生成新订单明细
                    OrderItem newOrderItem = new OrderItem();
                    BeanUtils.copyProperties(entireOrderItem,newOrderItem);//将entireOrderItem对象的属性复制到newOrderItem
                    newOrderItem.setId(idWorker.nextId()+"");
                    newOrderItem.setOrderId(new_order.getId());
                    newOrderItem.setNum(splitNum);
                    newOrderItem.setMoney(entireOrderItem.getMoney()*splitNum/totalNum);
                    newOrderItem.setPayMoney(entireOrderItem.getPayMoney()*splitNum/totalNum);
                    orderItemMapper.insert(newOrderItem);

                    //更新旧订单明细统计数据
                    entireOrderItem.setNum(entireOrderItem.getNum()-newOrderItem.getNum());
                    entireOrderItem.setMoney(entireOrderItem.getMoney()-newOrderItem.getMoney());
                    entireOrderItem.setPayMoney(entireOrderItem.getPayMoney()-newOrderItem.getPayMoney());
                    entireOrderItem.setWeight(entireOrderItem.getWeight()-newOrderItem.getWeight());
                    entireOrderItem.setPostFee(entireOrderItem.getPostFee()-newOrderItem.getPostFee());
                    orderItemMapper.updateByPrimaryKeySelective(entireOrderItem);
                }
            }
        }

        //迭代完成后操作订单
        orderMapper.insert(new_order);
        orderMapper.updateByPrimaryKey(old_order);

        //日志记录
        OrderLog orderLog = new OrderLog();
        orderLog.setId(idWorker.nextId()+"");
        orderLog.setOrderId(Long.parseLong(old_order.getId()));
        orderLog.setOperater("admin");
        orderLog.setOperateTime(new Date());
        orderLog.setRemarks("订单:"+old_order.getId()+"被拆分");
        orderLogMapper.insert(orderLog);
    }

    @Override
    public void updatePayStatus(String orderId, String transactionId) {
        //1.先查询出该订单
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(order!=null && "0".equals(order.getPayStatus())){ //订单存在并且状态为未支付
            order.setPayStatus("1");  //支付状态：已支付
            order.setOrderStatus("1"); //订单状态：已支付订单
            order.setUpdateTime(new Date()); //更新时间
            order.setPayTime(new Date()); //支付时间
            order.setTransactionId(transactionId); //交易流水号
            orderMapper.updateByPrimaryKeySelective(order);   //更新订单

            //记录日志
            OrderLog orderLog = new OrderLog();
            orderLog.setOperater("system");
            orderLog.setOperateTime(new Date());
            orderLog.setOrderId(Long.parseLong(orderId));
            orderLog.setId(idWorker.nextId()+"");
            orderLog.setPayStatus("1");
            orderLog.setOrderStatus("1");
            orderLog.setRemarks("支付流水号: "+transactionId);
            orderLogMapper.insert(orderLog);
        }
    }
    @Autowired
    private WXPayService wxPayService;

    /**
     * 调用微信支付关闭订单
     * @param orderId 订单id
     */
    @Override
    public void closeOrderLogic(String orderId) {

        wxPayService.closeOrder(orderId);
    }

    /**
     * 构建查询条件
     *
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 订单id
            if (searchMap.get("id") != null && !"".equals(searchMap.get("id"))) {
                criteria.andLike("id", "%" + searchMap.get("id") + "%");
            }
            // 支付类型，1、在线支付、0 货到付款
            if (searchMap.get("payType") != null && !"".equals(searchMap.get("payType"))) {
                criteria.andLike("payType", "%" + searchMap.get("payType") + "%");
            }
            // 物流名称
            if (searchMap.get("shippingName") != null && !"".equals(searchMap.get("shippingName"))) {
                criteria.andLike("shippingName", "%" + searchMap.get("shippingName") + "%");
            }
            // 物流单号
            if (searchMap.get("shippingCode") != null && !"".equals(searchMap.get("shippingCode"))) {
                criteria.andLike("shippingCode", "%" + searchMap.get("shippingCode") + "%");
            }
            // 用户名称
            if (searchMap.get("username") != null && !"".equals(searchMap.get("username"))) {
                criteria.andLike("username", "%" + searchMap.get("username") + "%");
            }
            // 买家留言
            if (searchMap.get("buyerMessage") != null && !"".equals(searchMap.get("buyerMessage"))) {
                criteria.andLike("buyerMessage", "%" + searchMap.get("buyerMessage") + "%");
            }
            // 是否评价
            if (searchMap.get("buyerRate") != null && !"".equals(searchMap.get("buyerRate"))) {
                criteria.andLike("buyerRate", "%" + searchMap.get("buyerRate") + "%");
            }
            // 收货人
            if (searchMap.get("receiverContact") != null && !"".equals(searchMap.get("receiverContact"))) {
                criteria.andLike("receiverContact", "%" + searchMap.get("receiverContact") + "%");
            }
            // 收货人手机
            if (searchMap.get("receiverMobile") != null && !"".equals(searchMap.get("receiverMobile"))) {
                criteria.andLike("receiverMobile", "%" + searchMap.get("receiverMobile") + "%");
            }
            // 收货人地址
            if (searchMap.get("receiverAddress") != null && !"".equals(searchMap.get("receiverAddress"))) {
                criteria.andLike("receiverAddress", "%" + searchMap.get("receiverAddress") + "%");
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if (searchMap.get("sourceType") != null && !"".equals(searchMap.get("sourceType"))) {
                criteria.andLike("sourceType", "%" + searchMap.get("sourceType") + "%");
            }
            // 交易流水号
            if (searchMap.get("transactionId") != null && !"".equals(searchMap.get("transactionId"))) {
                criteria.andLike("transactionId", "%" + searchMap.get("transactionId") + "%");
            }
            // 订单状态
            if (searchMap.get("orderStatus") != null && !"".equals(searchMap.get("orderStatus"))) {
                criteria.andLike("orderStatus", "%" + searchMap.get("orderStatus") + "%");
            }
            // 支付状态
            if (searchMap.get("payStatus") != null && !"".equals(searchMap.get("payStatus"))) {
                criteria.andLike("payStatus", "%" + searchMap.get("payStatus") + "%");
            }
            // 发货状态
            if (searchMap.get("consignStatus") != null && !"".equals(searchMap.get("consignStatus"))) {
                criteria.andLike("consignStatus", "%" + searchMap.get("consignStatus") + "%");
            }
            // 是否删除
            if (searchMap.get("isDelete") != null && !"".equals(searchMap.get("isDelete"))) {
                criteria.andLike("isDelete", "%" + searchMap.get("isDelete") + "%");
            }

            // 数量合计
            if (searchMap.get("totalNum") != null) {
                criteria.andEqualTo("totalNum", searchMap.get("totalNum"));
            }
            // 金额合计
            if (searchMap.get("totalMoney") != null) {
                criteria.andEqualTo("totalMoney", searchMap.get("totalMoney"));
            }
            // 优惠金额
            if (searchMap.get("preMoney") != null) {
                criteria.andEqualTo("preMoney", searchMap.get("preMoney"));
            }
            // 邮费
            if (searchMap.get("postFee") != null) {
                criteria.andEqualTo("postFee", searchMap.get("postFee"));
            }
            // 实付金额
            if (searchMap.get("payMoney") != null) {
                criteria.andEqualTo("payMoney", searchMap.get("payMoney"));
            }

            //根据id数组查询
            if (searchMap.get("ids") != null) {
                criteria.andIn("id", (List) searchMap.get("ids"));
            }

        }
        return example;
    }

}
