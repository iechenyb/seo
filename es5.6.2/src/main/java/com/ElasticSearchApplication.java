package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;  
@SpringBootApplication
public class ElasticSearchApplication {
	
	/*@Bean(name="ik")
	public Analyzer ikBean(){
		return new IKAnalyzer();
	}
	*/
	public static void main(String[] args) {
		SpringApplication.run(ElasticSearchApplication.class, args);
	}
}
