package com.cyb.es.dao;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.cyb.es.document.News;

public interface NewsSearchRepositoryES extends ElasticsearchRepository<News, Long>{

}
