
package com.mediatek.wappush.pushparser;

import java.io.InputStream;

import android.util.Log;

public class SlWbxmlParser extends Parser {
    private static String TAG = "PUSH";

    public static final String[] TAG_TABLE = {
        "sl"
    };

    public static final String[] ATTR_START_TABLE = {
            "action=execute-low", "action=execute-high", "action=cache", "href", "href=http://",
            "href=http://www.", "href=https://", "href=https://www."
    };

    public static final String[] ATTR_VALUE_TABLE = {
            ".com/", ".edu/", ".net/", ".org/"
    };

    public SlWbxmlParser(String paramString) {
        super(paramString);
    }

    public ParsedMessage parse(InputStream paramInputStream) {
        SlMessage localSlMessage = null;
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
                        if ("sl".equalsIgnoreCase(str1)) {
                            str2 = localWbxmlParser.getNamespace();

                            localSlMessage = new SlMessage("SL");
                            localSlMessage.url = localWbxmlParser.getAttributeValue(str2, "href");
                            String str3 = localWbxmlParser.getAttributeValue(str2, "action");

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
                        str1 = localWbxmlParser.getName();
                        if (!"sl".equalsIgnoreCase(str1))
                            ;
                    case 1:
                }

                i = localWbxmlParser.next();
            }
        } catch (Exception localException) {
            Log.e(TAG, "Parser Error:" + localException.getMessage());
            return null;
        }

        return localSlMessage;
    }
}
