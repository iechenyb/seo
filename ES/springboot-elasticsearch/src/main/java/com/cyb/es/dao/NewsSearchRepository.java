package com.cyb.es.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cyb.es.bean.News;
//普通的hibernate查询
public interface NewsSearchRepository extends JpaRepository<News, Long>{

}
