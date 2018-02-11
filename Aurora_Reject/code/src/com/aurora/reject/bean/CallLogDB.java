package com.aurora.reject.bean;

import gionee.provider.GnTelephony.SIMInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog.Calls;
public class CallLogDB {
	private static Uri uri = Uri
			.parse("content://com.android.contacts/black");
	public static List<CallLogEntity> queryCallLogs(Cursor cursor,Context context) {
		Map<String, CallLogEntity> temMap = new HashMap<String, CallLogEntity>();
		String number;
		long id;
		int type;
		long date;
		long duration;
		String area;
		int reject;
		String name;
		String lable;
		int simId;
		CallLogEntity callLogEntity = null;
		CallLogItem callLogItem = null;
		do {

			id = cursor.getLong(cursor.getColumnIndex("_id"));
			number = cursor.getString(cursor.getColumnIndex("number")).replace("-","").replace(" ","");
			if(number.startsWith("+86")){
				number=number.substring(3);
			}
			area = cursor.getString(cursor.getColumnIndex("area"));
			date = cursor.getLong(cursor.getColumnIndex("date"));
			type = cursor.getInt(cursor.getColumnIndex("type"));
			duration = cursor.getLong(cursor.getColumnIndex("duration"));
			reject=cursor.getInt(cursor.getColumnIndex("reject"));
			simId=cursor.getInt(cursor.getColumnIndex("simid"));
			SIMInfo info=SIMInfo.getSIMInfoById(context, simId);
			if(info!=null){
				simId=info.mSlot;
			}
			callLogEntity = temMap.get(number);
			if (callLogEntity == null) {
				callLogEntity = new CallLogEntity();
				name = getBlackNameByPhoneNumber(context, number);
				if (name == null) {
					name = getBlackNameByPhoneNumbers(context, number);
				}
				if(name==null){
					callLogEntity.setName("");
				}else{
					callLogEntity.setName(name);
				}
				lable=getLableByPhoneNumber(context, number);
	        	if(lable==null){
	        		lable=getLableByPhoneNumbers(context, number);
	        	}
	        	if(lable==null){
	        		callLogEntity.setLabel("");
	        	}else{
	        		callLogEntity.setLabel(lable);
	        	}
	        	callLogEntity.setSimId(simId);
				callLogEntity.setLastCallDate(date);
				callLogEntity.setDBPhomeNumber(number);
				callLogEntity.setArea(area);
				callLogEntity.setReject(reject);
				temMap.put(number, callLogEntity);
			}
			callLogItem = new CallLogItem();
			callLogItem.setId(id);
			callLogItem.setmType(type);
			callLogItem.setCallTime(date);
			callLogItem.setDuratation(duration);
			callLogEntity.addCallLogItem(callLogItem);

		} while (cursor.moveToNext());
		cursor.close();
		List<CallLogEntity> callLogEntities = new ArrayList<CallLogEntity>(
				temMap.values());
		Collections.sort(callLogEntities);
		return callLogEntities;
	}
	
	
	
	public static String getBlackNameByPhoneNumber(Context context,String address){
		Cursor cursor = context.getContentResolver().query(uri, null, "number='"+address+"'", null, null);
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
	
	public static String getBlackNameByPhoneNumbers(Context context,
			String address) {

		Cursor cursor = context.getContentResolver().query(Calls.CONTENT_URI,
				null, "number='" + address + "'" + " and reject=1", null, null);
		if (cursor != null) {
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToPosition(i);
				String name = cursor.getString(cursor
						.getColumnIndex("black_name"));
				cursor.close();
				return name;
			}
			cursor.close();
		}

		return null;
	}
	
	
	public static String getLableByPhoneNumber(Context context, String address) {
		Cursor cursor = context.getContentResolver().query(uri, null,
				"number='" + address + "'", null, null);
		if (cursor != null) {
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToPosition(i);
				String lable = cursor.getString(cursor.getColumnIndex("lable"));
				cursor.close();
				return lable;
			}
			cursor.close();
		}

		return null;
	}
	
	public static String getLableByPhoneNumbers(Context context,String address){
		Cursor cursor = context.getContentResolver().query(Calls.CONTENT_URI, null, "number='"+address+"'"+" and reject=1", null, null);
		if(cursor!=null){
			for(int i = 0; i < cursor.getCount(); i++){
				cursor.moveToPosition(i);
				String lable = cursor.getString(cursor.getColumnIndex("mark"));
				cursor.close();
				return lable;
			}
			cursor.close();
		}
		
		return null;
	}

}
