
package com.mediatek.wappush.pushparser;

import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;

import android.util.Log;
import android.util.Xml;

public class SlTextParser extends Parser {
    private static String TAG = "PUSH";

    public static final String SL = "sl";

    public SlTextParser(String paramString) {
        super(paramString);
    }

    public ParsedMessage parse(InputStream paramInputStream) {
        SlMessage localSlMessage = null;
        XmlPullParser localXmlPullParser = Xml.newPullParser();
        try {
            localXmlPullParser.setInput(paramInputStream, null);
            int i = localXmlPullParser.getEventType();

            while (i != 1) {
                String str1 = null;
                String str2 = null;
                switch (i) {
                    case 0:
                        break;
                    case 2:
                        str1 = localXmlPullParser.getName();
                        if ("sl".equalsIgnoreCase(str1)) {
                            str2 = localXmlPullParser.getNamespace();

                            localSlMessage = new SlMessage("SL");
                            localSlMessage.url = localXmlPullParser.getAttributeValue(str2, "href");
                            String str3 = localXmlPullParser.getAttributeValue(str2, "action");

                            if (str3 != null) {
                                str3 = str3.toLowerCase();
                            }
                            localSlMessage.action = 1;
                            if ("execute-low".equals(str3))
                                localSlMessage.action = 1;
                            else if ("execute-high".equals(str3))
                                localSlMessage.action = 2;
                            else if ("cache".equals(str3)) {
                                localSlMessage.action = 3;
                            }

                        }

                        break;
                    case 3:
                        str1 = localXmlPullParser.getName();
                        if (!"sl".equalsIgnoreCase(str1))
                            ;
                    case 1:
                }

                i = localXmlPullParser.next();
            }
        } catch (Exception localException) {
            Log.e(TAG, "Parser Error:" + localException.getMessage());
            return null;
        }

        return localSlMessage;
    }
}
