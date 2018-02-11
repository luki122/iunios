package com.android.contacts.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.mediatek.contacts.util.TelephonyUtils;

import android.content.Context;
import android.content.Entity;
import android.content.EntityIterator;
import android.content.Entity.NamedContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.RawContacts;
import gionee.provider.GnContactsContract.RawContactsEntity;
import android.text.TextUtils;
import android.util.Log;

public class MergeContacts {
	
	private static final String TAG = "liyang-MergeContacts";
	private final String ENTITY_SORT_ORDER = Data._ID + "," + Data.MIMETYPE + " DESC," + Data.DATA1 + " DESC";
	
	interface SameNameColumns {
		String[] COLUMNS = {
				RawContacts._ID,
				RawContacts.ACCOUNT_NAME,
				RawContacts.ACCOUNT_TYPE,
				RawContacts.DISPLAY_NAME_PRIMARY };
		
		int RAWCONTACT_ID_INDEX = 0;
		int ACCOUNT_NAME_INDEX = 1;
		int ACCOUNT_TYPE_INDEX = 2;
		int DISPLAY_NAME_INDEX = 3;
	}
	
	/**
	 * @author james
	 * 
	 */
	public static class RawContactItem implements Comparable<RawContactItem>, Serializable {
		private static final long serialVersionUID = 4984662856342780286L;

		public int mRawContactId;
		public String mDisplayName;

		public long mPhotoId;
		public String mRawCom;
		public List<String> mNumbers;
		
		//in some particular case, there are records in "raw_contacts table" but "data table"
		private boolean mIsNotNeedToCompare = false;

		public void readRawContact(Cursor c) {
			if (null == c || c.getCount() <= 0)
				return;

			mRawContactId = c.getInt(SameNameColumns.RAWCONTACT_ID_INDEX);
			mDisplayName = c.getString(SameNameColumns.DISPLAY_NAME_INDEX);
			mRawCom = c.getString(SameNameColumns.ACCOUNT_NAME_INDEX) + "_" +
				c.getString(SameNameColumns.ACCOUNT_TYPE_INDEX);
		}

		public void readContactDetail(Entity entity) {
			if (null == entity)
				return;

			clearUncompareDataValues(entity);
			mRawCom = entity.toString();
		}

		protected void clearUncompareDataValues(Entity entity) {
		    
		    if (null == entity)
			return;
		    
		    entity.getEntityValues().clear();
		    ArrayList<NamedContentValues> values = entity.getSubValues();
		    
		    if(null ==values || values.size() <= 0)
			return;
		    
		    ArrayList<Entity.NamedContentValues> tmpValues = new ArrayList<Entity.NamedContentValues>(values.size());
		    for (NamedContentValues v : values) {
		    	String type = v.values.getAsString(Data.MIMETYPE);
		    	if (null == type) {
		    		mIsNotNeedToCompare = true;
		    		continue;
		    	} else if (type.endsWith("name")) {
		    		continue;
		    	} 
//		    	else if (type.endsWith("photo")) {
//		    		mPhotoId = v.values.getAsLong(Data._ID);
//			    	continue;
//		    	} else if (type.endsWith("group_membership")) {
//		    		continue;
//		    	} 
		    	else if (type.endsWith("phone_v2")) {
		    		if (v.values.get(Data.DATA1) != null) {
			    		String formartPhone = v.values.get(Data.DATA1).toString();
			    		if (!TextUtils.isEmpty(formartPhone)) {
				    		formartPhone = formartPhone.replaceAll("-", "");
				    		formartPhone = formartPhone.replaceAll(" ", "");
				    		v.values.put(Data.DATA1, formartPhone);
				    	}
			    	}	
		    	}
		    	v.values.put(Data._ID, 0);
		    	v.values.put(Data.DATA_VERSION, 0);
		    	v.values.put("data_sync4", 0);
		    	v.values.put(Data.IS_PRIMARY, 0);
		    	v.values.put(Data.IS_SUPER_PRIMARY, 0);
		    	tmpValues.add(v);
		    }
			
		    entity.getSubValues().clear();
		    entity.getSubValues().addAll(tmpValues);
		}

		@Override
		public int compareTo(RawContactItem another) {
			Log.d("merge1","mDisplayName:"+mDisplayName+"\nmRawCom:"+mRawCom+"\nanother.mRawCom:"+another.mRawCom);
			boolean b;
			if(null == mRawCom){
				b=null == another.mRawCom;
			}else{
				b= mRawCom.equals(another.mRawCom);
			}
			return (b ? 1 : 0);
		}

