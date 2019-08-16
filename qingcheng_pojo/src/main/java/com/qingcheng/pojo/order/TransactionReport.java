package com.qingcheng.pojo.order;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * transactionReport实体类
 * @author Administrator
 *
 */
@Table(name="tb_transaction_report")
public class TransactionReport implements Serializable{

	private java.util.Date countDate;//统计日期

	private Integer uv;//浏览人数

	private Integer orderUserNum;//下单人数

	private Integer orderNum;//订单数

	private Integer orderPieceNum;//下单件数

	private Integer effectiveOrderNum;//有效订单数

	private Integer orderMoney;//下单金额

	private Integer returnMoney;//退款金额

	private Integer payUserNum;//付款人数

	private Integer payOrderNum;//付款订单数

	private Integer payPieceNum;//付款件数

	private Integer payMoney;//付款金额

	private Integer atv;//客单价



	public java.util.Date getCountDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(countDate);
		try {
			return sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	public void setCountDate(java.util.Date countDate) {
		this.countDate = countDate;
	}

	public Integer getUv() {
		return uv;
	}
	public void setUv(Integer uv) {
		this.uv = uv;
	}

	public Integer getOrderUserNum() {
		return orderUserNum;
	}
	public void setOrderUserNum(Integer orderUserNum) {
		this.orderUserNum = orderUserNum;
	}

	public Integer getOrderNum() {
		return orderNum;
	}
	public void setOrderNum(Integer orderNum) {
		this.orderNum = orderNum;
	}

	public Integer getOrderPieceNum() {
		return orderPieceNum;
	}
	public void setOrderPieceNum(Integer orderPieceNum) {
		this.orderPieceNum = orderPieceNum;
	}

	public Integer getEffectiveOrderNum() {
		return effectiveOrderNum;
	}
	public void setEffectiveOrderNum(Integer effectiveOrderNum) {
		this.effectiveOrderNum = effectiveOrderNum;
	}

	public Integer getOrderMoney() {
		return orderMoney;
	}
	public void setOrderMoney(Integer orderMoney) {
		this.orderMoney = orderMoney;
	}

	public Integer getReturnMoney() {
		return returnMoney;
	}
	public void setReturnMoney(Integer returnMoney) {
		this.returnMoney = returnMoney;
	}

	public Integer getPayUserNum() {
		return payUserNum;
	}
	public void setPayUserNum(Integer payUserNum) {
		this.payUserNum = payUserNum;
	}

	public Integer getPayOrderNum() {
		return payOrderNum;
	}
	public void setPayOrderNum(Integer payOrderNum) {
		this.payOrderNum = payOrderNum;
	}

	public Integer getPayPieceNum() {
		return payPieceNum;
	}
	public void setPayPieceNum(Integer payPieceNum) {
		this.payPieceNum = payPieceNum;
	}

	public Integer getPayMoney() {
		return payMoney;
	}
	public void setPayMoney(Integer payMoney) {
		this.payMoney = payMoney;
	}

	public Integer getAtv() {
		return atv;
	}
	public void setAtv(Integer atv) {
		this.atv = atv;
	}


	
}


