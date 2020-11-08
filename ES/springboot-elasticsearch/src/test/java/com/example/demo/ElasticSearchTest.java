package com.example.demo;

import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ElasticSearchApplication;
import com.cyb.es.dao.NewsSearchRepositoryES;
import com.cyb.es.dao.NewsSearchRepositoryMysql;
import com.cyb.es.document.ESNews;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ElasticSearchApplication.class)
public class ElasticSearchTest {
	 Logger log = LoggerFactory.getLogger(ElasticSearchTest.class);
	@Autowired
	NewsSearchRepositoryMysql re;

	@Autowired
	NewsSearchRepositoryES nsr;

	String word = "浣溪沙";// 搜索关键字
	@Autowired
	ElasticsearchTemplate esTemplate;

	/**
	 * 根据索引查询内容
	 */
	@Test
	public void testStringSearchNews() {
		log.info("xxxxxxxxxxxxxxxxxxxxxxx");
		QueryStringQueryBuilder builder = new QueryStringQueryBuilder(word);
		Iterable<com.cyb.es.document.ESNews> searchResult = nsr.search(builder);
		Iterator<com.cyb.es.document.ESNews> iterator = searchResult.iterator();
		while (iterator.hasNext()) {
			log.info(iterator.next().getContent());
		}
		log.info("xxxxxxxxxxxxxxxxxxxxxxx");
		while(true){
			
		}
	}
    @Test
	public void highlightBuilder() {
		log.info("--------------------------------2--------------------------------");
		SearchQuery searchQuery = new NativeSearchQueryBuilder()
				.withQuery(matchPhraseQuery("title", word))
				.withQuery(matchPhraseQuery("content", word))
				.withHighlightFields(new HighlightBuilder.Field("title"))
				.build();
		List<ESNews> news = esTemplate.queryForList(searchQuery, ESNews.class);
		showNews(news);
		log.info("------------------------------3----------------------------------");
		showNews(test().getContent());
		while(true){
			
		}
	}
    public AggregatedPage<ESNews> test(){
    	 Pageable pageable = PageRequest.of(0, 10);

         String preTag = "<font color='#dd4b39'>";//google的色值
         String postTag = "</font>";

         SearchQuery searchQuery = new NativeSearchQueryBuilder().
                 withQuery(matchPhraseQuery("title", word)).
                 withQuery(matchPhraseQuery("content", word)).
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
                    	 log.info("没有查询到数据！");
                         return null;
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
    
	private void showNews(List<ESNews> news) {
		for (ESNews n : news) {
			log.info(n.getTitle()+"-------->" + n.getContent());
		}
	}
}
