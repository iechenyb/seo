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
import java.util.Optional;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.index.query.WrapperQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
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
import org.springframework.web.bind.annotation.RequestParam;
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
import io.swagger.annotations.ApiParam;

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
	 * 作者 : iechenyb<br>
	 * 方法描述: 更新索引值<br>
	 * 创建时间: 2017年7月15日hj12
	 * 
	 * @param id
	 * @param title
	 * @return
	 */
	@GetMapping("addIndexByClass")
	public boolean addIndexByClass() {
		return template.createIndex(ESNews.class);
	}

	@GetMapping("delIndexByClass")
	public boolean delIndexByClass() {
		return template.deleteIndex(ESNews.class);
	}

	public void delete() {
		/*
		 * db.delete(id); db.delete(entity); db.delete(collection);
		 * db.deleteAllInBatch(); db.deleteInBatch(entities);
		 */

	}

	public File getFile(String path) throws FileNotFoundException {
		return ResourceUtils.getFile("classpath:data/" + path);
	}

	/**
	 * 
	 * 作者 : iechenyb<br>
	 * 方法描述: 更新索引值<br>
	 * 创建时间: 2017年7月15日hj12
	 * 
	 * @param id
	 * @param title
	 * @return
	 */
	@GetMapping("update")
	public Map<String, String> update(Long id, String title) {
		Map<String, String> data = new HashMap<>();
		Optional<ESNews> news = es.findById(id);
		data.put("oldTitle", news.get().getTitle());
		news.get().setTitle(title);
		es.save(news.get());
		Optional<ESNews> news_ = es.findById(id);
		data.put("newTitle", news_.get().getTitle());
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
	public List<ESNews> queryNews(String key) {
		List<ESNews> data = new ArrayList<ESNews>();
		String queryString = key;// 搜索关键字
		QueryStringQueryBuilder builder = new QueryStringQueryBuilder(queryString);
		// builder.analyzer("ik_smart");
		/* QueryBuilder queryBuilder = QueryBuilders.matchAllQuery(); */
		builder.field("title").field("content");
		//builder.analyzer("ik_smart");
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
		/*
		 * WildcardQueryParser parser = new WildcardQueryParser();
		 * parser.parse(parseContext)
		 */
		WildcardQueryBuilder builder = new WildcardQueryBuilder("title", queryString);
		// QueryBuilders.wildcardQuery("title","*"+queryString+"*");
		Iterable<ESNews> searchResult = es.search(builder);
		Iterator<ESNews> iterator = searchResult.iterator();
		while (iterator.hasNext()) {
			data.add(iterator.next());
		}
		return data;
	}

	// 模糊匹配
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

		/*
		 * BoolQueryBuilder builder = QueryBuilders.boolQuery();
		 * builder.must(QueryBuilders.termQuery("name",
		 * "")).must(QueryBuilders.termQuery("num", 12));
		 */
		return es.findById(id).get();
	}

	int PAGE_SIZE = 3; // 默认分页大小

	int PAGE_NUMBER = 0; // 默认当前分页

	String SCORE_MODE_SUM = "sum"; // 权重分求和模式

	Float MIN_SCORE = 10.0F; // 由于无相关性的分值默认为1， 设置权重分最小值为10
	String preTag = "<font color=‘#dd4b39‘>";//google的色值
    String postTag = "</font>";
	/**
	 * 根据地址值过滤
	 * 
	 * @return
	 */
	@GetMapping("queryByNewsField")
	@ApiOperation(value = "根据指定的field查询", notes = "根据指定的field查询")
	public List<ESNews> queryByNewsField(
			@ApiParam("新闻的属性，如title、content") @RequestParam("key") String key,
			@ApiParam("对应的搜索内容") @RequestParam("value") String value) {
	
		 HighlightBuilder highlightBuilder = new HighlightBuilder().field(key);  
	     highlightBuilder.preTags("<em>");  
	     highlightBuilder.postTags("</em>");
		
		// 根据地址值过滤
		@SuppressWarnings("deprecation")
		Pageable page = new PageRequest(0, 10);
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		queryBuilder.must(QueryBuilders.matchQuery(key, value));
		
		SearchQuery query = new NativeSearchQueryBuilder()
				.withQuery(queryBuilder)
				.withPageable(page)
			    //.withFilter(QueryBuilders.termQuery("status", "0"))
                //.withSort(SortBuilders.fieldSort("modifiedTime").order(SortOrder.DESC))
				.withHighlightFields(new HighlightBuilder.Field(key).preTags(preTag).postTags(postTag)
                        /*, new HighlightBuilder.Field("memo").preTags(preTag).postTags(postTag)*/)
				.build();
		Page<ESNews> pages = es.search(query);
		return pages.getContent();
	}

	@GetMapping("queryByNewsMutilField")
	@ApiOperation(value = "title和content字段同时查询", notes = "title和content字段同时查询")
	public List<ESNews> queryByNewsMutilField(String value) {
		// 根据地址值过滤
		@SuppressWarnings("deprecation")
		Pageable page = new PageRequest(0, 10);
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		queryBuilder.must(QueryBuilders.multiMatchQuery(value, "title", "content"));
		SearchQuery query = new NativeSearchQueryBuilder()
				.withQuery(queryBuilder)
				.withHighlightFields(new HighlightBuilder.Field("title").preTags(preTag).postTags(postTag)
                , new HighlightBuilder.Field("content").preTags(preTag).postTags(postTag))
				.withPageable(page).build();
		Page<ESNews> pages = es.search(query);
		return pages.getContent();
	}

	String title_field = "title";
	String content_field = "content";
	@GetMapping("queryByNewsFieldHighLight")
	@ApiOperation(value = "根据指定的field查询", notes = "根据指定的field查询")
	public List<ESNews> queryByNewsFieldHighLight(String value){
		/*BoolQueryBuilder queryBuilder =  QueryBuilders.boolQuery();
		 *queryBuilder.must(QueryBuilders.multiMatchQuery(value,title_field,content_field));
		 * */
		QueryStringQueryBuilder queryBuilder  = new QueryStringQueryBuilder(value);
		//QueryBuilders.queryStringQuery(value);
		queryBuilder.field(title_field)/*.field(content_field)*/;
		SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withHighlightFields(
                		new HighlightBuilder.Field(title_field)
                		.preTags(preTag).postTags(postTag)
                		/*,new HighlightBuilder.Field(content_field)
                        .preTags(preTag).postTags(postTag)*/)
                .build();
		
        return template.queryForPage(searchQuery, ESNews.class, new SearchResultMapper() {
            @SuppressWarnings("unchecked")
			public  <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                List<ESNews> chunk = new ArrayList<>();
                for (SearchHit searchHit : response.getHits()) {
                    if (response.getHits().getHits().length <= 0) {
                        return null;
                    }
                    ESNews user = new ESNews();
                    user.setId(Long.valueOf(searchHit.getId()));
                    user.setTime(new Date());//searchHit.getSource().get("time")
                    //name or memoe
                    HighlightField title = searchHit.getHighlightFields().get(title_field);
                    if (title != null) {
                        user.setTitle(title.fragments()[0].toString());
                    }else{
                    	user.setTitle(searchHit.getSource().get(title_field).toString());
                    }
                    HighlightField content = searchHit.getHighlightFields().get(content_field);
                    if (content != null) {
                        user.setContent(content.fragments()[0].toString());
                    }else{
                    	user.setContent(searchHit.getSource().get(content_field).toString());
                    }
                    System.out.println("======"+user);
                    chunk.add(user);
                }
                if (chunk.size() > 0) {
                	 return new AggregatedPageImpl<T>((List<T>) chunk);  
                }
                return null;
            }

			/*@Override
			public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
				return null;
			}*/
        }).getContent();
	}
	/*
	 * { BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
	 * queryBuilder.must(QueryBuilders.commonTermsQuery("title", ""));
	 * queryBuilder.must(QueryBuilders.matchPhrasePrefixQuery("title", ""));
	 * queryBuilder.must(QueryBuilders.matchPhraseQuery("title", ""));
	 * QueryBuilders.disMaxQuery(); QueryBuilders.fuzzyQuery("", ""); }
	 */
	/**
	 * 在ES中搜索内容
	 */
	@GetMapping("functionScoreQuery")
	public List<ESNews> searchEntity(int pageNumber, int pageSize, String searchContent) {
		if (pageSize == 0) {
			pageSize = PAGE_SIZE;
		}
		if (pageNumber < 0) {
			pageNumber = PAGE_NUMBER;
		}

		SearchQuery searchQuery = getEntitySearchQuery(pageNumber, pageSize, searchContent);

		Page<ESNews> cityPage = es.search(searchQuery);
		return cityPage.getContent();
	}

	/**
	 * 组装搜索Query对象
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param searchContent
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private SearchQuery getEntitySearchQuery(int pageNumber, int pageSize, String searchContent) {

		FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders
				.functionScoreQuery(ScoreFunctionBuilders.weightFactorFunction(100F));
		/*
		 * .add(QueryBuilders.matchPhraseQuery("name", searchContent),
		 * ScoreFunctionBuilders.weightFactorFunction(1000))
		 */
		// .add(QueryBuilders.matchPhraseQuery("other", searchContent),
		// ScoreFunctionBuilders.weightFactorFunction(1000))
		/* .scoreMode(SCORE_MODE_SUM).setMinScore(MIN_SCORE) */;
		// 设置分页，否则只能按照ES默认的分页给
		Pageable pageable = new PageRequest(pageNumber, pageSize);
		return new NativeSearchQueryBuilder().withPageable(pageable).withQuery(functionScoreQueryBuilder).build();
	}
}
