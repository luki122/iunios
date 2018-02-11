package com.aurora.ota.config;

import java.io.InputStream;
import java.util.List;

public interface ConfigParser {
	 /** 
     * 解析输入流 得到Config对象集合 
     * @param is 
     * @return 
     * @throws Exception 
     */  
    public List<Config> parse(InputStream is) throws Exception;  
      
    
}
