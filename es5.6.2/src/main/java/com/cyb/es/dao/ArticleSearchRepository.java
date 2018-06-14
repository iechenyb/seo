package com.cyb.es.dao;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.cyb.es.document.Article;

public interface ArticleSearchRepository extends ElasticsearchRepository<Article, Long>{

}
