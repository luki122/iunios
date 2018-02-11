package datas;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.aurora.weatherdata.ManagerThread;
import com.aurora.weatherdata.WeatherManager;
import com.aurora.weatherdata.implement.Command;
import com.aurora.weatherdata.implement.DataResponse;
import com.aurora.weatherdata.interf.INotifiableController;
import com.aurora.weatherdata.interf.INotifiableManager;
import com.aurora.weatherforecast.AuroraWeatherWarnDeatilInfo;
import com.aurora.weatherforecast.R;
import com.aurora.weatherforecast.WeatherMainFragment;

import datas.LocationProvider.ILocateResult;
public class DynamicDeskIconService extends Service implements INotifiableController {

	public static final String DYNAMIC_WEATHER_SERVICE="com.aurora.weather.dynamic.deskicon";
	private String localCityName="";
	private int localCityId = -1;
	@Override
	public IBinder onBind(Intent intent) {
		
		return null;
	}

	private WeatherManager mManager;
	private ManagerThread mThread;
	private void initManager() {
		mManager = new WeatherManager();
		mThread = new ManagerThread(mManager);
		mThread.init(this);
		
		//注册定位城市监听
	}
	
	private void requestWeatherInfo(int cityId)
	{
		localCityId = cityId;
		
		String id = ((Integer)cityId).toString();
		
		String maxDays = "6";
		
		mManager.getWeatherList(new DataResponse<WeatherDataEveryDay>() {
			@Override
			public void run() {
				if (value != null) {
				}
			}
		}, this, id, maxDays, false);
	}
	LocationProvider provider;
	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
			provider=new LocationProvider(getApplicationContext(), new ILocateResult() {
				
				@Override
				public void locateSuccess(String cityName, int cityId) {
					CitysHelp help = CitysHelp.getInstance(getApplicationContext());
					localCityName=cityName;
					if(help.getCityFromPhone().size()==0)
					{
						WeatherCityInfo city = new WeatherCityInfo();
						city.setCityName(cityName);
						city.setID(cityId);
						List<WeatherCityInfo> citys = new ArrayList<WeatherCityInfo>();
						citys.add(city);
						help.saveCitys(citys);
					}
					requestWeatherInfo(cityId);
				}
				
				@Override
				public void locateError() {
					// TODO Auto-generated method stub
					
				}
			});
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		initManager();
	}
	

	@Override
	public void onDestroy() {
		super.onDestroy();
		if ( mThread != null ) {
			 mThread.quit();
		}
		if ( mManager != null ) {
			 mManager.setController(null);
		}
		if(provider!=null)
		{
			provider.dispose();
		}
	}
	
	@Override
	public void onWrongConnectionState(int state, INotifiableManager manager,
			Command<?> source) {
	}

	@Override
	public void onError(int code, String message, INotifiableManager manager) {
	}

	@Override
	public void onMessage(String message) {
		// TODO Auto-generated method stub
	}

	private ArrayList<WeatherWarningInfo> warningInfos = null;
	
	@Override
	public void runOnUI(DataResponse<?> response) {
		WeatherDataEveryDay wd = (WeatherDataEveryDay) response.value;
		if(wd!=null)
		{
			WeatherHourInfo info=wd.getHourInfo();
			if(info!=null)
			{
				int curTemp=(int) info.getCurTemp();
				String weahterType=info.getmWeatherType();
				sendBroadcast(curTemp,weahterType);

				String title = getString(R.string.desk_tip_content, 
						info.getmWeatherType(), 
						(int) info.getmCurTemp(), 
						wd.getWeatherAirQuality().getAirQualityDesc());
                insertResolver(getContentResolver(), title);
			}
			ArrayList<WeatherWarningInfo> warns = (ArrayList<WeatherWarningInfo>) wd.getWeatherWarningList();
			if(warns!=null && warns.size()>0&&!isOutOfTime())
			{
				warningInfos = warns;
				WeatherWarningInfo warnInfo = warns.get(0);
				WarnInfoHelp warnInfoHelp = WarnInfoHelp.getInstance(getApplicationContext());
				if(localCityId != -1)
				{
				   if(!warnInfoHelp.isExistWarnInfo(localCityId, warnInfo))
				   {
					   if(info!=null)
					   {
						   sendNotification(localCityId, localCityName, warnInfo.getTitle(),info.getmWeatherDate(),info.getmWeatherType());
					   }
				   }
				}
			}
		}
	}
	
	private static final Uri CONTENT_URI = Uri.parse("content://com.aurora.reminder");
	private static final String INSERT_TITLE = "title", INSERT_ACTION = "action", INSERT_PACKAGE = "package";
	public static void insertResolver(ContentResolver cr, String title){
		try {
			ContentValues contentValues = new ContentValues();
			contentValues.put(INSERT_TITLE, title);
			contentValues.put(INSERT_ACTION, "com.aurora.weatherforecast.AuroraWeatherMain");//跳转到主页面
			contentValues.put(INSERT_PACKAGE, "com.aurora.weatherforecast");
			cr.insert(CONTENT_URI, contentValues);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("DynamicDeskIconService", "content://com.aurora.reminder not exist");
		}
	}
	
	
	
	private static final String PACKAGE_NAME = "com.aurora.weatherforecast";
	public static final String COME_FROM_KEY="come_from";
	public static final String WEATHER_DATE_KEY = "weather_date";
	public static final String WEATHER_WARN_INFO_KEY = "warn_info_key";
	public static final int COME_FROM_NOTIFICATION = 0X25541;
	public static final String WEATHER_TYPE="weather_type";
	private static int requestCode = 1;
	private final String WARN_INFO_NOTIFICATION = "warn_info_notification";
	
	private static final String TITLE = "title",WEATHER = "weather", LEVEL = "level",DETAIL = "detail";
	private void saveNotication(){
		SharedPreferences sp =getSharedPreferences(WARN_INFO_NOTIFICATION, Context.MODE_PRIVATE);
		if(warningInfos == null)
			return;
		WeatherWarningInfo info = warningInfos.get(0);
		sp.edit().putString(TITLE, info.getTitle()).putString(WEATHER, info.getWeather()).putString(LEVEL, info.getLevel()).putString(DETAIL, info.getDetail()).commit();
	}
	
	private boolean isExistInLocal(){
		if(warningInfos==null||warningInfos.size()==0)
			return false;
		WeatherWarningInfo info = new WeatherWarningInfo();
		SharedPreferences sp =getSharedPreferences(WARN_INFO_NOTIFICATION, Context.MODE_PRIVATE);
		if(!sp.contains(TITLE))
			return false;
		info.setTitle(sp.getString(TITLE, ""));
		info.setLevel(sp.getString(LEVEL, ""));
		info.setDetail(sp.getString(DETAIL, ""));
		info.setWeather(sp.getString(WEATHER, ""));
		return info.equals(warningInfos.get(0));
	}
	
	private final int NIGHT = 23 * 60,MORNING = 8 * 60+30;
	
	/**
	 * 当时间为晚上11点到第二天8点半则不提示预警
	 * @return
	 */
	private boolean isOutOfTime(){
		
		Calendar now = Calendar.getInstance();
		int miniute = now.get(Calendar.HOUR_OF_DAY)*60 + now.get(Calendar.MINUTE);
		
		if(miniute >= NIGHT || miniute <= MORNING)
		{
			Log.e("jadon", miniute+"   超出预警时间");
			return true;
		}
		
		return false;
		
	}
	
	
	private void sendNotification(int cityId,String city,String warnTitle,String weatherDate,String weatherType){
		ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> infos = manager.getRunningTasks(50);
		if(infos !=null&&!isExistInLocal())
		{
			if(!infos.get(0).topActivity.getPackageName().equals(PACKAGE_NAME))
			{
				saveNotication();
				Intent weatherMainActivity = new Intent(getApplicationContext(), AuroraWeatherWarnDeatilInfo.class);
				weatherMainActivity.putExtra(COME_FROM_KEY, COME_FROM_NOTIFICATION);
				weatherMainActivity.putExtra(WEATHER_DATE_KEY, weatherDate);
				weatherMainActivity.putExtra(WEATHER_TYPE, weatherType);
				weatherMainActivity.putParcelableArrayListExtra(WEATHER_WARN_INFO_KEY, warningInfos);
				PendingIntent weatherManPending = PendingIntent.getActivity(getApplicationContext(), requestCode++, weatherMainActivity, PendingIntent.FLAG_UPDATE_CURRENT);
				NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
				Notification n = new Notification(R.drawable.weather_icon, getString(R.string.notification_title), System.currentTimeMillis());
			    n.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				n.setLatestEventInfo(getApplicationContext(), getString(R.string.notification_title),getString(R.string.notification_content, city,warnTitle), weatherManPending);
				n.flags |= Notification.FLAG_AUTO_CANCEL;
//			            | Notification.FLAG_ONGOING_EVENT;
				nm.notify(cityId, n);
			}
		}
	}
	
	private void sendBroadcast(int curTemp,String weatherType){
		Calendar cal = Calendar.getInstance();
		Log.e("jadon", "发送广播到lanuch curTemp="+curTemp+"     weatherType="+weatherType + "time ="+(cal.get(Calendar.MONTH)+"-"+cal.get(Calendar.DAY_OF_MONTH)+" "+cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)));
		Intent intent = new Intent(WeatherMainFragment.UPDATE_BROADCAST_ACTION);
		intent.putExtra("curTemp", curTemp);
		intent.putExtra("weatherType", weatherType);
		intent.putExtra("cityname", localCityName);
		sendBroadcast(intent);
	}
}
