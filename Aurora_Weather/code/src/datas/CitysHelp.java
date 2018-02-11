package datas;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aurora.weatherdata.db.LocalCityAdapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

public class CitysHelp {
	private LocalCityAdapter localCityAdapter;
	private Context context;
	private static CitysHelp instance;
	public synchronized static CitysHelp getInstance(Context context){
		if(instance==null)
		{
			instance=new CitysHelp(context);
		}
		return instance;
	}
	
	private CitysHelp(Context context)
	{
		this.context=context;
		localCityAdapter=new LocalCityAdapter(context);
	}
	
	private List<WeatherCityInfo> getAllCitys(){
		List<WeatherCityInfo> citys=localCityAdapter.selectAll();
		return citys;
	}
	
	
	public  List<WeatherCityInfo> getCityFromPhone(){
		return getAllCitys();
	}
	
	public void saveCitys(List<WeatherCityInfo> citys){
		save(citys);
	}
	
	private void save(List<WeatherCityInfo> citys){
		localCityAdapter.inserCityList(citys);
	}
	
	public void release(){
		localCityAdapter.close();
		instance=null;
	}
	
}
