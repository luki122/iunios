package com.aurora.downloadIcon.http;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;

import com.aurora.downloadIcon.bean.IconResponseProp;
import com.aurora.downloadIcon.utils.FileLog;
import com.aurora.downloadIcon.utils.Globals;
import com.aurora.downloadIcon.utils.Log;
import com.aurora.downloadIcon.utils.SystemUtils;
import com.aurora.downloadIcon.utils.Utils2IconLocal;
import com.aurora.downloadIcon.utils.WifiHelper;
import com.aurora.downloadIcon.utils.WifiHelper.WifiStateException;

public class HttpRequestDataProxy {
	public static final String TAG = "HttpRequestDataProxy";

	public static String doGet(String requestUrl, Context context)
			throws MalformedURLException, IOException, WifiStateException {
		// TODO:xiejun
		WifiHelper.assertWifiState(context);
		String result = null;
		if (Globals.isTestCase) {
			result = SystemUtils.getFromAssets(context, "icon_infos.json");
			//Log.i("MainActivity", "***********************************");
		} else {
			result = HttpRequestData.doRequest(requestUrl);

		}
		return result;
	}

	public static String doPost(String uri, String jsonString, Context context)
			throws ClientProtocolException, IOException, WifiStateException {
		WifiHelper.assertWifiState(context);
		String result = null;
		if (Globals.isTestCase) {
			// result = SystemUtils.getFromAssets(context, "icon_infos.json");
			result = HttpRequestData.doPost(Globals.HTTP_REQUEST_URL_TEST,
					jsonString);
		} else {
			//Log.i("test", "*****************00000******************");
			result = HttpRequestData.doPost(uri, jsonString);
		}
		return result;
	}

	public static Bitmap doGetBitmap(IconResponseProp iconProp, Context context)
			throws Exception {
		Bitmap bitmap = null;
		//Log.i("test",
		//		"################################doGetBitmap  begin#############################");
		if (Globals.isTestCase) {
			//bitmap = Utils2IconLocal.getIconFromLocalDir();
			bitmap = HttpRequestData.doGetIconBitmap(iconProp.getPath(),iconProp.getResolution(),
					context);
		} else {
			bitmap = HttpRequestData.doGetIconBitmap(iconProp.getPath(),iconProp.getResolution(),
					context);
		}
		//Log.i("test",
		//		"################################doGetBitmap  end#############################");
		//Log.i("test",
		//		"################################doGetBitmap  savebitmap begin############ bitmap = "+bitmap);
		//Zome bitmap as desity
		
		if (bitmap != null) {
			
			String iconName = dealIconName(iconProp);
			Log.i("test","iconName = "+iconName);
			if (iconName != null) {
					Utils2IconLocal.dealAndSavedIcon(iconName,
							new BitmapDrawable(context.getResources(), bitmap),
							context);
			}
		}
		//Log.i("test",
		//		"################################doGetBitmap  savebitmap end#############################");
		return bitmap;
	}

	public static String dealIconName(IconResponseProp iconProp) {
		String iconName = null;
		if (iconProp.getPackageName() != null&&!"".equals(iconProp.getPackageName().trim())) {
			iconName = iconProp.getPackageName();
			if (iconProp.getClassName() != null&&!"".equals(iconProp.getClassName().trim())) {
				iconName += "_" + iconProp.getClassName();
			}
		}
		return iconName;
	}
}
