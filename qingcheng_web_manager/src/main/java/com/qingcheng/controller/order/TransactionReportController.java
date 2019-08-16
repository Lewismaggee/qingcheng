package com.qingcheng.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.PageResult;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.order.TransactionReport;
import com.qingcheng.service.order.TransactionReportService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/transactionReport")
public class TransactionReportController {

    @Reference
    private TransactionReportService transactionReportService;

    @RequestMapping("/getOrderUserNum")
    public Integer getOrderUserNum(){
        LocalDate localDate = LocalDate.parse("2019-04-15", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return transactionReportService.getOrderUserName(localDate);
    }

    @RequestMapping("/transactionCount")
    public List<TransactionReport> transactionCount(){
        String startDate = "2019-01-01";
        String endDate = "2019-12-30";
        return transactionReportService.transactionReport(startDate, endDate);
    }

    @RequestMapping("/transactionCount2")
    public List<TransactionReport> transactionCount2(){
        String startDate = "2019-01-01";
        String endDate = "2019-12-30";
        return transactionReportService.transactionReport2(startDate,endDate);
    }

}
