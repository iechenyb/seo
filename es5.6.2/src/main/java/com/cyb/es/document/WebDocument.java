package com.cyb.es.document;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonFormat;
/**
 *作者 : iechenyb<br>
 *类描述: 说点啥<br>
 *创建时间: 2018年6月15日
 */
@Document(indexName = PublicIndex.INDEX, type = PublicIndex.WEB_TYPE, indexStoreType = "fs", shards = 5, replicas = 1, refreshInterval = "-1")
public class WebDocument {
	@Id
	private Long id;
	
	@Field(type = FieldType.text, analyzer = "ik_smart",
			/*index = FieldIndex.not_analyzed, */
			searchAnalyzer = "ik_smart")
	private String title;
	
	@Field(type = FieldType.text, analyzer = "ik_smart",
			/*index = FieldIndex.not_analyzed, */
			searchAnalyzer = "ik_smart")
	private String url;
	
	@Field(type = FieldType.text, analyzer = "ik_smart",
			/*index = FieldIndex.not_analyzed, */
			searchAnalyzer = "ik_smart")
	private String meta;
	
	@Field( type = FieldType.text, 
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
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getMeta() {
		return meta;
	}
	public void setMeta(String meta) {
		this.meta = meta;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String toString(){
		return this.id+"/"+this.title+"/"+this.url+"/"+this.meta+"/"+this.time;
	}
	
}
