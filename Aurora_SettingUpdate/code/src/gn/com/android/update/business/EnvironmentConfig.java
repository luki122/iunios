package gn.com.android.update.business;

import java.io.File;

public class EnvironmentConfig {

    public static final String GIONEE_OTA_TEST_FLAGE_FILE_NAME = "/otatest1234567890";

    public static final String GIONEE_OTA_TEST_PACKAGE_FLAGE_TEST = "/test";
    public static final String GIONEE_OTA_TEST_PACKAGE_FLAGE_NORMAL = "/normal";
    
    public static final String GIONEE_OTA_CONFIG_FILE_NAME =  File.separator +"system" + File.separator +"iuni" +
    File.separator  + "aurora" +File.separator +"ota" + File.separator + "config.xml" ;
    /* Official version for Official server */
    public static final int NORMAL_ENVIRONMENT_NORMAL_VERSION = 1;
    /* test version for Official server */
    public static final int NORMAL_ENVIRONMENT_TEST_VERSION = 2;
    /* Official version for test server */
    public static final int TEST_ENVIRONMENT_NORMAL_VERSION = -1;
    /* test version for test server */
    public static final int TEST_ENVIRONMENT_TEST_VERSION = -2;
    
    /*test version for developer*/
    public static final int TEST_ENVIRONMENT_DEVELOPER = -3;
}
