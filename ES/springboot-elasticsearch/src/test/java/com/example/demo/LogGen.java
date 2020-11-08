package com.example.demo;

import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyb.utils.date.DateUnsafeUtil;
import com.cyb.utils.file.FileUtils;
import com.cyb.utils.random.RandomUtils;

/**
 * 原理：将日志写入文件，然后开启logstash监听文件，并将读取传输到es服务器！
 * @author Administrator
 *
 */
public class LogGen {
  Logger log = LoggerFactory.getLogger(LogGen.class);
  @Test
  public void test() {
      log.trace("trace 成功了");
      log.debug("debug 成功了");
      log.info("info 成功了");
      log.warn("warn 成功了");
      log.error("error 成功了");
  }
  @Test
  public void logGen() throws IOException, InterruptedException{
	  for(int i=0;i<100;i++){
		  String message = DateUnsafeUtil.descTimeToSec()+"->"+RandomUtils.getChineaseName()+"->"+RandomUtils.getEmail(10, 20)+"\n";
		  FileUtils.append("d:/data/logs/log.log", message);
		  System.out.println(message);
		  Thread.sleep(100);
	  }
  }
}
