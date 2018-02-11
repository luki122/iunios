package gn.com.android.update.business;

public class NetworkConfig {
	public static String IUNIOS_DEVELOPER_HOST; //¿ª·¢·þÎñÆ÷,ip
    public static final String IUNIOS_TEST_HOST = "http://otatest.iunios.com"; //²âÊÔ·þÎñÆ÷
    public static final String IUNIOS_NORMAL_HOST = "http://ota.iunios.com";   //ÕýÊ½·þÎñÆ÷(ÒÑÌá½»£¬ÒÑ·¢²¼)
    
    public static final String IUNIOS_ABROAD_HOST = "http://global.ota.iunios.com";   //海外服务器地址
    public static String IUNIOS_NORMAL_TEST;    //±£´æÕýÊ½·þÎñÆ÷µÄ°µÃÅ
   
    
    /*used for developer test*/
    public static String IUNIOS_DEVELOPER_HOST_PREFX = "http://";
    
    public static final String IUNIOS_HTTP_CHECK = "/check.do";

    public static final String TEST_HOST = "http://test1.gionee.com";
    public static final String NORMAL_HOST = "http://update.gionee.com";
    public static final String GIONEE_HTTP_CHECK = "/ota/check.do?";

    public static final int GIONEE_CONNECT_TIMEOUT = 6 * 1000;
    public static final int GIONEE_SOCKET_TIMEOUT = 10 * 1000;

    public static final int CONNECTION_TYPE_WAP = 100;
    public static final int CONNECTION_TYPE_NET = 201;

    public static enum ConnectionType {
        CONNECTION_TYPE_IDLE, CONNECTION_TYPE_WIFI, CONNECTION_TYPE_3G, CONNECTION_TYPE_2G, CONNECTION_TYPE_4G
    }

    public static final String CONNECTION_MOBILE_DEFAULT_HOST = "10.0.0.172";
    public static final int CONNECTION_MOBILE_DEFAULT_PORT = 80;

    public static final int CHECK_TYPE_DEFAULT = 1;
    public static final int CHECK_TYPE_AUTO = 2;
    public static final int CHECK_TYPE_PUSH = 3;

}
