/*
 * @author zw
 */
package com.aurora.market.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;

import com.aurora.datauiapi.data.bean.UpgradeListObject;
import com.aurora.datauiapi.data.bean.upappListtem;
import com.aurora.datauiapi.data.bean.upcountinfo;
import com.aurora.market.http.data.HttpRequestGetMarketData;
import com.aurora.market.model.DownloadData;

public class DataFromUtils {

	public ArrayList<DownloadData> getUpdateData(Context context) {
		String result = HttpRequestGetMarketData.getUpAppListObject(context);

		ObjectMapper mapper = new ObjectMapper();
		result = result.replaceAll("\t", "");

		UpgradeListObject obj = new UpgradeListObject();
		try {
			mapper.configure(
					DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
					true);
			mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
			mapper.getDeserializationConfig()
					.set(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
							false);

			obj = (UpgradeListObject) mapper.readValue(result,
					UpgradeListObject.class);
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
		ArrayList<DownloadData> down_data = new ArrayList<DownloadData>();
		upDownLoadData(obj, down_data);

		return down_data;
	}

	
	public int getUpdateSum(Context context) {
		String result = HttpRequestGetMarketData.getUpdateCountObject(context);

		ObjectMapper mapper = new ObjectMapper();
		result = result.replaceAll("\t", "");

		upcountinfo obj = new upcountinfo();
		try {
			mapper.configure(
					DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
					true);
			mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
			mapper.getDeserializationConfig()
					.set(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
							false);

			obj = (upcountinfo) mapper.readValue(result,
					upcountinfo.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
			return 0;

		} catch (JsonMappingException e) {
			e.printStackTrace();
			return 0;

		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	

		return obj.getCount();
	}

	
	
	public void upDownLoadData(UpgradeListObject up_obj,
			List<DownloadData> down_data) {
		for (int i = 0; i < up_obj.getUpgradeApps().size(); i++) {
			DownloadData tmp_data = new DownloadData();
			upappListtem list = up_obj.getUpgradeApps().get(i);
			tmp_data.setApkId(list.getId());
			tmp_data.setApkDownloadPath(list.getDownloadURL());
			tmp_data.setApkLogoPath(list.getIcons().getPx256());
			tmp_data.setApkName(list.getTitle());
			tmp_data.setPackageName(list.getPackageName());
			tmp_data.setVersionCode(list.getVersionCode());
			tmp_data.setVersionName(list.getVersionName());
			down_data.add(tmp_data);
		}
	}

}
