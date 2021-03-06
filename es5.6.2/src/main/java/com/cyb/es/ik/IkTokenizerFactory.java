//package com.cyb.es.ik;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
///**
// *作者 : iechenyb<br>
// *类描述: 说点啥<br>
// *创建时间: 2018年6月12日
// */
//import org.apache.lucene.analysis.Tokenizer;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.env.Environment;
//import org.elasticsearch.index.IndexSettings;
//import org.elasticsearch.index.analysis.AbstractTokenizerFactory;
//import org.wltea.analyzer.cfg.Configuration;
//import org.wltea.analyzer.lucene.IKTokenizer;
//
//public class IkTokenizerFactory extends AbstractTokenizerFactory {
//  private Configuration configuration;
//
//  public IkTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
//      super(indexSettings, name, settings);
//	  configuration=new Configuration(env,settings);
//  }
//
//  public static IkTokenizerFactory getIkTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
//      return new IkTokenizerFactory(indexSettings,env, name, settings).setSmart(false);
//  }
//
//  public static IkTokenizerFactory getIkSmartTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
//      return new IkTokenizerFactory(indexSettings,env, name, settings).setSmart(true);
//  }
//
//  public IkTokenizerFactory setSmart(boolean smart){
//        this.configuration.setUseSmart(smart);
//        return this;
//  }
//
//  @Override
//  public Tokenizer create() {
//      return new IKTokenizer(configuration);  }
//}