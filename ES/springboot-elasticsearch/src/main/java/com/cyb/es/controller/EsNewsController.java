package com.cyb.es.controller;

import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.index.query.WrapperQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
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
import com.cyb.es.bean.NewsData;
import com.cyb.es.dao.NewsSearchRepositoryES;
import com.cyb.es.dao.NewsSearchRepositoryMysql;
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
public class EsNewsController {

	@Autowired
	NewsSearchRepositoryES esResp;
	
	@Autowired
	ElasticsearchTemplate esTemplate;

	@Autowired
	NewsSearchRepositoryMysql mysql;
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
		return esTemplate.createIndex(ESNews.class);
	}
	/**
	 * 判断索引是否存在
	 * @return
	 */
	@GetMapping("hasIndexByClass")
	public boolean hasIndexByClass(){
		return esTemplate.indexExists(ESNews.class);
	}
	/**
	 * 删除索引
	 * @return
	 */
	@GetMapping("delIndexByClass")
	public boolean delIndexByClass(){
		return esTemplate.deleteIndex(ESNews.class);
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
		//从es中查询已经有的索引信息
		Optional<ESNews> news = esResp.findById(id);
		data.put("oldTitle", news.get().getTitle());
		news.get().setTitle(title);
		esResp.save(news.get());//保存索引信息
		//返回新的索引数据 从es中查询
		Optional<ESNews> news_ = esResp.findById(id);
		data.put("newTitle", news_.get().getTitle());
		return data;
	}
	@PostConstruct
	@GetMapping("initNews")
	public List<ESNews> init() throws FileNotFoundException, IOException {
		if(!hasIndexByClass()){
			esTemplate.createIndex(ESNews.class);
		}
		esResp.deleteAll();
		mysql.deleteAll();
	
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
			mysql.save(n);
		}
		for (int i = 0; i < 40; i++) {
			DBNews n = new DBNews();
            n.setTitle(NewsData.getTitle().get(i));
            n.setContent(NewsData.getContent().get(i));
            mysql.save(n);
        }
		List<ESNews> data = new ArrayList<ESNews>();
		// 创建索引
		Iterator<DBNews> iterator = mysql.findAll().iterator();
		while (iterator.hasNext()) {
			DBNews n = iterator.next();
			ESNews news = new ESNews();
			news.setId(n.getId());
			news.setContent(n.getContent());
			news.setTitle(n.getTitle());
			news.setTime(new Date());
			esResp.save(news);
			data.add(news);
		}
		return data;
	}

	@GetMapping("queryall")
	public List<ESNews> queryAllNews() {
		Iterator<ESNews> iter = esResp.findAll().iterator();
		List<ESNews> data = new ArrayList<ESNews>();
		while (iter.hasNext()) {
			data.add(iter.next());
		}
		return data;
	}

	@GetMapping("deleteall")
	public List<ESNews> deleteAll() {
		esResp.deleteAll();
		Iterator<ESNews> iter = esResp.findAll().iterator();
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
		Iterable<ESNews> searchResult = esResp.search(builder);
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
		Iterable<ESNews> searchResult = esResp.search(builder);
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
		Iterable<ESNews> searchResult = esResp.search(builder);
		Iterator<ESNews> iterator = searchResult.iterator();
		while (iterator.hasNext()) {
			data.add(iterator.next());
		}
		return data;
	}

	@PostMapping("addNews")
	public List<ESNews> addNews(@RequestBody ESNews news) {
		news.setTime(new Date());
		esResp.save(news);
		List<ESNews> data = new ArrayList<ESNews>();
		Iterator<ESNews> iterator = esResp.findAll().iterator();
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
		return esResp.findById(id).get();
	}
	
	int PAGE_SIZE = 3; //默认分页大小  
    
    int PAGE_NUMBER = 0; //默认当前分页  
      
    String SCORE_MODE_SUM = "sum"; //权重分求和模式  
      
    Float MIN_SCORE = 10.0F; //由于无相关性的分值默认为1， 设置权重分最小值为10 
    
  /*  *//** 
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
        Page<ESNews> cityPage = esResp.search(searchQuery);  
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
       /* FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery()  
                .add(QueryBuilders.matchPhraseQuery("name", searchContent),  
                        ScoreFunctionBuilders.weightFactorFunction(1000))  
                //.add(QueryBuilders.matchPhraseQuery("other", searchContent),  
                        //ScoreFunctionBuilders.weightFactorFunction(1000))  
                .scoreMode(SCORE_MODE_SUM).setMinScore(MIN_SCORE);  
        //设置分页，否则只能按照ES默认的分页给  
        Pageable pageable = new Pageable(pageNumber, pageSize);  
        return new NativeSearchQueryBuilder().withPageable(pageable)
        		.withQuery(functionScoreQueryBuilder)
        		.build(); */ 
    	return null;
    }
    @GetMapping("highLight")
    public AggregatedPage<ESNews> test(String key){
   	 Pageable pageable = PageRequest.of(0, 10);

        String preTag = "<font color='#dd4b39'>";//google的色值
        String postTag = "</font>";

        SearchQuery searchQuery = new NativeSearchQueryBuilder().
                withQuery(matchPhraseQuery("title", key)).
                withQuery(matchPhraseQuery("content", key)).
                withHighlightFields(
               		 new HighlightBuilder.Field("title").preTags(preTag).postTags(postTag),
                        new HighlightBuilder.Field("content").preTags(preTag).postTags(postTag)).build();
       searchQuery.setPageable(pageable);
        
        // 不需要高亮直接return ideas 
        // AggregatedPage<Idea> ideas = elasticsearchTemplate.queryForPage(searchQuery, Idea.class);
        
        // 高亮字段
        AggregatedPage<ESNews> ideas = esTemplate.queryForPage(searchQuery, ESNews.class, new SearchResultMapper() {

            @SuppressWarnings("unchecked")
			@Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                List<ESNews> chunk = new ArrayList<>();
                for (SearchHit searchHit : response.getHits()) {
                    if (response.getHits().getHits().length <= 0) {
                   	 System.out.println("没有查询到数据！");
                   	 return new AggregatedPageImpl<>((List<T>) chunk);
                    }
                    ESNews idea = new ESNews();
                    //name or memoe
                    HighlightField ideaTitle = searchHit.getHighlightFields().get("title");
                    if (ideaTitle != null) {
                        idea.setTitle(ideaTitle.fragments()[0].toString());
                    }
                    HighlightField ideaContent = searchHit.getHighlightFields().get("content");
                    if (ideaContent != null) {
                        idea.setContent(ideaContent.fragments()[0].toString());
                    }
                    chunk.add(idea);
                }
                return new AggregatedPageImpl<>((List<T>) chunk);
            }
        });
        return ideas;
   }
}
