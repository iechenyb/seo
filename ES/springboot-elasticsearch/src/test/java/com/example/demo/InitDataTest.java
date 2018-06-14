package com.example.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import com.ElasticSearchApplication;
import com.cyb.es.bean.DBNews;
import com.cyb.es.dao.NewsSearchRepository;
import com.cyb.es.dao.NewsSearchRepositoryES;
import com.cyb.es.document.ESNews;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ElasticSearchApplication.class)
public class InitDataTest {
	@Autowired
	NewsSearchRepository re;

	@Autowired
	NewsSearchRepositoryES nsr;
    /**
     * 将db中的数据读出进行索引存储
     */
	@Test
	public void testInitNewsFromMysqlToEs() {
		//需要提前向库表中插入数据
		Iterator<DBNews> iterator = re.findAll().iterator();
		while (iterator.hasNext()) {
			DBNews n = iterator.next();
			ESNews news = new ESNews();
			news.setId(n.getId());
			news.setContent(n.getContent());
			news.setTitle(n.getTitle());
			nsr.save(news);
		}
	}
	public File getFile(String path) throws FileNotFoundException{
		return ResourceUtils.getFile("classpath:data/"+path);
	}
	/**
	 * 
	 *作者 : iechenyb<br>
	 *方法描述: 将数据从xml中持久化到数据中<br>
	 *创建时间: 2017年7月15日hj12
	 *@throws IOException
	 */
	@Test
	public void testInitNewsFromXmlToMysql() throws IOException {
		re.deleteAll();
		Document doc = Jsoup.parse(getFile("news.xml"),"utf-8");
		Elements news = doc.select("new");
		for(int i=0;i<news.size();i++){
			System.out.println(news.get(i).attr("id"));
			System.out.println(news.get(i).attr("title"));
			System.out.println(news.get(i).text());
			DBNews n = new DBNews();
			n.setId(Long.valueOf(news.get(i).attr("id")));
			n.setTitle(news.get(i).attr("title"));
			n.setContent(news.get(i).text());
			re.save(n);
		}
	}
}
	
