package datas;

import android.content.Context;

import com.aurora.weatherdata.db.WarnInfoAdapter;

public class WarnInfoHelp {

	private static WarnInfoHelp instance=null;
	
	private WarnInfoAdapter adapter;
	
	private WarnInfoHelp(Context context){
		adapter = new WarnInfoAdapter(context);
	}
	
	public synchronized static WarnInfoHelp getInstance(Context context){
		if(instance == null)
		{
			instance = new WarnInfoHelp(context);
		}
		return instance;
	}
	
	public void release(){
		adapter.close();
	}
	
	public void addWarnInfo(int cityId,WeatherWarningInfo info){
		if(!adapter.isExistWarnInfo(cityId, info))
		{
		  adapter.addWarnInfo(cityId, info);
		}
	}
	
	public void deleteWarnInfo(int cityId){
		adapter.deleteWarnInfo(cityId);
	}
	
	public boolean isExistWarnInfo(int cityId,WeatherWarningInfo info){
		return adapter.isExistWarnInfo(cityId, info);
	}
	
	
}
