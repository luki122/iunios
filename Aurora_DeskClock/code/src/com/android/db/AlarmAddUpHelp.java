package com.android.db;

import com.android.deskclock.Log;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class AlarmAddUpHelp {

	private static final String MODULE_KEY_VALUE="160";
	
	private static final String MODULE_KEY="module_key";
	private static final String ITEM_TAG="item_tag";
	private static final String VALUE="value";
	
	private static AlarmAddUpHelp instance;
	private ContentResolver contentResolver;
	
	private static final String ITEM_TAG_ADD_ALARM="addalarm";//新建闹钟记录一次
	private static final String ITEM_TAG_ONCE="once";//新建一个闹钟，并选中重复为一次性记录一次
	private static final String ITEM_TAG_EVERYDAY="everyday";//新建一个闹钟，并选择重复为每天记录一次
	private static final String ITEM_TAG_WEEKDAY="weekday";//新建一个闹钟，并选择重复为工作日，记录一次
	private static final String ITEM_TAG_WORKDAY="workday";//新建一个闹钟，并选择重复为法定工作日，记录一次
	private static final String ITEM_TAG_CUSTOM="custom";//新建一个闹钟，并选择重复为自定义，记录一次
	private static final String ITEM_TAG_WCLOCK="wclock";//添加一个世界时钟，记录一次
	private static final String ITEM_TAG_STOP_WATCH="stopwatch";//启用一次秒表，记录一次
	private static final String ITEM_TAG_TIMER="timer";//启用一次计时器，记录一次
	
	private static final String[] ITEM_TAGS={ITEM_TAG_ADD_ALARM,ITEM_TAG_ONCE,ITEM_TAG_EVERYDAY,ITEM_TAG_WEEKDAY,
		ITEM_TAG_WORKDAY,ITEM_TAG_CUSTOM,ITEM_TAG_WCLOCK,ITEM_TAG_STOP_WATCH,ITEM_TAG_TIMER};
	
	public static Uri BASE_URI=Uri.parse("content://com.iuni.reporter/module/");
	private Context context;
	
	private AlarmAddUpHelp(Context context){
		this.context=context;
		contentResolver=context.getContentResolver();
	}
	
	public static AlarmAddUpHelp getInstance(Context context){
		if(instance==null)
		{
			instance=new AlarmAddUpHelp(context);
		}
		return instance;
	}
	
	public void add(AlarmAddUpType type){
		new TotalCount(context, MODULE_KEY_VALUE, ITEM_TAGS[type.ordinal()], 1).CountData();
	}
	
	public static enum AlarmAddUpType{
		ITEM_TAG_ADD_ALARM,ITEM_TAG_ONCE,ITEM_TAG_EVERYDAY,ITEM_TAG_WEEKDAY,ITEM_TAG_WORKDAY,ITEM_TAG_CUSTOM,ITEM_TAG_WCLOCK,ITEM_TAG_STOP_WATCH,ITEM_TAG_TIMER;
	}
	
}
