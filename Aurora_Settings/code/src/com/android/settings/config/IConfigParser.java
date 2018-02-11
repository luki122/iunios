package com.android.settings.config;

import java.io.InputStream;
import java.util.Map;

public interface IConfigParser {
	 /** 
     * 解析输入流 得到Config对象集合 
     * @param is 
     * @return 
     * @throws Exception 
     */  
    public Map<String,Boolean> parse(InputStream is) throws Exception;  
      
    
}
