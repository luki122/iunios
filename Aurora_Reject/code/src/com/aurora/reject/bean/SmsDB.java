package com.aurora.reject.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog.Calls;


public class SmsDB {
	private static Uri uri = Uri
			.parse("content://com.android.contacts/black");

	public static List<SmsEntity> querySms(Cursor cursor,List<MmsItem> list,Context mContext) {
		Map<String, Long> dates = new HashMap<String, Long>();
		Map<String, SmsEntity> temMap = new HashMap<String, SmsEntity>();
		String number;
		long id;
		int type;
		long date=0;
		String body;
		long thread_id;
		String name=null;
		SmsEntity smsEntity = null;
		SmsItem smsItem = null;
		if(cursor.getCount()>0){
			do {
				id = cursor.getLong(cursor.getColumnIndex("_id"));
				thread_id=cursor.getLong(cursor.getColumnIndex("thread_id"));
				number = cursor.getString(cursor.getColumnIndex("address")).replace("-","").replace(" ","");
				if(number.startsWith("+86")){
					number=number.substring(3);
				}
				date = cursor.getLong(cursor.getColumnIndex("date"));
				type = cursor.getInt(cursor.getColumnIndex("type"));
				body=cursor.getString(cursor.getColumnIndex("body"));
				smsEntity = temMap.get(number);
				if (smsEntity == null) {
					smsEntity = new SmsEntity();
					name=getBlackNameByPhoneNumber(mContext, number);
					if(name==null||"".equals(name)){
		        		name=getBlackNameByPhoneNumbers(mContext, number);
		        	}
					if(name==null){
						smsEntity.setName("");
					}else{
						smsEntity.setName(name);
					}
					smsEntity.setLastDate(date);
					dates.put(number, date);
					smsEntity.setDBPhomeNumber(number);
					smsEntity.setBody(body);
					smsEntity.setThread_id(thread_id);
					smsEntity.setIsMms(0);
					temMap.put(number, smsEntity);
				}
				smsItem = new SmsItem();
				smsItem.setId(id);
				smsItem.setType(type);
				smsItem.setDate(date);
				smsEntity.addSmsItem(smsItem);

			} while (cursor.moveToNext());
		}
		if(cursor != null) {
			cursor.close();
		}
	
		
		if(list!=null){
			for(int i=0;i<list.size();i++){
				id = list.get(i).getId();
				thread_id=list.get(i).getThread_id();
				Cursor cursors=mContext.getContentResolver().query(Uri.parse("content://mms-sms/canonical-addresses"), null, "_id in (select recipient_ids from threads where _id ="+ thread_id+")", null, null);
				if(!cursors.moveToFirst()){
					if(cursors!=null){
						cursors.close();
					}
					continue;
				}
				number=cursors.getString(cursors.getColumnIndex("address")).replace("-","").replace(" ","");
				if(cursors!=null){
					cursors.close();
				}
				if(number.startsWith("+86")){
					number=number.substring(3);
				}
				date = list.get(i).getDate()*1000;
				type = list.get(i).getType();
				body=list.get(i).getBody();
				smsEntity = temMap.get(number);
				if (smsEntity == null) {
					smsEntity = new SmsEntity();
					name=getBlackNameByPhoneNumber(mContext, number);
					if(name==null||"".equals(name)){
		        		name=getBlackNameByPhoneNumbers(mContext, number);
		        	}
					if(name==null){
						smsEntity.setName("");
					}else{
						smsEntity.setName(name);
					}
					smsEntity.setLastDate(date);
					smsEntity.setDBPhomeNumber(number);
					smsEntity.setBody(body);
					smsEntity.setThread_id(thread_id);
					smsEntity.setIsMms(1);
					temMap.put(number, smsEntity);
				}else{
					if(dates.get(number)!=null){
						if(date>dates.get(number)){
							SmsEntity smsEntitys=  temMap.get(number);
							smsEntitys.setLastDate(date);
							smsEntitys.setDBPhomeNumber(number);
							smsEntitys.setBody(body);
							smsEntitys.setThread_id(thread_id);
							smsEntitys.setIsMms(1);
							temMap.put(number, smsEntitys);
						}
					}
					
				}
				smsItem = new SmsItem();
				smsItem.setId(id);
				smsItem.setType(type);
				smsItem.setDate(date);
				smsEntity.addSmsItem(smsItem);
			}
		}
		List<SmsEntity> smsEntitys = new ArrayList<SmsEntity>(
				temMap.values());
		Collections.sort(smsEntitys);
		return smsEntitys;
	}
	
	
	
	
	public static String getBlackNameByPhoneNumber(Context context,String address){
		Cursor cursor=null;
		if(address.length()>10){
			cursor= context.getContentResolver().query(uri, null, "number LIKE '%"+address+"'", null, null);
		}else{
			cursor = context.getContentResolver().query(uri, null, "number='"+address+"'", null, null);
		}
		
		if(cursor!=null){
			for(int i = 0; i < cursor.getCount(); i++){
				cursor.moveToPosition(i);
				String name = cursor.getString(cursor.getColumnIndex("black_name"));
				cursor.close();
				return name;
			}
			cursor.close();
		}
		
		return null;
	}
	public static String getBlackNameByPhoneNumbers(Context context,String address){
		Cursor cursor=null;
		if(address.length()>10){
			cursor = context.getContentResolver().query(Calls.CONTENT_URI, null, "number LIKE '%"+address+"'"+" and reject=1", null, null);
		}else{
			cursor = context.getContentResolver().query(Calls.CONTENT_URI, null, "number='"+address+"'"+" and reject=1", null, null);
		}
		if(cursor!=null){
			for(int i = 0; i < cursor.getCount(); i++){
				cursor.moveToPosition(i);
				String name = cursor.getString(cursor.getColumnIndex("black_name"));
				cursor.close();
				return name;
			}
			cursor.close();
		}
		return null;
	}
}
