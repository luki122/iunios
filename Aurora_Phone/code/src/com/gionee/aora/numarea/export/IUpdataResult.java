package com.gionee.aora.numarea.export;

/**
 * Copyright (c) 2001, ÉîÛÚÊÐ°ÂÈíÍøÂç¿Æ¼¼¹«Ë¾ÑÐ·¢²¿
 * All rights reserved.
 *
 * @file IUpdataResult.java
 * ÕªÒª:¸üÐÂ¹éÊôµØÊý¾Ý¿âµÄ²Ù×÷½á¹û
 *
 * @author yewei
 * @data 2011-5-20
 * @version 
 *
 */
public interface IUpdataResult
{
	/**
	 * ¸üÐÂ³É¹¦
	 */
	final static public int RESULT_SUCCESS = 2;
	/**
	 * ÍøÂçÁ¬½Ó³ö´í
	 */
	final static public int RESULT_ERROR_CONNECT_FAILD = 1;
	/**
	 * ÍøÂçÁ¬½Ó³¬Ê±
	 */
	final static public int RESULT_ERROR_CONNECT_TIMEOUT = 0;
	/**
	 * ½âÎöÊý¾Ý´íÎó
	 */
	final static public int RESULT_ERROR_PARSE_DB_FAILD = 4;
	/**
	 * È¡ÏûÉý¼¶
	 */
	final static public int RESULT_USER_CANCEL_UPDATA = 5;
	/**
	 * ¸üÐÂ²Ù×÷ÕýÔÚ½øÐÐÖÐ
	 */
	final static public int RESULT_ERROR_UPDATA_PROCESSING = 6;
	/**
	 * ³õÊ¼»¯Êý¾Ý³É¹¦
	 */
	final static public int RESULT_INIT_FINISH = 7;
	/**
	 * Êý¾Ý¿âÒÑ¾­ÊÇ×îÐÂ°æ±¾ÁË
	 */
	final static public int RESULT_DB_IS_LAST_VERSION = 8;
	/**
	 * ÕýÔÚ½øÐÐ³õÊ¼»¯
	 */
	final static public int RESULT_INIT_PROCESSING = 9;
	/**
	 * ��������
	 */
	final static public int RESULT_DOWNLOADING = 10;
}
