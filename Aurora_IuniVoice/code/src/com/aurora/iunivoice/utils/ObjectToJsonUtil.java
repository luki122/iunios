package com.aurora.iunivoice.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.aurora.datauiapi.data.bean.AppUpdateInfo;
import com.aurora.datauiapi.data.bean.AppUpdateParams;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class ObjectToJsonUtil {

	public static String getUpJason(Context context) {
		String packageName = context.getPackageName();
		int versionCode = 1;
		String versionName = null;
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);
			versionCode = pi.versionCode;
			versionName = pi.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		AppUpdateParams obj = new AppUpdateParams();
		List<AppUpdateInfo> upinputItems = new ArrayList<AppUpdateInfo>();

		AppUpdateInfo tmp = new AppUpdateInfo();
		tmp.setPackageName(packageName);
		tmp.setVersionCode(versionCode);
		tmp.setVersionName(versionName);

		upinputItems.add(tmp);
		obj.setInstApps(upinputItems);

		StringWriter str = new StringWriter();

		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(str, obj);
		} catch (JsonParseException e) {
			e.printStackTrace();
			return null;
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return str.toString();
	}
}
