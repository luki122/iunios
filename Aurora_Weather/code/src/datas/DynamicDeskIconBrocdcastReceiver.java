package datas;

import java.util.ArrayList;
import java.util.List;

import com.aurora.weatherdata.bean.CityItem;
import com.aurora.weatherdata.db.CNCityAdapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DynamicDeskIconBrocdcastReceiver extends BroadcastReceiver {

	public static final String DYNAMIC_WEATHER_ICON="com.aurora.weatherfoecast.request.updateweather";
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent!=null)
		{
			if (intent.getAction().equals(DYNAMIC_WEATHER_ICON)
					|| intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
				Log.e("jadon", "接受到广播"+intent.getAction()+"              "+intent.getStringExtra("comfrom"));
				Intent dynamicDeskIcon = new Intent(DynamicDeskIconService.DYNAMIC_WEATHER_SERVICE);
				context.startService(dynamicDeskIcon);
			}
		}

	}
}
