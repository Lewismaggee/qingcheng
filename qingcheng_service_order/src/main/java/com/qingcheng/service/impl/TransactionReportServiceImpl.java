package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.TransactionReportMapper;
import com.qingcheng.pojo.order.TransactionReport;
import com.qingcheng.service.order.TransactionReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service(interfaceClass = TransactionReportService.class)
public class TransactionReportServiceImpl implements TransactionReportService {

    @Autowired
    private TransactionReportMapper transactionReportMapper;


    @Override
    public Integer getOrderUserName(LocalDate localDate) {
        return transactionReportMapper.getOrderUserNum(localDate);
    }

    @Override
    public Integer getOrderNum(LocalDate localDate) {
        return transactionReportMapper.getOrderNum(localDate);
    }

    @Override
    public Integer getOrderPieceNum(LocalDate localDate) {
        return transactionReportMapper.getOrderPieceNum(localDate);
    }

    @Override
    public Integer getEfectiveOrderNum(LocalDate localDate) {
        return transactionReportMapper.getEfectiveOrderNum(localDate);
    }

    @Override
    public Integer getOrderMoney(LocalDate localDate) {
        return transactionReportMapper.getOrderMoney(localDate);
    }

    @Override
    public Integer getReturnMoney(LocalDate localDate) {
        return transactionReportMapper.getReturnMoney(localDate);
    }

    @Override
    public Integer getPayUserNum(LocalDate localDate) {
        return transactionReportMapper.getPayUserNum(localDate);
    }

    @Override
    public Integer getPayOrderNum(LocalDate localDate) {
        return transactionReportMapper.getPayOrderNum(localDate);
    }

    @Override
    @Transactional
    public void createData(LocalDate localDate) {
        //将LocalDate转换为Date
        Instant instant = localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        TransactionReport transactionReport = new TransactionReport();
        transactionReport.setCountDate(Date.from(instant));
        transactionReport.setUv(50000);
        transactionReport.setEffectiveOrderNum(transactionReportMapper.getEfectiveOrderNum(localDate));
        transactionReport.setOrderMoney(transactionReportMapper.getOrderMoney(localDate));
        transactionReport.setOrderNum(transactionReportMapper.getOrderNum(localDate));
        transactionReport.setOrderPieceNum(transactionReportMapper.getOrderPieceNum(localDate));
        transactionReport.setOrderUserNum(transactionReportMapper.getOrderUserNum(localDate));
        transactionReport.setPayMoney(transactionReportMapper.getPayMoney(localDate));
        transactionReport.setPayOrderNum(transactionReportMapper.getPayOrderNum(localDate));
        transactionReport.setPayPieceNum(transactionReportMapper.getPayPieceNum(localDate));
        transactionReport.setPayUserNum(transactionReportMapper.getPayUserNum(localDate));
        transactionReport.setReturnMoney(transactionReportMapper.getReturnMoney(localDate));
        if(transactionReport.getPayMoney()!=null && transactionReport.getPayUserNum()!=null){
            transactionReport.setAtv(transactionReport.getPayMoney()/transactionReport.getPayUserNum());
        }

        transactionReportMapper.insert(transactionReport);
    }

    @Override
    public List<TransactionReport> transactionReport(String startDate, String endDate) {
        return transactionReportMapper.transactionReport(startDate, endDate);
    }

    @Override
    public List<TransactionReport> transactionReport2(String startDate, String endDate) {
        return transactionReportMapper.transactionReport2(startDate, endDate);
    }
}