		public void setNotNeedToCompare(boolean isNotNeedToCompare) {
			this.mIsNotNeedToCompare = isNotNeedToCompare;
		}

		public boolean isNotNeedToCompare() {
			return mIsNotNeedToCompare;
		}
	}

	public static class LetStopException extends Exception {
		private static final long serialVersionUID = 556585260543984400L;

		public LetStopException() {
			super();
		}
	}

	public static class SameContactItems extends ArrayList<ArrayList<RawContactItem>> {
		private static final long serialVersionUID = -6083195868321852276L;

	}

	public static class CombineItemData {
		public static final int COMBINE_READY = 0;
		public static final int COMBINE_FINISH = 1;
		public int state = COMBINE_READY;
		public ArrayList<RawContactItem> data;
	}

	private boolean mLetStop = false;

	protected void checkRunnable() throws LetStopException {
		if (mLetStop)
			throw new LetStopException();
	}

	public boolean isLetStop() {
		return mLetStop;
	}

	public void reset() {
		mLetStop = false;
	}

	public void stop() {
		mLetStop = true;
	}

	/**
	 * @param sci
	 * @return
	 */
	public SameContactItems fliterRawContactItems(
			ArrayList<RawContactItem> sci) throws LetStopException {
		if (null == sci || sci.size() < 2)
			return null;

		SameContactItems results = new SameContactItems();

		final int size = sci.size();

		boolean[] checkedMarks = new boolean[size];
		Arrays.fill(checkedMarks, false);

		for (int i = 0, si = size - 1; i < si; i++) {
			checkRunnable();

			ArrayList<RawContactItem> ret = new ArrayList<RawContactItem>();
			RawContactItem current = sci.get(i);

			if (checkedMarks[i])
				continue;

			checkedMarks[i] = true;
			
			if (current.isNotNeedToCompare())
				continue;

			for (int j = i + 1; j < size; j++) {
				checkRunnable();

				RawContactItem next = sci.get(j);

				if (!checkedMarks[j] && 1 == current.compareTo(next)) {
					checkedMarks[j] = true;
					ret.add(next);
					next.mRawCom = null;
				}
			}

			if (ret.size() > 0) {
				ret.add(current);
				current.mRawCom = null;
				results.add(ret);
			}
		}

		return results;
	}

