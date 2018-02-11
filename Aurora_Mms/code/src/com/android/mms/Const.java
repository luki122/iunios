package com.android.mms;

public interface Const {
    
    /**
     * China Mobile Communications Corporation(CMCC)
     * support GSM and TD-SCDMA
     */
    String OPERATOR_CMCC = "OP03";
    
    /**
     * China Unicom support GSM and WCDMA
     */
    String OPERATOR_UNICOM = "OP02";
    
    /**
     * China Telecom support CDMA
     */
    String OPERATOR_TELECOM = "OP01";
    
    //Gionee <guoyx> <2013-04-17> added for CR00797658 begin
    /**
     * Default theme style is "0"
     */
    String THEME_STYLE_DEFAULT = "0";
    
    /**
     * Litht theme style is "1"
     */
    String THEME_STYLE_LIGHT = "1";
    
    /**
     * Dark theme style is "2"
     */
    String THEME_STYLE_DARK = "2";
    
    /**
     * Feature of switch "ro.gn.theme.style"
     */
    String FEATURE_GN_THEME_STYLE = "ro.gn.theme.style";
    //Gionee <guoyx> <2013-04-17> added for CR00797658 end
    
    //Gionee <guoyx> <2013-05-07> added for CR00808519 begin
    /**
     * Feature of switch "ro.gn.gemini.support" for the Multi sim solution.(just for MTK now)
     */
    String FEATURE_GN_GEMINI_SUPPORT = "ro.gn.gemini.support";
    //Gionee <guoyx> <2013-05-07> added for CR00808519 end

}
