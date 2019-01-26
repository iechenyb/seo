package com.cyb.es.dao;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.cyb.es.document.ESNews;
//只要申明，就会在启动的时候检查是否有T的索引，没有的话，直接创建所以到es服务器
public interface NewsSearchRepositoryES extends ElasticsearchRepository<ESNews, Long>{

}
