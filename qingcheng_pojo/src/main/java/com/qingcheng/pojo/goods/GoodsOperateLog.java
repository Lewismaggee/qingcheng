package com.qingcheng.pojo.goods;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
/**
 * goodsOperateLog实体类
 * @author Administrator
 *
 */
@Table(name="tb_goods_operate_log")
public class GoodsOperateLog implements Serializable{

	@Id
	private String id;//主键


	

	private String spuId;//SPUID

	private String operator;//操作员

	private java.util.Date operateTime;//操作时间

	private String auditId;//审核记录ID

	private String isMarketableOld;//原：是否上架

	private String isMarketableNow;//现：是否上架

	private String isDeleteOld;//原：是否删除

	private String isDeleteNow;//现：是否删除

	private String statusOld;//原：审核状态

	private String statusNow;//现：审核状态

	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getSpuId() {
		return spuId;
	}
	public void setSpuId(String spuId) {
		this.spuId = spuId;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public java.util.Date getOperateTime() {
		return operateTime;
	}
	public void setOperateTime(java.util.Date operateTime) {
		this.operateTime = operateTime;
	}

	public String getAuditId() {
		return auditId;
	}
	public void setAuditId(String auditId) {
		this.auditId = auditId;
	}

	public String getIsMarketableOld() {
		return isMarketableOld;
	}
	public void setIsMarketableOld(String isMarketableOld) {
		this.isMarketableOld = isMarketableOld;
	}

	public String getIsMarketableNow() {
		return isMarketableNow;
	}
	public void setIsMarketableNow(String isMarketableNow) {
		this.isMarketableNow = isMarketableNow;
	}

	public String getIsDeleteOld() {
		return isDeleteOld;
	}
	public void setIsDeleteOld(String isDeleteOld) {
		this.isDeleteOld = isDeleteOld;
	}

	public String getIsDeleteNow() {
		return isDeleteNow;
	}
	public void setIsDeleteNow(String isDeleteNow) {
		this.isDeleteNow = isDeleteNow;
	}

	public String getStatusOld() {
		return statusOld;
	}
	public void setStatusOld(String statusOld) {
		this.statusOld = statusOld;
	}

	public String getStatusNow() {
		return statusNow;
	}
	public void setStatusNow(String statusNow) {
		this.statusNow = statusNow;
	}


	
}
