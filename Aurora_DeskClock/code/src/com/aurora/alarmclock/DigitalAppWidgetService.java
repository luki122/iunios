package com.aurora.alarmclock;

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
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.android.deskclock.AlarmClock;
import com.android.deskclock.R;

public class DigitalAppWidgetService extends Service{

	public static final String TAG = "222222";  
      
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver()  
    {  
        @Override  
        public void onReceive(Context context, Intent intent)  
        {  
            String action = intent.getAction();  
            //Log.d(TAG, "DigitalAppWidgetService onReceive() " + action);  
              
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
        //Log.d(TAG, "DigitalAppWidgetService onCreate()");  
        super.onCreate();  
        Log.d("cjslog", "DigitalAppWidgetService onCreate");
        
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
    	//Log.d(TAG, "DigitalAppWidgetService onStart() ");  

    	notifyWidget(this);  
    } 
    
    
    
    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
    	//Log.d(TAG, "DigitalAppWidgetService onStartCommand() ");
    	Log.d("cjslog", "DigitalAppWidgetService onStartCommand");
    	notifyWidget(this) ;
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
    	//Log.d(TAG, "DigitalAppWidgetService notifyWidget()"); 

    	String[] days = this.getResources().getStringArray(R.array.daysChinese);
    	String[] daysEnglish = this.getResources().getStringArray(R.array.daysEnglish);
    	String[] monthsEnglish = this.getResources().getStringArray(R.array.monthsEnglish);
    	String[] themonthday = this.getResources().getStringArray(R.array.themonthday);;

    	LayoutInflater inflater = LayoutInflater.from(this);
    	LinearLayout poplayout = (LinearLayout) inflater.inflate(
    			R.layout.update_appwidget_in, null, false);

    	RemoteViews remoteViews=new RemoteViews(this.getPackageName(), R.layout.update_appwidget);  
    	Time time=new Time();  
    	time.setToNow();  
    	String month = monthsEnglish[time.month] + " " + time.monthDay;  
    	String weekDay = String.valueOf(time.month + 1) + "月" + time.monthDay + "日  " + days[time.weekDay]; 
    	
    	int xxx = time.monthDay / 10;
        int xx = time.monthDay % 10 - 1;
        if ( xxx == 1 || xx < 0 || xx > 3 ) {
        	xx = 3;
        }
        //Log.d(TAG, "xx = " + xx);

    	//Typeface tf_1 = Typeface.createFromFile("system/fonts/DroidSansFallback.ttf");
    	//Typeface tf = Typeface.createFromFile("system/fonts/Roboto-Thin.ttf");

    	TextView tmpText = (TextView)poplayout.findViewById(R.id.txtWeekDay);
    	tmpText.setTypeface(DeskClockWidgetUtils.auroraGetTitleFontTypeFace());
    	tmpText.setText(daysEnglish[time.weekDay]);

    	tmpText = (TextView)poplayout.findViewById(R.id.txtMonth);
    	tmpText.setText(month);
    	tmpText.setTypeface(DeskClockWidgetUtils.auroraGetTitleFontTypeFace());

    	tmpText = (TextView)poplayout.findViewById(R.id.txtAll);
    	tmpText.setText(weekDay);
    	//tmpText.setTypeface(tf_1);
        if (DeskClockWidgetUtils.getCurrentLocale(Locale.getDefault()) ) {
        	tmpText.setVisibility(View.VISIBLE);
        } else {
        	tmpText.setVisibility(View.GONE);
        }

    	tmpText = (TextView)poplayout.findViewById(R.id.txtth);
    	tmpText.setText(themonthday[xx]);
    	tmpText.setTypeface(DeskClockWidgetUtils.auroraGetTitleFontTypeFace());
    	
    	Bitmap bitmap = makeBitMap( poplayout );

    	remoteViews.setImageViewBitmap(R.id.imageview, bitmap);
    	 Intent intent = new Intent( );   
    	 ComponentName comp = new ComponentName("com.android.calendar", "com.android.calendar.AllInOneActivity");  
    	 intent.setComponent(comp);   
    	
    	  
       // Intent intent=new Intent(context, AlarmClock.class);  
        PendingIntent pendingIntent=PendingIntent.getActivity(context, 0, intent, 0);  
        remoteViews.setOnClickPendingIntent(R.id.imageview, pendingIntent); 

    	//remoteViews.setTextViewText(R.id.txtMonth, month);  
    	//remoteViews.setTextViewText(R.id.txtWeekDay, daysEnglish[time.weekDay]);
    	//remoteViews.setTextViewText(R.id.txtAll, weekDay);

    	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
    	appWidgetManager.updateAppWidget(new ComponentName(this, DigitalAppWidgetProvider.class),
    			remoteViews);

    	bitmap.recycle();
    	System.gc();
    }
 
    @Override  
    public void onDestroy()  
    {  
        //Log.d(TAG, "DigitalAppWidgetService onDestroy()");  
    	Log.d("cjslog", "DigitalAppWidgetService onDestroy");
        unregisterReceiver(mIntentReceiver);  
          
        super.onDestroy();  
    }  
    @Override  
    public IBinder onBind(Intent intent)  
    {  
        //Log.d(TAG, "DigitalAppWidgetService onBind()");  
        return null;  
    }  
}
