
package com.mediatek.wappush.pushparser;

import java.io.InputStream;

import android.util.Log;

public class SiWbxmlParser extends Parser {
    private static String TAG = "PUSH";

    public static final String[] TAG_TABLE = {
            "si", "indication", "info", "item"
    };

    public static final String[] ATTR_START_TABLE = {
            "action=signal-none", "action=signal-low", "action=signal-medium",
            "action=signal-high", "action=delete", "created", "href", "href=http://",
            "href=http://www.", "href=https://", "href=https://www.", "si-expires", "si-id",
            "class"
    };

    public static final String[] ATTR_VALUE_TABLE = {
            ".com/", ".edu/", ".net/", ".org/"
    };

    public SiWbxmlParser(String paramString) {
        super(paramString);
    }

    public SiMessage parse(InputStream paramInputStream) {
        SiMessage localSiMessage = null;
        try {
            WbxmlParser localWbxmlParser = new WbxmlParser();
            localWbxmlParser.setTagTable(0, TAG_TABLE);
            localWbxmlParser.setAttrStartTable(0, ATTR_START_TABLE);
            localWbxmlParser.setAttrValueTable(0, ATTR_VALUE_TABLE);
            localWbxmlParser.setInput(paramInputStream, null);

            int i = localWbxmlParser.getEventType();

            while (i != 1) {
                String str1 = null;
                String str2 = null;
                switch (i) {
                    case 0:
                        break;
                    case 2:
                        str1 = localWbxmlParser.getName();
                        if ("si".equalsIgnoreCase(str1)) {
                            localSiMessage = new SiMessage(SiMessage.TYPE);
                        } else if ("indication".equalsIgnoreCase(str1)) {
                            str2 = localWbxmlParser.getNamespace();

                            if (localSiMessage != null) {
                                localSiMessage.siid = localWbxmlParser.getAttributeValue(str2,
                                        "si-id");
                                localSiMessage.url = localWbxmlParser.getAttributeValue(str2,
                                        "href");
                                localSiMessage.create = SiDateDecoderUtil.V(localWbxmlParser
                                        .getAttributeValue(str2, "created"));
                                localSiMessage.expiration = SiDateDecoderUtil.V(localWbxmlParser
                                        .getAttributeValue(str2, "si-expires"));
                                String str3 = localWbxmlParser.getAttributeValue(str2, "action");
                                localSiMessage.text = localWbxmlParser.nextText();

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
                        str1 = localWbxmlParser.getName();
                        if (!"indication".equalsIgnoreCase(str1))
                            ;
                    case 1:
                }

                i = localWbxmlParser.next();
            }
        } catch (Exception localException) {
            Log.e(TAG, "Parser Error:" + localException.getMessage());
            return null;
        }

        return localSiMessage;
    }
}
