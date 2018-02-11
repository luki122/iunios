package com.aurora.alarmclock;


import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.android.deskclock.AlarmClock;
import com.android.deskclock.R;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

public class DigitalAppWidgetProvider extends AppWidgetProvider{  
  
	
    private String[] days;
    private String[] daysEnglish;
    private String[] monthsEnglish;
    private String[] themonthday;
    @Override  
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,  
            int[] appWidgetIds) {  
    	
    	
    	//Log.e("222222", "---onUpdate");
    	
    	days = context.getResources().getStringArray(R.array.daysChinese);
    	daysEnglish = context.getResources().getStringArray(R.array.daysEnglish);
    	monthsEnglish = context.getResources().getStringArray(R.array.monthsEnglish);
    	themonthday = context.getResources().getStringArray(R.array.themonthday);
    	
    	LayoutInflater inflater = LayoutInflater.from(context);
		LinearLayout poplayout = (LinearLayout) inflater.inflate(
				R.layout.update_appwidget_in, null, false);
          
        RemoteViews remoteViews=new RemoteViews(context.getPackageName(), R.layout.update_appwidget);  
        Time time=new Time();  
        time.setToNow();  
        String month = monthsEnglish[time.month] + " " + time.monthDay;  
        String weekDay = String.valueOf(time.month + 1) + "月" + time.monthDay + "日  " + days[time.weekDay];

        int xxx = time.monthDay / 10;
        int xx = time.monthDay % 10 - 1;
        if ( xxx == 1 || xx < 0 || xx > 3 ) {
        	xx = 3;
        }

       
     
       
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
    
        PendingIntent pendingIntent=PendingIntent.getActivity(context, 0, intent, 0);  
        remoteViews.setOnClickPendingIntent(R.id.imageview, pendingIntent);  
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);  
        
        bitmap.recycle();
        System.gc();
        super.onUpdate(context, appWidgetManager, appWidgetIds);       
       
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
    
      
    @Override  
    public void onReceive(Context context, Intent intent) {  
        // TODO Auto-generated method stub  
    	
    	Log.e("xxj", "---onReceive = " + intent.getAction());
    	  /*context.startService(new Intent("com.aurora.UPDATE_SERVICE"));*/
    	//cjs modify in order to resolve bug 17548
    	Intent i = new Intent();
    	i.setAction("com.aurora.UPDATE_SERVICE");
    	i.setPackage(context.getPackageName());
    	context.startService(i);
    	//cjs modify end
    	super.onReceive(context, intent);
    }  
    
    
    /**
     * 第一个Widget组件启动时触发
     */ 
    public void onEnabled(Context context){ 
          //Log.i("222222", "onEnabled"); 
          
         /*context.startService(new Intent("com.aurora.UPDATE_SERVICE"));*/
    	//cjs modify in order to resolve bug 17548
    	Intent i = new Intent();
    	i.setAction("com.aurora.UPDATE_SERVICE");
    	i.setPackage(context.getPackageName());
    	context.startService(i);
    	//cjs modify end
    } 
    
    /**
     * 最后一个Widget组件关闭时触发
     */ 
    public void onDisabled(Context context){ 
          //Log.i("222222", "onDisabled"); 
          
         /*context.stopService(new Intent("com.aurora.UPDATE_SERVICE"));*/
    	//cjs modify in order to resolve bug 17548
    	Intent i = new Intent();
    	i.setAction("com.aurora.UPDATE_SERVICE");
    	i.setPackage(context.getPackageName());
    	context.stopService(i);
    	//cjs modify end 
    } 
    
    /**
     * 任一Widget组件被删除时触发
     */ 
    public void onDeleted(Context context, int[] appWidgetIds){ 
          //Log.i("222222", "onDeleted"); 
    }
    
}  