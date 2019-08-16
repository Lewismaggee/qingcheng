package com.qingcheng.service.order;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.TransactionReport;

import java.time.LocalDate;
import java.util.*;

/**
 * transactionReport业务逻辑层
 */
public interface TransactionReportService {

    public Integer getOrderUserName(LocalDate localDate);

    public Integer getOrderNum(LocalDate localDate);

    public Integer getOrderPieceNum(LocalDate localDate);

    public Integer getEfectiveOrderNum(LocalDate localDate);

    public Integer getOrderMoney( LocalDate localDate);

    public Integer getReturnMoney(LocalDate localDate);

    public Integer getPayUserNum(LocalDate localDate);

    public Integer getPayOrderNum(LocalDate localDate);

    public void createData(LocalDate localDate);

    public List<TransactionReport> transactionReport(String startDate,String endDate);

    public List<TransactionReport> transactionReport2(String startDate,String endDate);

}
