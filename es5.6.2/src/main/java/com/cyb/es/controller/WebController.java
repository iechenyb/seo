package com.cyb.es.controller;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.springframework.web.bind.annotation.RestController;

import com.cyb.es.dao.WebSearchRepositoryES;
import com.cyb.es.document.WebDocument;
/**
 *作者 : iechenyb<br>
 *类描述: 说点啥<br>
 *创建时间: 2018年6月15日
 */
@RestController
@RequestMapping("/web")
public class WebController {
	Log log = LogFactory.getLog(WebController.class);
	@Autowired
	WebSearchRepositoryES es;
	Map<String,String> urlMap =null;
	long id=1;
	long count =0;
	long maxDeep = 1000;
	List<WebDocument> list = null;
	/**
	 * 
	 *作者 : iechenyb<br>
	 *方法描述: 子查询一级<br>
	 *创建时间: 2017年7月15日hj12
	 *@param url
	 *@return
	 *@throws MalformedURLException
	 *@throws IOException
	 */
	@GetMapping("getWebsFromUrl")
	public List<WebDocument> getWebsFromUrl(String url) throws MalformedURLException, IOException {
		if(StringUtils.isEmpty(url)){
			url="http://www.88-888.com/";
		}
		urlMap = new HashMap<>();
		initWeb(url);
		return list;
	}
	
	
	private void initWeb(String url) {
		long id = 0;
		Document doc;
		try {
			doc = Jsoup.parse(new URL(url), 5000);
			Elements eles = doc.getElementsByTag("a");
			WebDocument web = null;
			for(int i=0;i<eles.size();i++){
				web = new WebDocument();
				web.setId(id++);
				web.setTime(new Date());
				web.setUrl(eles.get(i).absUrl("href"));
				web.setTitle(eles.get(i).text()==""?eles.get(i).attr("title"):eles.get(i).text());
				web.setMeta(keyWords(web.getUrl()));
				if(!urlMap.containsKey(web.getUrl())){
					es.save(web);
					urlMap.put(web.getUrl(), "");
					System.out.println(web);
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}


	public void  lookupWeb(String url){
		try {
			if(count++>maxDeep){
				return ;
			}
			urlMap = new HashMap<String, String>();
			Document doc = Jsoup.parse(new URL(url), 5000);
			Elements eles = doc.getElementsByTag("a");
			WebDocument web = new WebDocument();
			for(int i=0;i<eles.size();i++){
				web.setId(id++);
				web.setTime(new Date());
				web.setUrl(eles.get(i).attr("href"));
				web.setTitle(eles.get(i).text());
				web.setMeta(keyWords(web.getUrl()));
				if(!StringUtils.isEmpty(web.getUrl())){
					if(!web.getUrl().startsWith("http://")||web.getUrl().startsWith("https://")){
						continue;
					}
				}
				if(StringUtils.isEmpty(web.getTitle())&&StringUtils.isEmpty(web.getMeta())){
					continue;//没有意义的连接跳过
				}else{
					if(urlMap.containsKey(web.getUrl())){
						continue;//相同的网站跳过
					}
					list.add(web);
					urlMap.put("url", web.getUrl());
					lookupWeb(web.getUrl());
					System.out.println("遍历深度："+eles.get(i).text()+","+eles.get(i).attr("href")+","+eles.get(i).attr("title"));
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String keyWords(String url){
		Document doc;
		String keyWords = "";
		try {
			doc = Jsoup.parse(new URL(url), 5000);
			//后去meta标签  name=keywords
			Elements eles = doc.getElementsByAttributeValue("name", "keywords");
			if(eles!=null&&eles.size()>0){
				keyWords = eles.get(0).attr("content");
			}
		} catch (IOException e) {
			return "";
		}
		return keyWords;
		
	}
	
	public static void main(String[] args) throws MalformedURLException, IOException {
		String url="http://www.88-888.com/";
		System.out.println(keyWords(url));
		long id = 0;
		Document doc = Jsoup.parse(new URL(url), 5000);
		Elements eles = doc.getElementsByTag("a");
		WebDocument web = null;
		for(int i=0;i<eles.size();i++){
			web = new WebDocument();
			web.setId(id++);
			web.setTime(new Date());
			web.setUrl(eles.get(i).absUrl("href"));
			web.setTitle(eles.get(i).text()==""?eles.get(i).attr("title"):eles.get(i).text());
			web.setMeta(keyWords(web.getUrl()));
			System.out.println(web);
		}
	}
	
	
	
}
