package com.example.demo;

import java.util.Date;
import java.util.Iterator;

import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ElasticSearchApplication;
import com.cyb.es.bean.News;
import com.cyb.es.dao.ArticleSearchRepository;
import com.cyb.es.dao.NewsSearchRepository;
import com.cyb.es.dao.NewsSearchRepositoryES;
import com.cyb.es.document.Article;
import com.cyb.es.document.Author;
import com.cyb.es.document.Tutorial;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ElasticSearchApplication.class)
public class ElasticSearchTest {

	@Autowired
	private ArticleSearchRepository articleSearchRepository;

	@Test
	public void testSaveArticleIndex() {
		Author author = new Author();
		author.setId(1L);
		author.setName("tianshouzhi");
		author.setRemark("java developer");

		Tutorial tutorial = new Tutorial();
		tutorial.setId(1L);
		tutorial.setName("elastic search");

		Article article = new Article();
		article.setId(1L);
		article.setTitle("springboot integreate elasticsearch");
		article.setAbstracts("springboot integreate elasticsearch is very easy");
		article.setTutorial(tutorial);
		article.setAuthor(author);
		article.setContent("elasticsearch based on lucene," + "spring-data-elastichsearch based on elaticsearch"
				+ ",this tutorial tell you how to integrete springboot with spring-data-elasticsearch");
		article.setPostTime(new Date());
		article.setClickCount(1L);
		articleSearchRepository.save(article);
	}

	@Test
	public void testSearch() {
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxx");
		String queryString = "springboot";// 搜索关键字
		QueryStringQueryBuilder builder = new QueryStringQueryBuilder(queryString);
		Iterable<Article> searchResult = articleSearchRepository.search(builder);
		Iterator<Article> iterator = searchResult.iterator();
		while (iterator.hasNext()) {
			System.out.println(iterator.next());
		}
	}

	@Autowired
	NewsSearchRepository re;

	@Autowired
	NewsSearchRepositoryES nsr;
    /**
     * 将db中的数据读出进行索引存储
     */
	@Test
	public void testInitNewsFromMysql() {
		Iterator<News> iterator = re.findAll().iterator();
		while (iterator.hasNext()) {
			com.cyb.es.document.News news = new com.cyb.es.document.News();
			News n = iterator.next();
			news.setId(n.getId());
			news.setContent(n.getContent());
			news.setTitle(n.getTitle());
			nsr.save(news);
		}
		testSearchNews();
	}
	/**
	 * 根据索引查询内容
	 */
	@Test
	public void testSearchNews() {
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxx");
		String queryString = "2";// 搜索关键字
		QueryStringQueryBuilder builder = new QueryStringQueryBuilder(queryString);
		Iterable<com.cyb.es.document.News> searchResult = nsr.search(builder);
		Iterator<com.cyb.es.document.News> iterator = searchResult.iterator();
		while (iterator.hasNext()) {
			System.out.println(iterator.next());
		}
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxx");
	}

}
