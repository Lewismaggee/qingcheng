package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.qingcheng.pojo.goods.Category;
import com.qingcheng.pojo.goods.Goods;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.goods.Spu;
import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/item")
public class ItemController {

    @Reference
    private SpuService spuService;

    @Reference
    private CategoryService categoryService;

    @Value("${pagePath}") //从配置文件读取文件路径
    private String pagePath;

    @Autowired
    private TemplateEngine templateEngine;

    /**
     * 生成页面逻辑
     * @param spuId
     */
    @GetMapping("/createPage")
    public void createPage(String spuId){ //根据spuId批量生成页面: 一个skuId对应一个页面
        //1.查询商品信息
        Goods goods = spuService.findGoodsById(spuId);
        //1.1 获取spu信息
        Spu spu = goods.getSpu();

        //1.2 获取sku列表
        List<Sku> skuList = goods.getSkuList();

        //1.3 查询商品分类
        List<String> categoryList = new ArrayList<String>();
        categoryList.add(categoryService.findById(spu.getCategory1Id()).getName()); //一级分类
        categoryList.add(categoryService.findById(spu.getCategory2Id()).getName()); //二级分类
        categoryList.add(categoryService.findById(spu.getCategory3Id()).getName()); //三级分类

        //构建sku地址列表
        //1.将当前spu这个商品下的所有sku的页面全部找到，存到一个map集合
        //2.以具体某种规格组合的json字符串作为key ,以对应url作为值，当点击的时候，我再获取这个规格组合转换成json字符串，
        //3.通过这个json字符串到map中提取，就可以把对应Url提取过来，然后加到当前数据模型中，然后页面就可以取出url
        Map<String,String> urlMap = new HashMap<String,String>();
        for(Sku sku : skuList){
            //以规格组合的json字符串作为key
            if("1".equals(sku.getStatus())){ //有效的规格
                String specJson = JSON.toJSONString(JSON.parseObject(sku.getSpec()), SerializerFeature.MapSortField);
                urlMap.put(specJson,sku.getId()+".html");
            }

        }

        //2.批量生成页面
        for(Sku sku : skuList){
            //(1) 创建上下文和数据模型
            Context context = new Context();
            Map<String,Object> dataModel = new HashMap<String,Object>();
            dataModel.put("spu",spu);
            dataModel.put("sku",sku);
            dataModel.put("categoryList",categoryList);
            dataModel.put("skuImages",sku.getImages().split(",")); //sku图片列表
            dataModel.put("spuImages",spu.getImages().split(",")); //spu图片列表

            //参数列表
            Map paraItems = JSON.parseObject(spu.getParaItems());
            dataModel.put("paraItems",paraItems);
            //规格列表
            Map<String,String> specItems = (Map) JSON.parseObject(sku.getSpec()); //当前sku的规格
            dataModel.put("specItems",specItems);

            //规格面板
            //{"颜色":["枪色","黑色"],"尺码":["250度","200度","100度","150度","300度"]}
            //{"颜色":[{"option":"枪色","checked":"true"},{"option":"黑色","checked":"false"}],"尺码":["250度","200度","100度","150度","300度"]}
            Map<String,List> specMap =(Map) JSON.parseObject(spu.getSpecItems()); //规格和规格选项
            for(String key : specMap.keySet()){ //循环规格
                List<String> list = specMap.get(key); //["枪色","黑色"]

                List<Map> mapList = new ArrayList<Map>();//构建新的集合  [{"option":"枪色","checked":"true"},{"option":"黑色","checked":"false"}]
                //循环规格选项
                for(String value : list){
                    Map map = new HashMap();
                    map.put("option",value);
                    //当前规格:{"颜色","黑色","尺码":"200度"}
                    if(specItems.get(key).equals(value)){
                        map.put("checked",true);
                    }else{
                        map.put("checked",false);
                    }
                    //当前sku 规格
                    Map<String,String> spec = (Map)JSON.parseObject(sku.getSpec());
                    spec.put(key,value);//当前点击的
                    String specJson = JSON.toJSONString(spec, SerializerFeature.MapSortField);

                    map.put("url",urlMap.get(specJson));
                    mapList.add(map);
                }
                specMap.put(key,mapList); //用新的集合替换原有的集合
            }


            dataModel.put("specMap",specMap);

            context.setVariables(dataModel);

            //(2) 准备文件
            File dir = new File(pagePath);
            if(!dir.exists()){
                dir.mkdirs();
            }
            File dest = new File(dir,sku.getId()+".html");

            //(3) 生成页面
            try {
                PrintWriter writer = new PrintWriter(dest,"UTF-8");
                templateEngine.process("item",context,writer);
                System.out.println("生成页面" + sku.getId()+".html");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }

    }
}
