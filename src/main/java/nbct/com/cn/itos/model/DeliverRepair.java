package nbct.com.cn.itos.model;

import java.time.LocalDateTime;
import io.vertx.core.json.JsonObject;
import nbct.com.cn.itos.jdbc.RowMapper;
import util.DateUtil;

/**
 * @author PJ
 * @version 创建时间：2020年1月9日 上午9:49:05
 */
public class DeliverRepair implements RowMapper<DeliverRepair> {

	private String drId;

	private String taskId;

	private LocalDateTime deliverDate;

	private LocalDateTime receiptDate;

	private String invoiceNumber;

	private String amount;

	private String remark;

	public DeliverRepair from(JsonObject j) {
		try {
			DeliverRepair dr = new DeliverRepair();
			dr.setDrId(j.getString("DRID"));
			dr.setTaskId(j.getString("TASKID"));
			dr.setDeliverDate(DateUtil.utcToLocalDT(j.getString("DELIVERDATE")));
			dr.setReceiptDate(DateUtil.utcToLocalDT(j.getString("RECEIPTDATE")));
			dr.setInvoiceNumber(j.getString("INVOICENUMBER"));
			dr.setAmount(j.getString("AMOUNT"));
			dr.setRemark(j.getString("REMARK"));
			return dr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("后台数据转换成委外修理记录时发生错误。");
		}
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public LocalDateTime getDeliverDate() {
		return deliverDate;
	}

	public void setDeliverDate(LocalDateTime deliverDate) {
		this.deliverDate = deliverDate;
	}

	public LocalDateTime getReceiptDate() {
		return receiptDate;
	}

	public void setReceiptDate(LocalDateTime receiptDate) {
		this.receiptDate = receiptDate;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getDrId() {
		return drId;
	}

	public void setDrId(String drId) {
		this.drId = drId;
	}

}