	/**
	 * 
	 * @param context
	 * @return
	 */
	public SameContactItems querySameNameRawContact(
			Context context) throws LetStopException {
		
		//Gionee <huangzy> <2013-06-22> modify for CR00828718 begin
		/*String selection = "deleted = 0 AND indicate_phone_or_sim_contact = -1 AND "
				+ "display_name IN "
				+ "(SELECT display_name FROM raw_contacts "
				+ "WHERE deleted = 0 and indicate_phone_or_sim_contact = -1 "
				+ "GROUP BY display_name "
				+ "HAVING COUNT(display_name) > 1)";*/
		String selection;
		//Gionee <huangzy> <2013-06-22> modify for CR00828718 end
		
		if(Build.VERSION.SDK_INT >= 19) {
			selection = "is_privacy = 0 AND deleted = 0 AND account_type='Local Phone Account' AND "
					+ "display_name IN "
					+ "(SELECT display_name FROM raw_contacts left join accounts on (raw_contacts.account_id=accounts._id and accounts.account_type='Local Phone Account')"
					+ "WHERE is_privacy = 0 AND deleted = 0  "
					+ "GROUP BY display_name "
					+ "HAVING COUNT(display_name) > 1)";
		}else{
			selection = "is_privacy = 0 AND deleted = 0 AND account_type='Local Phone Account' AND "
					+ "display_name IN "
					+ "(SELECT display_name FROM raw_contacts "
					+ "WHERE is_privacy = 0 AND deleted = 0 and account_type='Local Phone Account' "
					+ "GROUP BY display_name "
					+ "HAVING COUNT(display_name) > 1)";
		}
		
		String sortOrder =  "sort_key COLLATE NOCASE," 
				+ RawContacts.STARRED + " DESC,"
				+ RawContacts.SEND_TO_VOICEMAIL + " DESC,"
				+ RawContacts.CUSTOM_RINGTONE + " DESC," 
				+ RawContacts.TIMES_CONTACTED + " DESC";

		Uri uri = GnContactsContract.RawContacts.CONTENT_URI;
		Cursor cursor = context.getContentResolver().query(uri, SameNameColumns.COLUMNS, selection, null, sortOrder);

		SameContactItems sameNameAll = null;
		if (null != cursor) {
			try {
				if (cursor.moveToFirst()) {
					sameNameAll = new SameContactItems();
					do {
						checkRunnable();
						RawContactItem item = new RawContactItem();
						item.readRawContact(cursor);
						
						if (null != item.mDisplayName) {
							ArrayList<RawContactItem> sameNamePart = null;
							int size = sameNameAll.size();
							if (size > 0) {
								sameNamePart = sameNameAll.get(size-1);
							}
							
							if (null == sameNamePart ||
									!(item.mDisplayName.equals(sameNamePart.get(0).mDisplayName))) {
								sameNamePart = new ArrayList<RawContactItem>();
								sameNameAll.add(sameNamePart);
							}

							sameNamePart.add(item);
						}

					} while (cursor.moveToNext());
				}
			} catch (LetStopException e) {
				throw e;
			} finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}
			}
		}

		return sameNameAll;
	}
	
	
	
	
	
	
	/**
	 * 
	 * @param context
	 * @return
	 */
	public SameContactItems querySameNumberRawContact(
			Context context) throws LetStopException {
		
		
		
		
		
		String selection ="is_privacy = 0 AND mimetype='vnd.android.cursor.item/phone_v2' AND account_type='Local Phone Account' ";
//		String selection = "is_privacy = 0 AND mimetype='vnd.android.cursor.item/phone_v2' AND account_type='Local Phone Account' AND "
//				+ "data1 IN "
//				+ "(SELECT data1 FROM data "
//				+ "WHERE is_privacy = 0 AND mimetype='vnd.android.cursor.item/phone_v2' AND account_type='Local Phone Account' "
//				+ "GROUP BY data1 "
//				+ "HAVING COUNT(data1) > 1)";
		Uri uri =Uri.parse("content://com.android.contacts/data").buildUpon().appendQueryParameter("merge", "true").build();
		Cursor cursor = context.getContentResolver().query(uri, new String[]{"data1"}, selection, null, null);
		if(cursor!=null&&cursor.getCount()>0){
			StringBuilder sb = new StringBuilder("(");
			while(cursor.moveToNext()){
				sb.append(cursor.getString(0)).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")");
			selection = "is_privacy = 0 AND mimetype='vnd.android.cursor.item/phone_v2' AND account_type='Local Phone Account' AND "
					+ "data1 IN "+ sb.toString();
			cursor.close();
			cursor = null;
		}else{
			if(cursor != null) {
				cursor.close();
				cursor = null;
			}
			return null;
		}
		uri=Uri.parse("content://com.android.contacts/data");
		cursor = context.getContentResolver().query(uri, new String[]{"_id","data1","raw_contact_id"}, selection, null, null);
		Log.i(TAG, System.currentTimeMillis()+"          m");
		Log.i(TAG, "cursor.getCount()="+cursor.getCount());
		Map<String, String> key=new HashMap<String, String>();
		List<Set<String>> lists=new ArrayList<Set<String>>();
		Set<String> set=null;
		while(cursor.moveToNext()){
			Log.i(TAG, "_id="+cursor.getString(0));
			Log.i(TAG,"data1="+cursor.getString(1));
			Log.i(TAG, "raw_contact_id="+cursor.getString(2));
			if(key.get(cursor.getString(1))==null){
				if(set!=null){
					if(set.size()>1){
						lists.add(set);
					}
				}
				set=new HashSet<String>();
				set.add(cursor.getString(2));
				key.put(cursor.getString(1), cursor.getString(2));
			}else{
				set.add(cursor.getString(2));
			}
			
		}
		if(key.size()>0){
			if(set.size()>1){
				lists.add(set);
			}
		}
		if(lists.size()==0){
			return null;
		}
		
		Map<String, Set<String>> map=new HashMap<String, Set<String>>();
		for (int j = 0; j < lists.size(); j++) {
			map.put("set"+(j+1), lists.get(j));
			
		}

		// 存放需要剔除的集合
		Map<String, Set<String>> map2 = new HashMap<String, Set<String>>();

		Set<Entry<String, Set<String>>> entrySet = map.entrySet();
		// 两层自循环比较集合是否有交集
		for (Entry<String, Set<String>> entry : entrySet) {
			// 如果已经剔除 就不需要在比较
			if (map2.get(entry.getKey()) == null) {
				Set<Entry<String, Set<String>>> entrySet2 = map.entrySet();
				for (Entry<String, Set<String>> entry2 : entrySet2) {
					// 如果key相等表示是同一个集合 不需要比较
					if (!entry.getKey().equals(entry2.getKey())) {
						boolean exist = isExist(entry.getValue(),
								entry2.getValue());
						// 如果存在就讲set2添加到set1中
						if (exist) {
							entry.getValue().addAll(entry2.getValue());
							map2.put(entry2.getKey(), entry2.getValue());
							// map2.put("entry.getKey()", entry.getValue());
						}

					}
				}
			}

		}

		// 删除map中剔除的结果集就是最终合并的结果
		for (Entry<String, Set<String>> entry : map2.entrySet()) {
			map.remove(entry.getKey());
		}
        List<String> result=new ArrayList<String>();
		// 输出最终结果
		for (Entry<String, Set<String>> entry : map.entrySet()) {
			System.out.println(entry.getValue());
			result.add(entry.getValue().toString().replace("[", "(").replace("]", ")"));
		}
		
		if(cursor!=null){
			cursor.close();
		}
		cursor=null;
		SameContactItems sameNameAll = new SameContactItems();
		ArrayList<RawContactItem> sameNamePart = null;
		for (int j = 0; j < result.size(); j++) {
			cursor = context.getContentResolver().query(GnContactsContract.RawContacts.CONTENT_URI, SameNameColumns.COLUMNS, "_id in "+result.get(j), null, null);
			if (null != cursor) {
				sameNamePart = new ArrayList<RawContactItem>();
				try {
					if (cursor.moveToFirst()) {
						do {
							checkRunnable();
							RawContactItem item = new RawContactItem();
							item.readRawContact(cursor);
							sameNamePart.add(item);
						} while (cursor.moveToNext());
						sameNameAll.add(sameNamePart);
					}
				} catch (LetStopException e) {
					throw e;
				} finally {
					if(cursor != null){
						cursor.close();
						cursor = null;
					}
				}
			}
		}
		
		return sameNameAll;
	
	}
	
	
	
	
	
	
	private static boolean isExist(Set<String> set1,Set<String> set2){
		boolean bo = false;
		Iterator<String> iterator = set2.iterator();
		while(iterator.hasNext()){
			String value = iterator.next();
			//如果存在就跳出循环
			if(set1.contains(value)){
				bo=true;
				break;
			}
		}
		return bo;
	}
	
	
	
	
	
	
	
	
	

	/**
	 * 
	 * @param context
	 * @return
	 */
	public SameContactItems querySameRawContact(Context context)
			throws LetStopException {
		
		SameContactItems sameNameAll = querySameNameRawContact(context);
		if (null == sameNameAll || sameNameAll.size() <= 0)
			return null;

		SameContactItems sameRawAll = new SameContactItems();
		
		for (ArrayList<RawContactItem> entry : sameNameAll) {
			checkRunnable();
			SameContactItems sameRawPart = fliterRawContactItems(entry);

			if (null == sameRawPart || sameRawPart.size() <= 0)
				continue;

			sameRawAll.addAll(sameRawPart);
		}

		return sameRawAll;
	}

	/**
	 * 
	 * @param context
	 * @return
	 */
	public SameContactItems querySameContact(Context context,
			Handler publishProgress) throws LetStopException {
		SameContactItems sameRawAll = querySameRawContact(context);
		if (null == sameRawAll || sameRawAll.size() <= 0) {
			return null;
		}
		
		return querySameContact(context, sameRawAll, publishProgress);
	}
	public SameContactItems querySameContact(Context context, SameContactItems sameRawAll,
			Handler publishProgress) throws LetStopException {
		
		

		int finishedCount = 0;
		sendMessage(publishProgress, 0, sameRawAll.size());

		SameContactItems queryGroups = new SameContactItems();
		for (int i = 0, size = sameRawAll.size(); i < size;) {
			ArrayList<RawContactItem> qg = new ArrayList<RawContactItem>();
			
			int count = 0;			
			while (count < 80 && i < size) {
				checkRunnable();
				ArrayList<RawContactItem> items = sameRawAll.get(i);
				if (null != items) {
					count += items.size();
					qg.addAll(items);
				}
				i++;
			}
			queryGroups.add(qg);
		}

		SameContactItems sameContactAll = new SameContactItems();
		Map<Integer, Entity> entityCache = new HashMap<Integer, Entity>();
		int ensureIndex = 0;
		for (ArrayList<RawContactItem> sameRaws : sameRawAll) {
			if (null == sameRaws || sameRaws.size() <= 0) {
				continue;
			}

			if (null == entityCache.get(sameRaws.get(0).mRawContactId)) {
				ArrayList<RawContactItem> aGroup = queryGroups.get(ensureIndex);
				++ensureIndex;
				
				StringBuilder sb = new StringBuilder(Data._ID + " IN (");
				for (RawContactItem rci : aGroup) {
					if (null != rci) {
						sb.append(rci.mRawContactId).append(",");	
					}
				}
				sb.setLength(sb.length() - 1);
				sb.append(")");

				String selection = sb.toString();
				
				//add by liguangyu for 16685 start
				TelephonyUtils.sleepInCall();
				//add by liguangyu for 16685  end

				Cursor cursor = context.getContentResolver().query(
						RawContactsEntity.CONTENT_URI, null, selection,
						null, ENTITY_SORT_ORDER);

				if (null != cursor) {
					if (cursor.moveToFirst()) {
						EntityIterator iterator = RawContacts.newEntityIterator(cursor);

						while (iterator.hasNext()) {
							checkRunnable();
							Entity e = iterator.next();
							int id = e.getEntityValues().getAsInteger(
									RawContacts._ID);
							entityCache.put(id, e);
						}							
					}
					cursor.close();
				}
			}

			for (RawContactItem sameRaw : sameRaws) {
				checkRunnable();
				Entity entity = entityCache.remove(sameRaw.mRawContactId);
				sameRaw.readContactDetail(entity);
			}

			SameContactItems sameContactPart = fliterRawContactItems(sameRaws);

			if (null != sameContactPart && sameContactPart.size() > 0) {
				sameContactAll.addAll(sameContactPart);	
			}

			sendMessage(publishProgress, 1, ++finishedCount);
		}

		return sameContactAll;
	}
	
	private void sendMessage(Handler handler, int what, int arg1) {
		if (null != handler) {
			Message msg = Message.obtain();
			msg.what = what;
			msg.arg1 = arg1;
			handler.sendMessage(msg);
		}
	}

	public List<RawContactItem> fliterNeedToDelete(SameContactItems same) {
		if (null == same || same.size() <= 0)
			return null;

		List<RawContactItem> needToDelete = new ArrayList<RawContactItem>(
				(same.size() * 2));

		for (ArrayList<RawContactItem> item : same) {

			for (int i = 1, s = item.size(); i < s; i++) {
				needToDelete.add(item.get(i));
			}
		}

		return needToDelete;
	}

	/**
	 * 
	 * @param context
	 * @param same
	 * @param publishProgress can be null
	 * 	msg.what:msg:arg1 {0:TotalSize, 1:deletedSize, 2:1(finished), 2:2(stopped)}
	 * 			
	 * @return
	 * @throws LetStopException
	 */
	public int mergeContacts(Context context, SameContactItems same,
			Handler publishProgress){
		if (null == same || same.size() <= 0)
			return 0;

		int deletedCount = 0;
		boolean isLetStop = false;

		List<RawContactItem> needToDelete = fliterNeedToDelete(same);
		
		if(null == needToDelete)
			return 0;		
		int size = needToDelete.size();
		if(size <= 0)
			return 0;

		if (null != publishProgress) {
			Message msg = Message.obtain();
			msg.what = 0;
			msg.arg1 = needToDelete.size();
			publishProgress.sendMessage(msg);
		}

		final int maxDeleteOnce = 80;
		try {
			for (int i = 0; i < size;) {			
					checkRunnable();			
				StringBuilder sb = new StringBuilder("_id in (");
				for (int j = 0; j < maxDeleteOnce && i < size; j++, i++) {
					checkRunnable();
					sb.append(needToDelete.get(i).mRawContactId).append(",");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append(")");
				String where = sb.toString();
				deletedCount = context.getContentResolver().delete(
						RawContacts.CONTENT_URI, where, null);
	
				if (null != publishProgress) {
					Message msg = Message.obtain();
					msg.what = 1;
					msg.arg1 = deletedCount;
					publishProgress.sendMessage(msg);
				}
			}
		} catch (LetStopException e) {
			isLetStop = true;
			e.printStackTrace();
		} finally {
			if (null != publishProgress) {
				Message msg = Message.obtain();
				msg.what = 2;
				msg.arg1 = isLetStop ? 0 : 1;
				publishProgress.sendMessage(msg);
			}
		}

		return deletedCount;
	}
}