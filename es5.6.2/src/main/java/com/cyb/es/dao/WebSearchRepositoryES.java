package com.cyb.es.dao;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.cyb.es.document.WebDocument;

public interface WebSearchRepositoryES extends ElasticsearchRepository<WebDocument, Long>{

}
