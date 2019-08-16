package com.qingcheng.service.order;

import com.qingcheng.pojo.order.CategoryReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/*
    报表业务接口
 */
public interface CategoryReportService {
    /**
     * 商品类目按日期统计(订单表关联查询)
     * @param date
     * @return
     */
    public List<CategoryReport> categoryReport(LocalDate date);

    /**
     * 定时任务将订单详情表中数据同步到tb_category_report表中
     */
    public void createData();

    /**
     * 按一级分类统计
     * @param startDate
     * @param endDate
     * @return
     */
    public List<Map> category1Count(String startDate,String endDate);
}
