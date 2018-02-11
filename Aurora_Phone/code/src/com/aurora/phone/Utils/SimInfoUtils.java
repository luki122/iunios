package com.android.phone;  
  
import java.io.ByteArrayOutputStream;  
import java.io.IOException;  
import java.io.InputStream;   
import java.net.HttpURLConnection;  
import java.net.URL; 

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract.Data;
import android.util.Log;
import com.android.phone.AuroraTelephony.SIMInfo;
import com.android.phone.AuroraTelephony.SimInfo;
  
public class SimInfoUtils {  
    private static final String TAG = "SimInfoUtils";


    public static boolean isCardInsert(Context ctx, int slot) {
    	SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(ctx, slot);
    	return simInfo != null;    	
    }

    

}  