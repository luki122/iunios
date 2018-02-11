
package com.mediatek.wappush.pushparser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class SiDateDecoderUtil {
    static SimpleDateFormat jS = new SimpleDateFormat("yyyyMMddHHmmss");

    static SimpleDateFormat jT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static int V(String paramString) {
        if (paramString == null)
            return 0;
        try {
            jS.setTimeZone(TimeZone.getTimeZone("GMT"));
            if (paramString.length() < 14) {
                String str = String.format("%-14s", new Object[] {
                    paramString
                }).replace(' ', '0');
                return (int) (jS.parse(str).getTime() / 1000L);
            }
            return (int) (jS.parse(paramString).getTime() / 1000L);
        } catch (ParseException localParseException) {
        }
        return 0;
    }

    static int W(String paramString) {
        if (paramString == null) {
            return 0;
        }
        try {
            jT.setTimeZone(TimeZone.getTimeZone("GMT"));
            return (int) (jT.parse(paramString).getTime() / 1000L);
        } catch (ParseException localParseException) {
        }
        return 0;
    }
}
