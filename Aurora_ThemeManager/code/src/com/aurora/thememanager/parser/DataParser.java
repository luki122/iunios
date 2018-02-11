package com.aurora.thememanager.parser;

import java.util.List;

public interface DataParser {
	

	/**
	 * parse data from internet or others
	 * @param source  need to parse source file
	 * @return   
	 */
		List<Object> parser(Object source) throws ParserException;
	
		
}
