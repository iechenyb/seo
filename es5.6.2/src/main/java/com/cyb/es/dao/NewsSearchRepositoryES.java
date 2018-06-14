package com.cyb.es.dao;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.cyb.es.document.ESNews;

public interface NewsSearchRepositoryES extends ElasticsearchRepository<ESNews, Long>{

}
