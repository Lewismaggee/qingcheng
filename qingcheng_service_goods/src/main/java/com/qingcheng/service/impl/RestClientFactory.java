package com.qingcheng.service.impl;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * elasticsearch索引库操作客户端工厂类
 */
public class RestClientFactory {
    public static RestHighLevelClient getRestHighLevelClient(String hostname,int port){

        HttpHost httpHost = new HttpHost(hostname,port,"http");
        //构建器
        RestClientBuilder builder = RestClient.builder(httpHost);
        //高级客户端对象(连接)
        return new RestHighLevelClient(builder);

    }
}
