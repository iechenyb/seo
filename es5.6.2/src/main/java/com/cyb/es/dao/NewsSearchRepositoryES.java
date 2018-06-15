package com.cyb.es.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.cyb.es.document.ESNews;

public interface NewsSearchRepositoryES extends ElasticsearchRepository<ESNews, Long>{
	 //term是代表完全匹配，即不进行分词器分析，文档中必须包含整个搜索的词汇 title
	@Query("{\"bool\" : {\"must\" : {\"term\" : {\"title\" : \"?0\"}}}}")
	Page<ESNews> findByNewsTerm(String title, Pageable pageable);
	//
	@Query("{\"query\":{\"match\":{\"title\":\"?0\"}}}")
    Page<ESNews> findByNewsMatch(String title, Pageable pageable);
	
	@Query("{\"query\": {\"multi_match\": {\"query\": \"?0\",\"type\": \"best_fields\",\"fields\": [\"title\"],\"tie_breaker\": 0.9}}")
    List<ESNews> findByNewsMutilMatch(String title);
	
	@Query("{\"bool\" : {\"must\" : {\"term\" : {\"title\" : \"?0\"}}}}")
    List<ESNews> findByNews(String title);
}
