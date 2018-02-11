package com.baidu.xcloud.pluginAlbum;

import android.os.Environment;

public class AlbumConfig {

    /**
     * 当前应用的测试云端路径,如果想定义自己的路径,请登录developer.baidu.com查看相当内容.
     */
    public static final String REMOTEPATH = "/apps/iuni云/";///apps/iuni云/ /apps/xcloudDir/album/IUNI/

    /**
     * 当前sdcard目录
     */
    public static final String SDCRADPATH = Environment.getExternalStorageDirectory().getPath();
    /**
     * 相册的主目录位置:sdcard/albumTest
     */
    public static final String LOCALPATH = SDCRADPATH + "/DCIM/";

    /**
     * 相册的下载位置:sdcard/albumTest/download
     */

    public static final String DOWNLOADPATH = LOCALPATH + "cloud/";

	public static final String CACHEPATH = LOCALPATH + "cache/download/";//paul add
	
    /** 请将此ID替换成您申请的APPID **/
    public static final String APPID = "5437751";//正式: 5437751 测试: 2998711
    

    /** 请将此ID替换成您申请的APIKEY **/
    public static final String APIKEY = "gcnpmqh1jcxRDwjsXzx7uGt5";//正式: gcnpmqh1jcxRDwjsXzx7uGt5 , 测试: mBAuU4heZvN178rN8IulKXef
    /** 请将此ID替换成您申请的Secret Key **/
    public static final String SECRETKEY = "ihYTu2nLj5Kd2AtdteyyYr7kpvHnaovo";//正式: ihYTu2nLj5Kd2AtdteyyYr7kpvHnaovo , 测试: bD8pYNGdqon4gcL103763mk3qeb4XITD

    /** 该demo使用的权限列表 **/
    public static final String[] BAIDU_PERMISSIONS = new String[] { "basic", "netdisk" }; 

    public static final String IUNI_TEST_TOKEN = "IUNI-X";// X从1至20均可用
 	public static final String SERVICE_PKG = "com.baidu.xcloud";
 	public static final String SERVICE_CLS = "com.baidu.xcloud.pluginAlbum.AlbumService";

    public static final boolean IUNI_TEST = false;
}
