package com.mediatek.wappush.pushparser;

import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.util.Log;
import android.util.Xml;

public class CoTextParser extends Parser
{
  private static String TAG = "PUSH";
  public static final String CO = "co";
  public static final String OBJECT = "invalidate-object";
  public static final String SERVICE = "invalidate-service";

  public CoTextParser(String paramString)
  {
    super(paramString);
  }

  public CoMessage parse(InputStream paramInputStream) {
    CoMessage localCoMessage = null;
    XmlPullParser localXmlPullParser = Xml.newPullParser();
    try {
      localXmlPullParser.setInput(paramInputStream, null);
      int i = localXmlPullParser.getEventType();

      while (i != 1) {
        String str1 = null;
        String str2 = null;
        switch (i)
        {
        case 0:
          break;
        case 2:
          str1 = localXmlPullParser.getName();
          str2 = localXmlPullParser.getNamespace();

          if ("co".equalsIgnoreCase(str1))
          {
            localCoMessage = new CoMessage(CoMessage.TYPE);
            localCoMessage.objects = new ArrayList();
            localCoMessage.services = new ArrayList();
          } else if ("invalidate-object".equalsIgnoreCase(str1)) {
            if (localCoMessage != null)
              localCoMessage.objects.add(localXmlPullParser.getAttributeValue(str2, "uri"));
          }
          else if (("invalidate-service".equalsIgnoreCase(str1)) && 
            (localCoMessage != null)) {
            localCoMessage.services.add(localXmlPullParser.getAttributeValue(str2, "uri"));
          }

          break;
        case 3:
          str1 = localXmlPullParser.getName();
          if (!"co".equalsIgnoreCase(str1));
        case 1:
        }

        i = localXmlPullParser.next();
      }
    }
    catch (Exception localException) {
      Log.e(TAG, "Parser Error:" + localException.getMessage());
    }

    return localCoMessage;
  }
}