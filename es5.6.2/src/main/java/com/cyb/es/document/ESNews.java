package com.cyb.es.document;

import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonFormat;

@Document(indexName = ESNews.INDEX, type = ESNews.ORDER_TYPE, indexStoreType = "fs", shards = 5, replicas = 1, refreshInterval = "-1")
public class ESNews {
	public static final String INDEX = "es-study";
	public static final String ORDER_TYPE = "news-type";
	public static final String DETAIL_TYPE = "news-content-document";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Field(type = FieldType.text, analyzer = "ik_max_word"
			/*index = FieldIndex.not_analyzed, 
			searchAnalyzer = "ik_smart"*/)
	private String title;
	
	//analysis-ik
	@Field(type = FieldType.text, 
			searchAnalyzer = "ik_smart",
			analyzer = "ik_smart")
	private String content;

	/*
	 * // 订单备注，不需要分词，可以搜索
	 * 
	 * @Field(type = FieldType.String, index = FieldIndex.not_analyzed) private
	 * String note;
	 * 
	 * // 订单名称，可以通过ik 分词器进行分词
	 * 
	 * @Field(type = FieldType.String, searchAnalyzer = "ik", analyzer = "ik")
	 * private String name;
	 */

	
	  @Field( type = FieldType.Date, 
			  format = DateFormat.custom,pattern =
	  "yyyyMMddHHmmss" )//20180613083253
	  @JsonFormat (shape = JsonFormat.Shape.STRING, pattern =
	  "yyyyMMddHHmmss",timezone="GMT+8")
	  private Date time;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String toString() {
		return "id=" + id + "\n title=" + title + "\n content=" + content;
	}
}
