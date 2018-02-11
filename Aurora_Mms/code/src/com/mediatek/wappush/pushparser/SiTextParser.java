
package com.mediatek.wappush.pushparser;

import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;

import android.util.Log;
import android.util.Xml;

public class SiTextParser extends Parser {
    private static String TAG = "PUSH";

    public static final String SI = "si";

    public static final String INDICATION = "indication";

    public static final String INFO = "info";

    public SiTextParser(String paramString) {
        super(paramString);
    }

    public ParsedMessage parse(InputStream paramInputStream) {
        SiMessage localSiMessage = null;
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
                        if ("si".equalsIgnoreCase(str1)) {
                            localSiMessage = new SiMessage(SiMessage.TYPE);
                        } else if ("indication".equalsIgnoreCase(str1)) {
                            str2 = localXmlPullParser.getNamespace();

                            if (localSiMessage != null) {
                                localSiMessage.siid = localXmlPullParser.getAttributeValue(str2,
                                        "si-id");
                                localSiMessage.url = localXmlPullParser.getAttributeValue(str2,
                                        "href");
                                localSiMessage.create = SiDateDecoderUtil.W(localXmlPullParser
                                        .getAttributeValue(str2, "created"));
                                localSiMessage.expiration = SiDateDecoderUtil.W(localXmlPullParser
                                        .getAttributeValue(str2, "si-expires"));
                                String str3 = localXmlPullParser.getAttributeValue(str2, "action");
                                localSiMessage.text = localXmlPullParser.nextText();

                                if (str3 != null) {
                                    str3 = str3.toLowerCase();
                                }
                                localSiMessage.action = 2;
                                if ("signal-none".equals(str3))
                                    localSiMessage.action = 0;
                                else if ("signal-low".equals(str3))
                                    localSiMessage.action = 1;
                                else if ("signal-medium".equals(str3))
                                    localSiMessage.action = 2;
                                else if ("signal-high".equals(str3))
                                    localSiMessage.action = 3;
                                else if ("delete".equals(str3)) {
                                    localSiMessage.action = 4;
                                }
                            }

                        }

                        break;
                    case 3:
                        str1 = localXmlPullParser.getName();
                        if (!"indication".equalsIgnoreCase(str1))
                            ;
                    case 1:
                }

                i = localXmlPullParser.next();
            }
        } catch (Exception localException) {
            Log.e(TAG, "Parser Error:" + localException.getMessage());
            return null;
        }

        return localSiMessage;
    }
}
