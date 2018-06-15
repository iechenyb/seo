package com.cyb.es.controller;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
/**
 *作者 : iechenyb<br>
 *类描述: 说点啥<br>
 *创建时间: 2018年6月11日
 */
import org.springframework.web.bind.annotation.RestController;

import com.cyb.es.dao.NewsSearchRepositoryES;
import com.cyb.es.document.ESNews;
/**
 * 注解json查询
 * @author DHUser
 *
 */
@RestController
@RequestMapping("newsJson")
public class NewsJsonController {
	Log log = LogFactory.getLog(NewsJsonController.class);
	
	@Autowired
	NewsSearchRepositoryES es;
	
	@GetMapping("findByNewsMatchPage")
	public List<ESNews> findByNewsPage(String title) {
		@SuppressWarnings("deprecation")
		Pageable page = new PageRequest(0,10);
		Page<ESNews> list = es.findByNewsMatch(title,page);
		return list.getContent();
	}
	
	@GetMapping("findByNews")
	public List<ESNews> findByNews(String title) {
		List<ESNews> list = es.findByNewsMutilMatch(title);
		return list;
	}
}
