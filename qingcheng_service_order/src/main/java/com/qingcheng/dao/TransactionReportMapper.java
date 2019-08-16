package com.qingcheng.dao;

import com.qingcheng.pojo.order.TransactionReport;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.time.LocalDate;
import java.util.List;

public interface TransactionReportMapper extends Mapper<TransactionReport> {

    /**
     * 下单人数
     */
    @Select("SELECT COUNT(*) orderUserNum FROM " +
            "(SELECT username,COUNT(username) " +
            "FROM tb_order " +
            "WHERE DATE_FORMAT(pay_time,'%Y-%m-%d') = #{date} " +
            "GROUP BY username,DATE_FORMAT(pay_time,'%Y-%m-%d')) t;")
    public Integer getOrderUserNum(@Param("date") LocalDate localDate);

    /**
     * 订单数
     * @return
     */
    @Select("SELECT COUNT(*) orderNum FROM tb_order WHERE DATE_FORMAT(pay_time,'%Y-%m-%d') = #{date};")
    public Integer getOrderNum(@Param("date") LocalDate localDate);

    /**
     * 下单件数
     */
    @Select("select sum(total_num) orderPieceNum " +
            "from tb_order " +
            "WHERE DATE_FORMAT(pay_time,'%Y-%m-%d') = #{date};")
    public Integer getOrderPieceNum(@Param("date") LocalDate localDate);

    /**
     * 有效订单数
     */
    @Select("SELECT COUNT(*) effectiveOrderNum " +
            "FROM tb_order " +
            "WHERE DATE_FORMAT(pay_time,'%Y-%m-%d') = #{date} AND pay_status='1';")
    public Integer getEfectiveOrderNum(@Param("date") LocalDate localDate);

    /**
     * 下单金额
     */
    @Select("select sum(total_money) orderMoney  from tb_order WHERE DATE_FORMAT(pay_time,'%Y-%m-%d') = #{date};")
    public Integer getOrderMoney(@Param("date") LocalDate localDate);

    /**
     * 退款金额
     */
    @Select("SELECT SUM(pay_money) returnMoney FROM tb_order WHERE DATE_FORMAT(pay_time,'%Y-%m-%d') = #{date} AND pay_status = '2';")
    public Integer getReturnMoney(@Param("date") LocalDate localDate);

    /**
     * 付款人数
     */
    @Select("select count(*) payUserNum from " +
            "(select username,count(username) " +
            "from tb_order " +
            "WHERE DATE_FORMAT(pay_time,'%Y-%m-%d') = '2019-04-15' and pay_status='1' " +
            "group by username,DATE_FORMAT(pay_time,'%Y-%m-%d')) t;")
    public Integer getPayUserNum(@Param("date") LocalDate localDate);

    /**
     * 付款订单数
     */
    @Select("SELECT COUNT(*) payOrderNum FROM tb_order WHERE DATE_FORMAT(pay_time,'%Y-%m-%d') = #{date} and pay_status='1';")
    public Integer getPayOrderNum(@Param("date") LocalDate localDate);


    /**
     * 付款件数
     */
    @Select("select sum(total_num) payPieceNum " +
            "from tb_order " +
            "WHERE DATE_FORMAT(pay_time,'%Y-%m-%d') = #{date} and pay_status='1';")
    public Integer getPayPieceNum(@Param("date") LocalDate localDate);

    /**
     * 付款金额
     */
    @Select("SELECT SUM(pay_money) payMoney " +
            "FROM tb_order " +
            "WHERE DATE_FORMAT(pay_time,'%Y-%m-%d') = #{date} AND pay_status = '1'")
    public Integer getPayMoney(@Param("date") LocalDate localDate);

    /**
     * 按时间段统计交易数据
     * @param startDate
     * @param endDate
     * @return
     */
    @Select("SELECT count_date countDate,uv,order_user_num orderUserNum,order_num orderNum, " +
            "       order_piece_num orderPieceNum,effective_order_num effectiveOrderNum, " +
            "       order_money orderMoney,return_money returnMoney,pay_user_num payUserNum, " +
            "       pay_order_num payOrderNum,pay_piece_num payPieceNum,pay_money payMoney,atv " +
            " FROM tb_transaction_report;")
    public List<TransactionReport> transactionReport(@Param("startDate") String startDate, @Param("endDate") String endDate);


    @Select("SELECT count_date countDate, SUM(uv) uv,SUM(order_user_num) orderUserNum,SUM(order_num) orderNum,SUM(order_piece_num) orderPieceNum, " +
            "SUM(effective_order_num) effectiveOrderNum,SUM(order_money) orderMoney,SUM(return_money) returnMoney, " +
            "SUM(pay_user_num) payUserNum,SUM(pay_order_num) payOrderNum,SUM(pay_piece_num) payPieceNum, " +
            "SUM(pay_money) payMoney,SUM(atv) atv " +
            "FROM tb_transaction_report  " +
            "WHERE count_date >= #{startDate} AND count_date <= #{endDate};")
    public List<TransactionReport> transactionReport2(@Param("startDate") String startDate,@Param("endDate") String endDate);

}
