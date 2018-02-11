package com.aurora.alarmclock;


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
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

public class AnalogClockAppWidgetProvider extends AppWidgetProvider{  

    @Override  
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,  
            int[] appWidgetIds) {  
        // TODO Auto-generated method stub
          
        RemoteViews remoteViews=new RemoteViews(context.getPackageName(), R.layout.update_appwidget_analogclock);  
        
        Intent intent=new Intent(context, AlarmClock.class);  
        PendingIntent pendingIntent=PendingIntent.getActivity(context, 0, intent, 0);  
        remoteViews.setOnClickPendingIntent(R.id.analog_appwidget, pendingIntent);  
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);  
          
        super.onUpdate(context, appWidgetManager, appWidgetIds);  
    }

    @Override  
    public void onReceive(Context context, Intent intent) {  
        // TODO Auto-generated method stub  
    	
    	super.onReceive(context, intent);
    }  
    
    
    /**
     * 第一个Widget组件启动时触发
     */ 
    public void onEnabled(Context context){ 
          //Log.i("222222", "onEnabled"); 
    } 
    
    /**
     * 最后一个Widget组件关闭时触发
     */ 
    public void onDisabled(Context context){ 
          //Log.i("222222", "onDisabled"); 
    } 
    
    /**
     * 任一Widget组件被删除时触发
     */ 
    public void onDeleted(Context context, int[] appWidgetIds){ 
          //Log.i("222222", "onDeleted"); 
    } 
    
}  
