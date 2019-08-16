package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.goods.SkuSearchService;
import com.qingcheng.util.WebUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class SearchController {
    @Reference
    private SkuSearchService skuSearchService;

    @GetMapping("/search")
    public String search(Model model, @RequestParam Map<String,String> searchMap) throws Exception {
        //字符集处理(解决中文乱码问题)
        searchMap = WebUtil.convertCharsetToUTF8(searchMap);

        //页码容错,没有页码,默认第一页
        if(searchMap.get("pageNo") == null){
            searchMap.put("pageNo","1");
        }

        //排序: 页面传递过来2个参数: sort 排序字段   sortOrder: 排序规则(升序/降序)
        if(searchMap.get("sort")==null){
            searchMap.put("sort","");
        }

        if(searchMap.get("sortOrder")==null){
            searchMap.put("sortOrder","DESC");//
        }

        //远程调用接口
        Map result = skuSearchService.search(searchMap);
        model.addAttribute("result", result);

        //url处理
        StringBuilder url = new StringBuilder("/search.do?");
        for(String key : searchMap.keySet()){
            url.append("&"+key+"="+searchMap.get(key));
        }
        model.addAttribute("url",url);
        model.addAttribute("searchMap",searchMap);

        //页码
        Integer pageNo = Integer.parseInt(searchMap.get("pageNo"));
        model.addAttribute("pageNo",pageNo);
        //总页码数
        Long totalPages = (Long) result.get("totalPages");
        //开始页码
        int startPage = 1;
        //结束页码
        int endPage = totalPages.intValue();
        if(endPage > 5){
            startPage = pageNo - 2;
            if(startPage < 1){
                startPage = 1;
            }
            endPage = startPage + 4;
        }
        model.addAttribute("startPage",startPage);
        model.addAttribute("endPage",endPage);


        return "search";
    }
}
