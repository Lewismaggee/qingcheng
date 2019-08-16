package com.qingcheng.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.order.CategoryReportService;
import com.qingcheng.service.order.OrderService;
import com.qingcheng.service.order.TransactionReportService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/*
    定时任务
 */
/*@Component
public class OrderTask {

    @Reference
    private OrderService orderService;
    @Reference
    private CategoryReportService categoryReportService;

    @Reference
    private TransactionReportService transactionReportService;



    *//*@Scheduled(cron = "* * * * * ?") //每秒执行一次
    public void orderTimeOutLogic(){
        System.out.println(new Date());
    }*//*

    //每30分钟处理一次请求,将60分钟前未付款订单关闭
    @Scheduled(cron = " * 0 0 0/6 *?")
    public void orderTimeOutLogic(){
        System.out.println("每隔2分钟执行一次任务....."+new Date());
        orderService.orderTimeOutLogic();
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void createCategoryReportData(){
        System.out.println("createCategoryReportData..........");
        categoryReportService.createData();
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void createTransactionReportData(){
        System.out.println("createTransactionReportData..........");
        LocalDate localDate = LocalDate.parse("2019-04-15", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        transactionReportService.createData(localDate);
    }


}*/
