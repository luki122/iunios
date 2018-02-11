
package com.mediatek.wappush.pushparser;

import java.io.InputStream;
import java.util.ArrayList;

import android.util.Log;

public class CoWbxmlParser extends Parser {
    private static String TAG = "PUSH";

    public static final String[] TAG_TABLE = {
            "co", "invalidate-object", "invalidate-service"
    };

    public static final String[] ATTR_START_TABLE = {
            "uri", "uri=http://", "uri=http://www.", "uri=https://", "uri=https://www."
    };

    public static final String[] ATTR_VALUE_TABLE = {
            ".com/", ".edu/", ".net/", ".org/"
    };

    public CoWbxmlParser(String paramString) {
        super(paramString);
    }

    public ParsedMessage parse(InputStream paramInputStream) {
        CoMessage localCoMessage = null;
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
                        str2 = localWbxmlParser.getNamespace();

                        if ("co".equalsIgnoreCase(str1)) {
                            localCoMessage = new CoMessage(CoMessage.TYPE);
                            localCoMessage.objects = new ArrayList();
                            localCoMessage.services = new ArrayList();
                        }
                        if ("invalidate-object".equalsIgnoreCase(str1)) {
                            if (localCoMessage != null)
                                localCoMessage.objects.add(localWbxmlParser.getAttributeValue(str2,
                                        "uri"));
                        } else if (("invalidate-service".equalsIgnoreCase(str1))
                                && (localCoMessage != null)) {
                            localCoMessage.services.add(localWbxmlParser.getAttributeValue(str2,
                                    "uri"));
                        }

                        break;
                    case 3:
                        str1 = localWbxmlParser.getName();
                        if (!"co".equalsIgnoreCase(str1))
                            ;
                    case 1:
                }

                i = localWbxmlParser.next();
            }
        } catch (Exception localException) {
            Log.e(TAG, "Parser Error:" + localException.getMessage());
        }

        return localCoMessage;
    }
}
