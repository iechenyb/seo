package com.cyb.es.config.swagger.es;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
/**
 *作者 : iechenyb<br>
 *类描述: 说点啥<br>
 *创建时间: 2018年6月14日
 */
/*@Configuration
@EnableElasticsearchRepositories(basePackages = "com.puregold.ms")*/
public class SearchConfig {
	Log log = LogFactory.getLog(SearchConfig.class);
	
	    // 假设使用三个node,(一主两备)的配置。在实际的生产环境，需在properties文件中替换成实际ip(内网或者外网ip)
	    //@Value("${elasticsearch.host1}")
	    @SuppressWarnings("unused")
		private String esHost;// master node

	    //@Value("${elasticsearch.host2:}") 
	    @SuppressWarnings("unused")
		private String esHost2;//replica node

	    //@Value("${elasticsearch.host3:}")
	    @SuppressWarnings("unused")
		private String esHost3;//replica node

	    //@Value("${elasticsearch.port}")
	    @SuppressWarnings("unused")
		private int esPort;

	    //@Value("${elasticsearch.clustername}")
	    private String esClusterName;

	    //@Bean
	    public TransportClient transportClient() throws Exception {
	    	 TransportClient transportClient =null;

	        /*Settings settings = Settings.settingsBuilder()
	                .put("cluster.name", esClusterName)
	                .build();

	        transportClient = TransportClient.builder()
	                .settings(settings)
	                .build()
	                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esHost), esPort));
	        if (StringUtils.isNotEmpty(esHost2)) {
	            transportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esHost2), esPort));
	        }
	        if (StringUtils.isNotEmpty(esHost3)) {
	            transportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esHost3), esPort));
	        }*/
	        return transportClient;
	    }

	    //@Bean
	    public ElasticsearchTemplate elasticsearchTemplate() throws Exception {
	        return new ElasticsearchTemplate(transportClient());
	    }
}
