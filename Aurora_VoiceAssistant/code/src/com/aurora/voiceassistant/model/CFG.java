package com.aurora.voiceassistant.model;

import android.os.Debug;


public class CFG 
{
	//response type
	public static final String RESULTTYPE_TEXT 		= "text";
	public static final String RESULTTYPE_JSON 		= "json";
	public static final String RESULTTYPE_WEBVIEW 	= "webview";
	public static final String RESULTTYPE_XML 		= "xml";
	public static final String RESULTTYPE_SOGOUMAP_URL	= "sogoumap_url";
	public static final String RESULTTYPE_TEXT_MUSIC	= "text_music";
	
	public static final String FINAL_RESULT 	= "final_result";
	public static final String REAL_POINT		= "real_point";
	public static final String SYS_TIME			= "sys_time";
	public static final String CITY 			= "city";
	
	public static final String RESULT_TYPE 		= "result_type"; 
	public static final String SEARCH_URL 		= "search_url";
	public static final String DESCRIPTION 		= "description";
	public static final String ANSWER 			= "answer";
	public static final String RESULT_NAME 		= "result_name";
	public static final String WEBVIEW_URL 		= "webview_url";
	public static final String SEARCH_CONTENT 	= "search_content";
	public static final String RESULT 			= "result";
	
	//json data reminder alarm
	public static final String CONTENT 			= "content";
	public static final String CMD 				= "cmd";
	public static final String DATE 			= "date";
	public static final String DATETYPE 		= "date_type";
	public static final String TIME 			= "time";
	public static final String CONTENTRESPSTAT  = "contentRespStat";
	
	
	
	public static final String TAB_TYPE_XML_TUPIAN 		= "图片";
	public static final String TAB_TYPE_XML_TIANQI	 	= "天气-天气VR";
	public static final String TAB_TYPE_XML_NB_SH 		= "搜索商户-搜索商户";
	public static final String TAB_TYPE_XML_NB_CS 		= "搜索商户-厕所推广";
	public static final String TAB_TYPE_XML_CX_MSG_LC 	= "出行信息-列车";
	public static final String TAB_TYPE_XML_CX_KC 		= "出行-客车";
	public static final String TAB_TYPE_XML_CX_LC 		= "出行-列车";
	//public static final String TAB_TYPE_XML_CX_MSG_HB 	= "出行信息-航班";
	public static final String TAB_TYPE_XML_CX_HB 		= "出行-航班";
	public static final String TAB_TYPE_XML_BAIKE 		= "百科-百科";
	
	
	//resultType = null, the type of data is json, depends on description
	public static final String DESCRIPTION_TYPE_REMIND_ALARM 		= "提醒-提醒/闹钟";
	
	//view type
	public static final int VIEW_TYPE_NOTHING = 0;
	
	public static final int VIEW_TYPE_QUIZ 			= VIEW_TYPE_NOTHING+1;
	public static final int VIEW_TYPE_RES 			= VIEW_TYPE_NOTHING+2;
	public static final int VIEW_TYPE_TEXT 			= VIEW_TYPE_NOTHING+3;
	public static final int VIEW_TYPE_WEBVIEW 		= VIEW_TYPE_NOTHING+4;
	public static final int VIEW_TYPE_JOSN 			= VIEW_TYPE_NOTHING+5;
	public static final int VIEW_TYPE_XML 			= VIEW_TYPE_NOTHING+6;
	public static final int VIEW_TYPE_XML_PIC 		= VIEW_TYPE_NOTHING+7;//xml图片
	public static final int VIEW_TYPE_XML_TIANQI 	= VIEW_TYPE_NOTHING+8 ;//xml天气
	public static final int VIEW_TYPE_XML_NB 		= VIEW_TYPE_NOTHING+9 ;//xml附近
	public static final int VIEW_TYPE_XML_CX_MSG_LC = VIEW_TYPE_NOTHING+10 ;//xml出行信息-列车
	public static final int VIEW_TYPE_XML_BAIKE 	= VIEW_TYPE_NOTHING+11 ;//xml百科-百科
	public static final int VIEW_TYPE_XML_CX_KC 	= VIEW_TYPE_NOTHING+12 ;//xml出行-客车
	public static final int VIEW_TYPE_XML_CX_LC 	= VIEW_TYPE_NOTHING+13 ;//xml出行-客车
	//public static final int VIEW_TYPE_XML_CX_MSG_HB = VIEW_TYPE_NOTHING+14 ;//xml出行信息-航班
	public static final int VIEW_TYPE_XML_CX_HB 	= VIEW_TYPE_NOTHING+15 ;//xml出行-航班
	
