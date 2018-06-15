package com.cyb.es.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
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
/**
 * 
 * @author DHUser
 * 百度新闻抓取
 */
@RestController
@RequestMapping("baidu")
public class BaiduController {
	Log log = LogFactory.getLog(BaiduController.class);

	@GetMapping("findNewsByType")
	public List<BaiDuNews> getNewsFromBaidu(String type) throws MalformedURLException, IOException {
		Document doc = Jsoup.parse(new URL("https://news.baidu.com/" + type), 5000);
		Elements eles = doc.getElementsByTag("a");
		BaiDuNews news = null;
		List<BaiDuNews> list = new ArrayList<>();
		for (int i = 0; i < eles.size(); i++) {
			news = new BaiDuNews();
			news.setTitle(eles.get(i).text());
			news.setUrl(eles.get(i).attr("href"));
			if (!StringUtils.isEmpty(news.getUrl()) && !StringUtils.isEmpty(news.getTitle())) {
				if (news.getUrl().startsWith("http:")) {
					if (news.getTitle().length() > 10) {// 至少10个汉字
						news.setContent(getContentFromUrl(news.getUrl()));
						list.add(news);

					}
				}
			}
		}
		return list;
	}

	public String getContentFromUrl(String url) throws MalformedURLException {
		StringBuffer sb = new StringBuffer();
		try {
			Document doc = Jsoup.parse(new URL(url), 5000);
			Elements eles = doc.getElementsByTag("p");
			int max = 10;
			if (eles.size() < 10) {
				max = eles.size();
			}
			for (int i = 0; i < max; i++) {
				if (!StringUtils.isEmpty(eles.get(i).text()) && eles.get(i).text().length() > 10) {
					sb.append(eles.get(i).text());
				}
			}
		} catch (Exception e) {
			System.out.println(url + "内容获取失败！");
		}
		return sb.toString();
	}

	public List<String> initTypes() {
		List<String> types = new ArrayList<>();
		types.add("guonei");
		types.add("guoji");
		types.add("mil");
		types.add("finance");
		types.add("ent");
		types.add("sports");
		types.add("internet");
		types.add("tech");
		types.add("game");
		types.add("lady");
		types.add("auto");
		types.add("house");
		return types;
	}

	@GetMapping("getAllNews")
	public String diGuiGetNews() throws MalformedURLException, IOException {
		List<String> types = initTypes();
		es.deleteAll();//清除索引
		urlMap.clear();//清除记录
		total = 0;
		a=0;
		for (String type : types) {
			mkIndex(type,0);
		}
		return "百度新闻抓取完成！总条数" + total;
	}

	
	@GetMapping("mkIndexFromMysql")
	public String mkIndexFromMysql() throws MalformedURLException, IOException {
		es.deleteAll();
		db.deleteAll();
		List<DBNews> data = db.findAll();
		for (DBNews type : data) {
			ESNews n = new ESNews();
			n.setContent(type.getContent());
			n.setId(type.getId());
			n.setTime(new Date());
			n.setTitle(type.getTitle());
			es.save(n);
		}
		return "百度新闻抓取完成！总条数" + data.size();
	}
	int total = 0;

	@Autowired
	NewsSearchRepositoryES es;
	
	@Autowired
	NewsSearchRepository db;

	// 从百度新闻首页栏目进入
	private void mkIndex(String type,int deep) throws MalformedURLException, IOException {
		Document doc = Jsoup.parse(new URL("https://news.baidu.com/" + type), 5000);
		Elements eles = doc.getElementsByTag("a");
		BaiDuNews news = null;
		for (int i = 0; i < eles.size(); i++) {
			news = new BaiDuNews();
			news.setTitle(eles.get(i).text());
			news.setUrl(eles.get(i).attr("href"));
			if (!StringUtils.isEmpty(news.getUrl()) && !StringUtils.isEmpty(news.getTitle())) {
				if (news.getUrl().startsWith("http:")) {
					if (news.getTitle().length() > 10) {// 至少10个汉字
						news.setContent(getContentFromUrl(news.getUrl()));
						System.out.println("主页新闻"+type+"  " + news);
						// 创建索引
						if (!StringUtils.isEmpty(news.getContent())) {
							if (!urlMap.containsKey(news.getUrl())) {// 不再查询子页面中的当前url
								saveNewsToEs(news);
								getNewsByUrl(news.getUrl(),deep++);// 继续深入查询
							}
						}
						total++;
					}
				}
			}
		}
	}

	long id = 100l;

	private void saveNewsToEs(BaiDuNews news) {
		ESNews a = new ESNews();
		a.setId(id++);
		a.setTime(new Date());
		a.setTitle(news.getTitle());
		a.setContent(news.getContent());
		es.save(a);
		DBNews dbNews = new DBNews();
		dbNews.setContent(a.getContent());
		dbNews.setTitle(a.getTitle());
		//dbNews.setId(id);
		db.save(dbNews);
		urlMap.put(news.getUrl(), "".intern());
	}

	int a = 0;
	int maxDeep=100;
	Map<String, String> urlMap = new HashMap<>();

	public void getNewsByUrl(String url,int deep) {
		if (deep++> maxDeep) {
			return;
		} // 深度为10
		try {
			Document doc = Jsoup.parse(new URL(url), 5000);
			Elements eles = doc.getElementsByTag("a");
			BaiDuNews news = null;
			for (int i = 0; i < eles.size(); i++) {
				news = new BaiDuNews();
				news.setTitle(eles.get(i).text());
				news.setUrl(eles.get(i).attr("href"));
				if (!StringUtils.isEmpty(news.getUrl()) && !StringUtils.isEmpty(news.getTitle())) {
					if (news.getUrl().startsWith("http:")) {
						if (news.getTitle().length() > 10) {// 至少10个汉字
							news.setContent(getContentFromUrl(news.getUrl()));
							System.out.println("深度 " + (a++) + "----" + news);
							// 创建索引
							if (!StringUtils.isEmpty(news.getContent())) {
								if (!urlMap.containsKey(url)) {// 不再查询子页面中的当前url
									saveNewsToEs(news);//不存储相同的新闻，记录存储记录
									getNewsByUrl(news.getUrl(),deep);// 继续深入查询
								}
							}
							
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println(url + "递归抓取内容中断！");
		}
	}
}

class BaiDuNews {
	String title;
	String url;
	String content;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content.replace(" ", "");
	}

	public String toString() {
		return title + "," + url;
	}

}
