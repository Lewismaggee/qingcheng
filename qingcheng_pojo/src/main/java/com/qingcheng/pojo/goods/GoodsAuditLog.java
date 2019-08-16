package com.qingcheng.pojo.goods;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
/**
 * goodsAuditLog实体类
 * @author Administrator
 *
 */
@Table(name="tb_goods_audit_log")
public class GoodsAuditLog implements Serializable{

	@Id
	private String id;//主键

	private String spuId;//SPUID

	private String auditor;//审核人

	private java.util.Date auditTime;//审核时间

	private String auditMessage;//审核意见

	private String operateLogId;//商品日志ID

	
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

	public String getAuditor() {
		return auditor;
	}
	public void setAuditor(String auditor) {
		this.auditor = auditor;
	}

	public java.util.Date getAuditTime() {
		return auditTime;
	}
	public void setAuditTime(java.util.Date auditTime) {
		this.auditTime = auditTime;
	}

	public String getAuditMessage() {
		return auditMessage;
	}
	public void setAuditMessage(String auditMessage) {
		this.auditMessage = auditMessage;
	}

	public String getOperateLogId() {
		return operateLogId;
	}
	public void setOperateLogId(String operateLogId) {
		this.operateLogId = operateLogId;
	}


	
}
