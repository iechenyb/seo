package com.cyb.es.bean;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;


//@Data
///@Log //java.utl.log 二选一
@Slf4j //日志对象
@AllArgsConstructor //所有参数构造方法
@NoArgsConstructor(staticName="Abcd") //无参构造方法
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class LombokTest {
   private String name;
   private String[] age;
   private String birthday;
   @SneakyThrows(Exception.class)
   public void logTest(@NonNull String aaa){
	   log.info("slf4j test");
   }
}
