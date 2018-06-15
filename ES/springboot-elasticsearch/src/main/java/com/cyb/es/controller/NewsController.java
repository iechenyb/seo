package com.cyb.es.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.index.query.WrapperQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
/**
 *作者 : iechenyb<br>
 *类描述: 说点啥<br>
 *创建时间: 2018年6月11日
 */
import org.springframework.web.bind.annotation.RestController;

import com.cyb.es.bean.DBNews;
import com.cyb.es.dao.NewsSearchRepository;
import com.cyb.es.dao.NewsSearchRepositoryES;
import com.cyb.es.document.ESNews;

import io.swagger.annotations.ApiOperation;

/**
 * http://localhost:9200/_cluster/health/?level=shards http://localhost:9200/
 * 
 * @author DHUser
 *
 */
@RestController
@RequestMapping("news")
public class NewsController {

	@Autowired
	NewsSearchRepositoryES es;
	
	@Autowired
	ElasticsearchTemplate template;

	@Autowired
	NewsSearchRepository db;
	/**
	 * 
	 *作者 : iechenyb<br>
	 *方法描述: 更新索引值<br>
	 *创建时间: 2017年7月15日hj12
	 *@param id
	 *@param title
	 *@return
	 */
	@GetMapping("addIndexByClass")
	public boolean addIndexByClass(){
		return template.createIndex(ESNews.class);
	}
	
	@GetMapping("delIndexByClass")
	public boolean delIndexByClass(){
		return template.deleteIndex(ESNews.class);
	}
	
	public void delete(){
		/*db.delete(id);
		db.delete(entity);
		db.delete(collection);
		db.deleteAllInBatch();
		db.deleteInBatch(entities);*/
		
	}
	
	public File getFile(String path) throws FileNotFoundException {
		return ResourceUtils.getFile("classpath:data/" + path);
	}
	/**
	 * 
	 *作者 : iechenyb<br>
	 *方法描述: 更新索引值<br>
	 *创建时间: 2017年7月15日hj12
	 *@param id
	 *@param title
	 *@return
	 */
	@GetMapping("update")
	public Map<String,String> update(Long id ,String title){
		Map<String,String> data = new HashMap<>();
		ESNews news = es.findOne(id);
		data.put("oldTitle", news.getTitle());
		news.setTitle(title);
		es.save(news);
		ESNews news_ = es.findOne(id);
		data.put("newTitle", news_.getTitle());
		return data;
	}
	
	@GetMapping("initNews")
	public List<ESNews> init() throws FileNotFoundException, IOException {
		es.deleteAll();
		db.deleteAll();
	
		Document doc = Jsoup.parse(getFile("news.xml"), "utf-8");
		Elements newsa = doc.select("new");
		for (int i = 0; i < newsa.size(); i++) {
			System.out.println(newsa.get(i).attr("id"));
			System.out.println(newsa.get(i).attr("title"));
			System.out.println(newsa.get(i).text());
			DBNews n = new DBNews();
			n.setId(Long.valueOf(newsa.get(i).attr("id")));
			n.setTitle(newsa.get(i).attr("title"));
			n.setContent(newsa.get(i).text());
			db.save(n);
			
		}
		List<ESNews> data = new ArrayList<ESNews>();
		// 创建索引
		Iterator<DBNews> iterator = db.findAll().iterator();
		while (iterator.hasNext()) {
			DBNews n = iterator.next();
			ESNews news = new ESNews();
			news.setId(n.getId());
			news.setContent(n.getContent());
			news.setTitle(n.getTitle());
			news.setTime(new Date());
			es.save(news);
			data.add(news);
		}
		return data;
	}

	@GetMapping("queryall")
	public List<ESNews> queryAllNews() {
		Iterator<ESNews> iter = es.findAll().iterator();
		List<ESNews> data = new ArrayList<ESNews>();
		while (iter.hasNext()) {
			data.add(iter.next());
		}
		return data;
	}

	@GetMapping("deleteall")
	public List<ESNews> deleteAll() {
		es.deleteAll();
		Iterator<ESNews> iter = es.findAll().iterator();
		List<ESNews> data = new ArrayList<ESNews>();
		while (iter.hasNext()) {
			data.add(iter.next());
		}
		return data;
	}

