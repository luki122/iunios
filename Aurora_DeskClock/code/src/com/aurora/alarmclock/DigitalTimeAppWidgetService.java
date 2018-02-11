package com.aurora.alarmclock;

import java.util.Calendar;
import java.util.Locale;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.android.deskclock.AlarmClock;
import com.android.deskclock.R;
import com.aurora.utils.MobileUtil;
//这个Service暂停不用，用timeservice代替
public class DigitalTimeAppWidgetService extends Service{

	public static final String TAG = "222222";  
      
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver()  
    {  
        @Override  
        public void onReceive(Context context, Intent intent)  
        {  
            String action = intent.getAction();  
            //Log.d(TAG, "DigitalTimeAppWidgetService onReceive() " + action);  
              
            if (action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_DATE_CHANGED)
            		|| action.equals(Intent.ACTION_TIMEZONE_CHANGED) || action.equals(Intent.ACTION_TIME_TICK) )  
            {  
            	Message msg = new Message();
            	msg.obj = context;
            	handler.sendMessage(msg);
            }  
        }  
    };  
    
    private Handler handler = new Handler(){
    	
    	public void handleMessage(android.os.Message msg) {
    		super.handleMessage(msg);
    		notifyWidget((Context)msg.obj);
    	};
    	
    };
    
    @Override  
    public void onCreate()  
    {  
        //Log.d(TAG, "DigitalTimeAppWidgetService onCreate()");  
        super.onCreate();  
        
        IntentFilter filter = new IntentFilter();  
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(mIntentReceiver, filter);
    }
    
    
      
    @Override  
    public void onStart(Intent intent, int startId)  
    {  
    	super.onStart(intent, startId);  
    	//String action = intent.getAction();  
    	//Log.d(TAG, "DigitalTimeAppWidgetService onStart() ");  

    	notifyWidget(getApplicationContext());
    } 
    
    
    
    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
    	//Log.d(TAG, "DigitalTimeAppWidgetService onStartCommand() ");
    	notifyWidget(getApplicationContext());
		return Service.START_STICKY;
	}



	private Bitmap makeBitMap( LinearLayout poplayout ) {
    	
    	Bitmap bitmap = null;
    	//打开图像缓存
    	poplayout.setDrawingCacheEnabled(true);
    	//必须调用measure和layout方法才能成功保存可视组件的截图到png图像文件
    	//测量View大小
    	poplayout.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
    			MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
    	//发送位置和尺寸到View及其所有的子View
    	poplayout.layout(0, 0, poplayout.getMeasuredWidth(), poplayout.getMeasuredHeight());
    	try{
    		//获得可视组件的截图
    		bitmap = poplayout.getDrawingCache();
    	}catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return bitmap;
    }

    private void notifyWidget(Context context) {
    	//Log.d(TAG, "DigitalTimeAppWidgetService notifyWidget()"); 

    	String[] days = this.getResources().getStringArray(R.array.daysChinese);
    	LinearLayout poplayout = MobileUtil.getAppWidgetTimeLayout(context);
		RemoteViews remoteViews = MobileUtil.getAppWidgetTimeRemoteViews(context);
    	//Typeface tf = Typeface.createFromFile("system/fonts/Roboto-Light.ttf");
    	String format = android.text.format.DateFormat.is24HourFormat(context) ? DigitalTimeAppWidgetProvider.M24 : DigitalTimeAppWidgetProvider.M12;
    	
    	Calendar calendar = Calendar.getInstance();
        CharSequence newTime = DateFormat.format(format, calendar);

        String weekDay = String.valueOf(calendar.get(Calendar.MONTH) + 1) + "月" + calendar.get(Calendar.DAY_OF_MONTH) + "日     " + days[calendar.get(Calendar.DAY_OF_WEEK) - 1];	
    	TextView tmpText = (TextView)poplayout.findViewById(R.id.txtAll);
    	tmpText.setText(weekDay);
        if (DeskClockWidgetUtils.getCurrentLocale(Locale.getDefault()) ) {
        	tmpText.setVisibility(View.VISIBLE);
        } else {
        	tmpText.setVisibility(View.GONE);
        }
    	
    	tmpText = (TextView)poplayout.findViewById(R.id.timedisplay);
        tmpText.setText(newTime);
        tmpText.setTypeface(DeskClockWidgetUtils.auroraGetTitleFontTypeFaceEx());
        
        tmpText = (TextView)poplayout.findViewById(R.id.ampm);
        tmpText.setText(calendar.get(Calendar.AM_PM) == 0 ? "上午" : "下午");
        //tmpText.setVisibility(format == DigitalTimeAppWidgetProvider.M12 ? View.VISIBLE : View.GONE);
        tmpText.setVisibility(View.GONE);
        tmpText.setTypeface(DeskClockWidgetUtils.auroraGetTitleFontTypeFaceEx());

    	Bitmap bitmap = makeBitMap( poplayout );

    	remoteViews.setImageViewBitmap(R.id.imageview, bitmap);
    	
        Intent intent=new Intent(context, AlarmClock.class);  
        PendingIntent pendingIntent=PendingIntent.getActivity(context, 0, intent, 0);  
        remoteViews.setOnClickPendingIntent(R.id.imageview, pendingIntent); 

    	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
    	appWidgetManager.updateAppWidget(new ComponentName(this, DigitalTimeAppWidgetProvider.class),
    			remoteViews);

    	bitmap.recycle();
    	System.gc();
    }
 
    @Override  
    public void onDestroy()  
    {  
        //Log.d(TAG, "DigitalTimeAppWidgetService onDestroy()");  
          
        unregisterReceiver(mIntentReceiver);  
          
        super.onDestroy();  
    }  
    @Override  
    public IBinder onBind(Intent intent)  
    {  
        //Log.d(TAG, "DigitalTimeAppWidgetService onBind()");  
        return null;  
    }  
}
