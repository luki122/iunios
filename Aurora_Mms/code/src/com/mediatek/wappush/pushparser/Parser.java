
package com.mediatek.wappush.pushparser;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

public abstract class Parser {
    public static final String TAG = "PUSH";

    protected String m_mimetype;

    protected Parser(String paramString) {
        this.m_mimetype = paramString;
    }

    public static Parser createParser(String paramString) {
        Object localObject = null;

        if (paramString.equals("text/vnd.wap.si"))
            localObject = new SiTextParser(paramString);
        else if (paramString.equals("application/vnd.wap.sic"))
            localObject = new SiWbxmlParser(paramString);
        else if (paramString.equals("text/vnd.wap.sl"))
            localObject = new SlTextParser(paramString);
        else if (paramString.equals("application/vnd.wap.slc"))
            localObject = new SlWbxmlParser(paramString);
        else if (paramString.equals("text/vnd.wap.co"))
            localObject = new CoTextParser(paramString);
        else if (paramString.equals("application/vnd.wap.coc"))
            localObject = new CoWbxmlParser(paramString);
        else {
            Log.e("PUSH", "createParser: wrong type!" + paramString);
        }

        return (Parser) localObject;
    }

    public ParsedMessage parseFile(String paramString) {
        if (paramString != null) {
            try {
                FileInputStream localFileInputStream = new FileInputStream(paramString);
                ParsedMessage localParsedMessage = parse(localFileInputStream);
                localFileInputStream.close();
                return localParsedMessage;
            } catch (FileNotFoundException localFileNotFoundException) {
                Log.e("PUSH", "File Not Found" + paramString);
                return null;
            } catch (IOException localIOException) {
                Log.e("PUSH", "InputStream Close Error");
                return null;
            }
        }
        return null;
    }

    public ParsedMessage parseData(byte[] paramArrayOfByte) {
        if (paramArrayOfByte != null) {
            ByteArrayInputStream localByteArrayInputStream = new ByteArrayInputStream(
                    paramArrayOfByte);
            ParsedMessage localParsedMessage = parse(localByteArrayInputStream);
            try {
                localByteArrayInputStream.close();
            } catch (IOException localIOException) {
                Log.e("PUSH", "InputStream Close Error:");
                return null;
            }
            return localParsedMessage;
        }
        return null;
    }

    protected abstract ParsedMessage parse(InputStream paramInputStream);
}