	public static final int VIEW_TYPE_SOGOUMAP_URL 	= VIEW_TYPE_NOTHING+16 ;//sogou map地图
	public static final int VIEW_TYPE_TEXT_MUSIC 	= VIEW_TYPE_NOTHING+17 ;//text music
	
	public static final int VIEW_TYPE_XML_MAX 		= 1000 ;
	
	/*
	//webview type
	public static final int VIEW_TYPE_WEBVIEW_WENDAI 	= VIEW_TYPE_XML_MAX+1 ;//问答
	public static final int VIEW_TYPE_WEBVIEW_TIEBA  	= VIEW_TYPE_XML_MAX+2 ;//贴吧
	public static final int VIEW_TYPE_WEBVIEW_SHIPIN 	= VIEW_TYPE_XML_MAX+3 ;//视频
	public static final int VIEW_TYPE_WEBVIEW_SOUSUO 	= VIEW_TYPE_XML_MAX+4 ;//搜索
	public static final int VIEW_TYPE_WEBVIEW_WENKU 	= VIEW_TYPE_XML_MAX+5 ;//文库
	public static final int VIEW_TYPE_WEBVIEW_ZHOUBIAN 	= VIEW_TYPE_XML_MAX+6 ;//周边
	
	public static final int VIEW_TYPE_WEBVIEW_MAX = 3000 ;
	*/
	public static final int VIEW_TYPE_MAX = 4000;
	
	public static final String XMLPIC_MORELINK = "http://wap.sogou.com/pic/searchList.jsp?keyword=";
	public static final boolean DEBUG = true;
	
	//public static final String SAVE_IMG_PATH = "data/data/vs/temp/";
	
	//Offline start
	/************************Offline KeyWords Definition************************/
	public static final String OFFLINE_KEYWORDS_PHONE = "电话";
	public static final String OFFLINE_KEYWORDS_PHONE_2 = "的电话";
	public static final String OFFLINE_KEYWORDS_NUMBER = "号码";
	public static final String OFFLINE_KEYWORDS_NUMBER_2 = "的号码";
	public static final String OFFLINE_KEYWORDS_MESSAGE = "短信";
	public static final String OFFLINE_KEYWORDS_MESSAGE2 = "信息";
	
	public static final String OFFLINE_KEYWORDS_DA = "打";
	public static final String OFFLINE_KEYWORDS_BO = "拨";
	public static final String OFFLINE_KEYWORDS_QU = "去";
	public static final String OFFLINE_KEYWORDS_FA = "发";
	public static final String OFFLINE_KEYWORDS_SONG = "送";
	public static final String OFFLINE_KEYWORDS_GE = "个";
	public static final String OFFLINE_KEYWORDS_SHUO = "说";
	public static final String OFFLINE_KEYWORDS_GEI= "给";
	public static final String OFFLINE_KEYWORDS_DE= "的";
	public static final String OFFLINE_KEYWORDS_DAKAI = "打开";
	//Offline end
	
	//account start
	/************************account Definition************************/
	public static final String ACCOUNT_MODULE_ID				= "260";	//voiceassistant module id --> 260
	public static final String ACCOUNT_ACTION_ENTER 			= "001";
	public static final String ACCOUNT_ACTION_HELP_SHOW			= "002";
	public static final String ACCOUNT_ACTION_HELP_EXAMPLE		= "003";
	public static final String ACCOUNT_ACTION_VOICE_INPUT		= "004";
	public static final String ACCOUNT_ACTION_SEND				= "005";
	public static final String ACCOUNT_ACTION_CLEAR_SCREEN		= "006";
	public static final String ACCOUNT_ACTION_SETTING			= "007";
	public static final String ACCOUNT_ACTION_VOICE_SWITCH		= "008";
	public static final String ACCOUNT_ACTION_BARCODE_CAMERA	= "009";
	public static final String ACCOUNT_ACTION_BARCODE_PHOTO		= "010";
	//account end
}
