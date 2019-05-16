package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;  
//https://github.com/medcl/elasticsearch-analysis-ik/releases?after=v6.1.1 ik分词
//https://blog.csdn.net/u011499747/article/details/78917718
/**
 * 程序创建的索引没有对应的分词侧率。目前解决方案，先删除索引，启动程序自动创建即可！
 * @author Administrator
 *
 */
@SpringBootApplication
public class ElasticSearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(ElasticSearchApplication.class, args);
	}
}
