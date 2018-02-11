package com.gionee.aora.numarea.export;

/**
 * Copyright (c) 2001, 深圳市奥软网络科技公司研发部
 * All rights reserved.
 *
 * @file IUpdataResult.java
 * 摘要:更新归属地数据库的操作结果
 *
 * @author yewei
 * @data 2011-5-20
 * @version 
 *
 */
public interface IUpdataResult
{
	/**
	 * 更新成功
	 */
	final static public int RESULT_SUCCESS = 2;
	/**
	 * 网络连接出错
	 */
	final static public int RESULT_ERROR_CONNECT_FAILD = 1;
	/**
	 * 网络连接超时
	 */
	final static public int RESULT_ERROR_CONNECT_TIMEOUT = 0;
	/**
	 * 解析数据错误
	 */
	final static public int RESULT_ERROR_PARSE_DB_FAILD = 4;
	/**
	 * 取消升级
	 */
	final static public int RESULT_USER_CANCEL_UPDATA = 5;
	/**
	 * 更新操作正在进行中
	 */
	final static public int RESULT_ERROR_UPDATA_PROCESSING = 6;
	/**
	 * 初始化数据成功
	 */
	final static public int RESULT_INIT_FINISH = 7;
	/**
	 * 数据库已经是最新版本了
	 */
	final static public int RESULT_DB_IS_LAST_VERSION = 8;
	/**
	 * 正在进行初始化
	 */
	final static public int RESULT_INIT_PROCESSING = 9;
	/**
	 * 正在下载
	 */
	final static public int RESULT_DOWNLOADING = 10;
}