	/**
	 * 
	 * 作者 : iechenyb<br>
	 * 方法描述: 精确匹配<br>
	 * 创建时间: 2017年7月15日hj12
	 * 
	 * @param key
	 * @return
	 */
	@GetMapping("query")
	@ApiOperation(value="全文匹配，查询所有创建索引的字段",notes="全文匹配，查询所有创建索引的字段")
	public List<ESNews> queryNews(String key) {
		List<ESNews> data = new ArrayList<ESNews>();
		String queryString = key;// 搜索关键字
		QueryStringQueryBuilder builder = new QueryStringQueryBuilder(queryString);
		//builder.analyzer("ik_smart");
		/*QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();*/  
		builder.field("title").field("content");
		Iterable<ESNews> searchResult = es.search(builder);
		Iterator<ESNews> iterator = searchResult.iterator();
		while (iterator.hasNext()) {
			data.add(iterator.next());
		}
		return data;
	}

	/**
	 * 
	 * 作者 : iechenyb<br>
	 * 方法描述: 模糊匹配<br>
	 * 创建时间: 2017年7月15日hj12
	 * 
	 * @param key
	 * @return
	 */
	@GetMapping("wildcardQuery")
	public List<ESNews> wildcardQuery(String key) {
		List<ESNews> data = new ArrayList<ESNews>();
		String queryString = key;// 搜索关键字
		/*WildcardQueryParser parser = new WildcardQueryParser();
		parser.parse(parseContext)*/
		WildcardQueryBuilder builder = new WildcardQueryBuilder("title", queryString);
		//QueryBuilders.wildcardQuery("title","*"+queryString+"*");
		Iterable<ESNews> searchResult = es.search(builder);
		Iterator<ESNews> iterator = searchResult.iterator();
		while (iterator.hasNext()) {
			data.add(iterator.next());
		}
		return data;
	}
	//模糊匹配
	@GetMapping("wrapperQuery")
	public List<ESNews> queryNews3(String key) {
		List<ESNews> data = new ArrayList<ESNews>();
		String queryString = key;// 搜索关键字
		WrapperQueryBuilder builder = new WrapperQueryBuilder(queryString);
		Iterable<ESNews> searchResult = es.search(builder);
		Iterator<ESNews> iterator = searchResult.iterator();
		while (iterator.hasNext()) {
			data.add(iterator.next());
		}
		return data;
	}

	@PostMapping("addNews")
	public List<ESNews> addNews(@RequestBody ESNews news) {
		news.setTime(new Date());
		es.save(news);
		List<ESNews> data = new ArrayList<ESNews>();
		Iterator<ESNews> iterator = es.findAll().iterator();
		while (iterator.hasNext()) {
			data.add(iterator.next());
		}
		return data;
	}

	@PostMapping("queryById")
	public ESNews queryById(long id) {
		/*
		 * es.count(); es.delete(null); es.exists(id); es.findAll(ids);
		 * es.search();
		 */

		// 采用过滤器的形式，提高查询效率

		/*BoolQueryBuilder builder = QueryBuilders.boolQuery();
		builder.must(QueryBuilders.termQuery("name", "")).must(QueryBuilders.termQuery("num", 12));
		*/
		return es.findOne(id);
	}
	
	int PAGE_SIZE = 3; //默认分页大小  
    
    int PAGE_NUMBER = 0; //默认当前分页  
      
    String SCORE_MODE_SUM = "sum"; //权重分求和模式  
      
    Float MIN_SCORE = 10.0F; //由于无相关性的分值默认为1， 设置权重分最小值为10 
    
    /** 
     * 在ES中搜索内容 
     */  
    @GetMapping("functionScoreQuery")
    public List<ESNews> searchEntity(int pageNumber, int pageSize, String searchContent){  
        if(pageSize==0) {  
            pageSize = PAGE_SIZE;  
        }  
        if(pageNumber<0) {  
            pageNumber = PAGE_NUMBER;  
        }  
          
        SearchQuery searchQuery = getEntitySearchQuery(pageNumber,pageSize,searchContent);  
        Page<ESNews> cityPage = es.search(searchQuery);  
        return cityPage.getContent();  
    }  
      
    /** 
     * 组装搜索Query对象 
     * @param pageNumber 
     * @param pageSize 
     * @param searchContent 
     * @return 
     */  
    private SearchQuery getEntitySearchQuery(int pageNumber, int pageSize, String searchContent) {  
        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery()  
                .add(QueryBuilders.matchPhraseQuery("name", searchContent),  
                        ScoreFunctionBuilders.weightFactorFunction(1000))  
                //.add(QueryBuilders.matchPhraseQuery("other", searchContent),  
                        //ScoreFunctionBuilders.weightFactorFunction(1000))  
                .scoreMode(SCORE_MODE_SUM).setMinScore(MIN_SCORE);  
        //设置分页，否则只能按照ES默认的分页给  
        Pageable pageable = new PageRequest(pageNumber, pageSize);  
        return new NativeSearchQueryBuilder().withPageable(pageable).withQuery(functionScoreQueryBuilder).build();  
    }
}
