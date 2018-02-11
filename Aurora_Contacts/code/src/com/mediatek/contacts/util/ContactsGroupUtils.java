package com.mediatek.contacts.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContentValues;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.IIccPhoneBook;
import com.android.internal.telephony.ITelephony;
import com.mediatek.contacts.simcontact.AbstractStartSIMService;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.simcontact.AbstractStartSIMService.ServiceWorkData;
//import com.mediatek.featureoption.FeatureOption;
import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import gionee.provider.GnContactsContract;
import android.provider.Settings;
import gionee.provider.GnContactsContract.CommonDataKinds;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.Groups;
import gionee.provider.GnContactsContract.RawContacts;
import gionee.provider.GnTelephony.SIMInfo;
import gionee.provider.GnContactsContract.CommonDataKinds.Email;
import gionee.provider.GnContactsContract.CommonDataKinds.GroupMembership;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import gionee.provider.GnContactsContract.CommonDataKinds.StructuredName;

import com.android.internal.telephony.gsm.UsimPhoneBookManager;
import com.android.contacts.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.android.internal.telephony.UsimGroup;
import gionee.telephony.GnTelephonyManager;
import com.gionee.internal.telephony.GnITelephony;
import com.gionee.internal.telephony.GnIIccPhoneBook;
import com.android.contacts.AuroraCardTypeUtils;

public class ContactsGroupUtils {

	public static final String TAG = "ContactsGroupUtils";
	private static final boolean DBG = true;
	
	public static final String SIMPHONEBOOK_SERVICE = "simphonebook";
	public static final String SIMPHONEBOOK2_SERVICE = "simphonebook2";
	public static final int SINGLE_SLOT = 0;
	public static final int GEMINI_SLOT1 = ContactsFeatureConstants.GEMINI_SIM_1;
	public static final int GEMINI_SLOT2 = ContactsFeatureConstants.GEMINI_SIM_2;
	public static final int SLOT_COUNT = FeatureOption.MTK_GEMINI_SUPPORT?2:1;
	private static int UGRP_SLOT_LOCK = 0;
	public static int ArrayData;
	public static final boolean DEBUG = true;
    public static final String CONTACTS_IN_GROUP_SELECT =
    " IN "
            + "(SELECT " + RawContacts.CONTACT_ID
            + " FROM " + "raw_contacts"
            + " WHERE " + "raw_contacts._id" + " IN "
                    + "(SELECT " + "data."+Data.RAW_CONTACT_ID
                    + " FROM " + "data "
                    + "JOIN mimetypes ON (data.mimetype_id = mimetypes._id)"
                    + " WHERE " + Data.MIMETYPE + "='" + GroupMembership.CONTENT_ITEM_TYPE
                            + "' AND " + GroupMembership.GROUP_ROW_ID + "="
                            + "(SELECT " + "groups" + "." + Groups._ID
                            + " FROM " + "groups"
                            + " WHERE " + Groups.DELETED + "=0 AND " + Groups.TITLE + "=?))" 
             + " AND "+ RawContacts.DELETED +"=0)";
	
	public static IIccPhoneBook getIIccPhoneBook(int slotId) {
		logd(TAG,"[getIIccPhoneBook]slotId:" + slotId);
		String serviceName;
		if (FeatureOption.MTK_GEMINI_SUPPORT) {
			serviceName = (slotId == GEMINI_SLOT2) ? SIMPHONEBOOK2_SERVICE : SIMPHONEBOOK_SERVICE;
		} else {
			serviceName = SIMPHONEBOOK_SERVICE;
		}
		final IIccPhoneBook iIccPhb = IIccPhoneBook.Stub
				.asInterface(ServiceManager.getService(serviceName));
		return iIccPhb;
	}
	

	public static final class USIMGroup {
		public static final String TAG = ContactsGroupUtils.TAG;
		
		public static final String SIM_TYPE_USIM = "USIM";
		
		//A inner class USED for Usim group cache.
		private static final class USimGroupArray {
			private ArrayList<ArrayList<UsimGroup>> mUgrpArray;
			private int mSize = 0;
			USimGroupArray(int size) {
				mUgrpArray = new ArrayList<ArrayList<UsimGroup>>();
				for(int i=0;i<size;i++) {
					ArrayList<UsimGroup> ugrpList = new ArrayList<UsimGroup>();
					mUgrpArray.add(ugrpList);
				}
				mSize = size;
			}
			int size() {
				return mSize;
			}
			
			ArrayList<UsimGroup> get(int slot) {
				if (slot<0 || slot>=mSize) {
					return null;
				}
				return mUgrpArray.get(slot);
			}
			
			void clear() {
				for(int i=0;i<mSize;i++) {
					mUgrpArray.get(i).clear();
				}
			}
			
			boolean addItem(int slot, UsimGroup usimGroup) {
				if (slot<0 || slot>=mSize) {
					return false;
				}
				return mUgrpArray.get(slot).add(usimGroup);
			}
			
			boolean removeItem(int slot, int usimGroupId) {
				if (slot<0 || slot>=mSize) {
					return false;
				}
				ArrayList<UsimGroup> ugrpList = mUgrpArray.get(slot);
				int i=0;
				for(UsimGroup ug: ugrpList) {
					Log.i(TAG, "ug---index:" + ug.getRecordIndex() + " || name:" + ug.getAlphaTag());
					if (ug.getRecordIndex() == usimGroupId)
						break;
					Log.i(TAG, "ug---i count:" + i);
					i++;
				}
				Log.i(TAG, "ug---size:" + ugrpList.size());
				if (i<ugrpList.size()) {
					ugrpList.remove(i);
					Log.i(TAG, "ug---size after remove:" + ugrpList.size());
					return true;
				} else
					return false;
			}
			
			UsimGroup getItem(int slot, int usimGroupId) {
				if (slot<0 || slot>=mSize) {
					return null;
				}
				ArrayList<UsimGroup> ugrpList = mUgrpArray.get(slot);
				int i=0;
				for(UsimGroup ug: ugrpList) {
					if (ug.getRecordIndex() == usimGroupId)
						return ug;
				}
				return null;
			}

		}
		// Framework interface, here should be change in future.
		//These values may be changed when booting phone to get the really usim capability.
		private static int[] MAX_USIM_GROUP_NAME_LENGTH = {-1, -1};
		private static int[] MAX_USIM_GROUP_COUNT = {-1, -1};
		private static final USimGroupArray ugrpListArray = new USimGroupArray(SLOT_COUNT);
		
		// Framework interface, here should be change in future.
		public static int hasExistGroup(int slotId, String grpName) throws RemoteException {
			int grpId = -1;
			final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
			logd(TAG, "grpName:" + grpName + "|iIccPhb:" + iIccPhb);
			if (TextUtils.isEmpty(grpName) || iIccPhb == null) {
				return grpId;
			}
			ArrayList<UsimGroup> ugrpList = ugrpListArray.get(slotId);
			logd(TAG, "[hasExistGroup]ugrpList---size:" + ugrpList.size());
			if (ugrpList.isEmpty()) {
				List<UsimGroup> uList = GnIIccPhoneBook.getUsimGroups(iIccPhb);
				for(UsimGroup ug: uList) {
					String gName = ug.getAlphaTag();
					int gIndex = ug.getRecordIndex();
					if (!TextUtils.isEmpty(gName) && gIndex > 0) {
						ugrpList.add(new UsimGroup(gIndex, gName));
						logd(TAG, "[hasExistGroup]gName:" + gName + "||gIndex:" + gIndex);
						if (gName.equals(grpName)) {
							grpId = gIndex;
						}
					}
				}
			} else {
				for(UsimGroup ug: ugrpList) {
					logd(TAG, "[hasExistGroup]ug---index:" + ug.getRecordIndex() + " || name:" + ug.getAlphaTag());
					if (grpName.equals(ug.getAlphaTag())) {
						grpId = ug.getRecordIndex();
						break;
					}
				}
			}
			logd(TAG,"ugrpList size:" + ugrpList.size());
			return grpId;
		}
		
		public static int syncUSIMGroupNewIfMissing(int slotId, String name) 
				throws RemoteException, USIMGroupException {
			int nameLen = 0;
			logd(TAG,"[syncUSIMGroupNewIfMissing]name:" + name);
			if (TextUtils.isEmpty(name)) {
				return -1;
			}
			try {
				nameLen = name.getBytes("GBK").length;
			} catch (java.io.UnsupportedEncodingException e) {
				nameLen = name.length();
			}
			logd(TAG,"[syncUSIMGroupNewIfMissing]nameLen:" + nameLen + " ||getUSIMGrpMaxNameLen(slotId):" + getUSIMGrpMaxNameLen(slotId));
			if (nameLen > getUSIMGrpMaxNameLen(slotId))
				throw new USIMGroupException(
						USIMGroupException.ERROR_STR_GRP_NAME_OUTOFBOUND,
						USIMGroupException.GROUP_NAME_OUT_OF_BOUND, slotId);
			final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
			int grpId = -1;
			if ((grpId = hasExistGroup(slotId, name)) < 1 && iIccPhb != null) {
				grpId = GnIIccPhoneBook.insertUSIMGroup(iIccPhb, name);
				Log.i(TAG, "[syncUSIMGroupNewIfMissing]inserted grpId:" +grpId);
				if (grpId > 0)
					ugrpListArray.addItem(slotId, new UsimGroup(grpId, name));
			}
			logd(TAG,"[syncUSIMGroupNewIfMissing]grpId:" + grpId);
			if (grpId < 1) {
				switch (grpId) {
				case USIMGroupException.USIM_ERROR_GROUP_COUNT: {
					throw new USIMGroupException(
							USIMGroupException.ERROR_STR_GRP_COUNT_OUTOFBOUND,
							USIMGroupException.GROUP_NUMBER_OUT_OF_BOUND, slotId);
				}
				//Name len has been check before new group.
				//However, do protect here just for full logic.
				case USIMGroupException.USIM_ERROR_NAME_LEN: {
					throw new USIMGroupException(
							USIMGroupException.ERROR_STR_GRP_NAME_OUTOFBOUND,
							USIMGroupException.GROUP_NAME_OUT_OF_BOUND, slotId);
				}
				default:
					break;
				}
			}
			return grpId;
		}
		
		/**
		 * If a USIM group is created, it should indicate which USIM it 
		 * creates on.
		 *  
		 * @param slotId
		 * @param name
		 * @return
		 */
		public static int createUSIMGroup (int slotId, String name) 
			throws RemoteException, USIMGroupException {
			int nameLen = 0;
			try {
				nameLen = name.getBytes("GBK").length;
			} catch (java.io.UnsupportedEncodingException e) {
				nameLen = name.length();
			}
			if (nameLen > getUSIMGrpMaxNameLen(slotId))
				throw new USIMGroupException(
						USIMGroupException.ERROR_STR_GRP_NAME_OUTOFBOUND,
						USIMGroupException.GROUP_NAME_OUT_OF_BOUND, slotId);
			final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
			int grpId = -1;
			if (iIccPhb != null)
				grpId = GnIIccPhoneBook.insertUSIMGroup(iIccPhb, name);
				Log.i(TAG, "[createUSIMGroup]inserted grpId:" +grpId);
			if (grpId > 0) {
				UsimGroup usimGroup = new UsimGroup(grpId, name);
				ugrpListArray.addItem(slotId, usimGroup);
			} else {
				switch (grpId) {
				case USIMGroupException.USIM_ERROR_GROUP_COUNT: {
					throw new USIMGroupException(
							USIMGroupException.ERROR_STR_GRP_COUNT_OUTOFBOUND,
							USIMGroupException.GROUP_NUMBER_OUT_OF_BOUND, slotId);
				}
				//Name len has been check before new group.
				//However, do protect here just for full logic.
				case USIMGroupException.USIM_ERROR_NAME_LEN: {
					throw new USIMGroupException(
							USIMGroupException.ERROR_STR_GRP_NAME_OUTOFBOUND,
							USIMGroupException.GROUP_NAME_OUT_OF_BOUND, slotId);
				}
				default:
					break;
				}
			}
			return grpId;
		}
		
		/**
		 * If a group has to change name, the mapping group of USIM card 
		 * should also be changed
		 * 
		 * @return
		 */
		public static int syncUSIMGroupUpdate(int slotId, String oldName, String newName)
			throws RemoteException, USIMGroupException {
			final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
			int grpId = hasExistGroup(slotId, oldName);
			logd(TAG,"grpId:" + grpId + "|slotId:" + slotId + "|oldName:" + oldName + "|newName:" + newName);
			if (grpId > 0) {
				int nameLen = 0;
				try {
				    if(!TextUtils.isEmpty(newName)){
		                 nameLen = newName.getBytes("GBK").length; 
				    }else{
				        return grpId;
				    }
				} catch (java.io.UnsupportedEncodingException e) {
					nameLen = newName.length();
				}
				if (getUSIMGrpMaxNameLen(slotId) < nameLen)
					throw new USIMGroupException(
							USIMGroupException.ERROR_STR_GRP_NAME_OUTOFBOUND,
							USIMGroupException.GROUP_NAME_OUT_OF_BOUND, slotId);
			    int ret = GnIIccPhoneBook.updateUSIMGroup(iIccPhb, grpId, newName);//qc--mtk
			    if (ret == USIMGroupException.USIM_ERROR_NAME_LEN) {
			    	throw new USIMGroupException(
							USIMGroupException.ERROR_STR_GRP_COUNT_OUTOFBOUND,
							USIMGroupException.GROUP_NUMBER_OUT_OF_BOUND, slotId);
			    }
			     
			    UsimGroup usimGrp = ugrpListArray.getItem(slotId, grpId);
			    logd(TAG, "[syncUSIMGroupUpdate]: usimGrp is null = " + (usimGrp == null));			    
			    if (usimGrp != null) {
			    	usimGrp.setAlphaTag(newName);
			    } 
			    
			}
            return grpId;
		}
		
		
		/**
		 * TDB: delete contacts in this USIM card.
		 * @param grpName
		 * @return
		 */
		public static int[] syncUSIMGroupDeleteDualSim(String grpName) {
			int[] errFlag = new int[SLOT_COUNT];
			int flag = -2;
			if (FeatureOption.MTK_GEMINI_SUPPORT) {
				if ((flag = deleteUSIMGroup(GEMINI_SLOT1, grpName))>0) {
					errFlag[GEMINI_SLOT1] = flag;
				}
				if ((flag = deleteUSIMGroup(GEMINI_SLOT2, grpName))>0) {
					errFlag[GEMINI_SLOT2] = flag;
				}
			} else {
				if ((flag = deleteUSIMGroup(SINGLE_SLOT, grpName))>0) {
					errFlag[SINGLE_SLOT] = flag;
				}
			}
			return errFlag;
		}
		
		// Framework interface, here should be change in future.
		public static void deleteUSIMGroupMembersAllSim(Context context, String grpName) {
			ContentResolver cr = context.getContentResolver();
			if (grpName == null)
				grpName = "";
			String selection = Contacts._ID
					+ CONTACTS_IN_GROUP_SELECT.replace("?", "'" + grpName + "'")
					+ " AND " + RawContacts.INDICATE_PHONE_SIM + " > 0";
			
			int grpId1 = -1, grpId2 = -1, grpId = -1;
			if (FeatureOption.MTK_GEMINI_SUPPORT) {
				try {
					grpId1 = hasExistGroup(GEMINI_SLOT1, grpName);
				} catch (RemoteException e) {
					grpId1 = -1;
				}
				try {
					grpId2 = hasExistGroup(GEMINI_SLOT2, grpName);
				} catch (RemoteException e) {
					grpId2 = -1;
				}
			} else {
				try {
					grpId = hasExistGroup(SINGLE_SLOT, grpName);
				} catch (RemoteException e) {
					grpId = -1;
				}
			}
			long [] simSlotMap = getUSIMIdArray(context);
			Cursor c = cr.query(Contacts.CONTENT_URI, 
					new String[]{Contacts._ID, Contacts.INDICATE_PHONE_SIM, Contacts.INDEX_IN_SIM},
					selection, null, null);
			try {
				while(c.moveToNext()) {
					int simId = c.getInt(c.getColumnIndexOrThrow(Contacts.INDICATE_PHONE_SIM));
					int simIndex = c.getInt(c.getColumnIndexOrThrow(Contacts.INDEX_IN_SIM));
					logd(TAG,"[deleteUSIMGroupMembersAllSim]simId:" + simId + " ||simIndex:" + simIndex);
					int slotId = -1;
					if (FeatureOption.MTK_GEMINI_SUPPORT) {
						if (simSlotMap[0] == simId && grpId1 > 0) {
							slotId = 0;
							deleteUSIMGroupMember(slotId, simIndex, grpId1);
						} else if (simSlotMap[1] == simId && grpId2 > 0) {
							slotId = 1;
							deleteUSIMGroupMember(slotId, simIndex, grpId2);
						} else {
							long contactId = c.getLong(c.getColumnIndexOrThrow(Contacts._ID));
							logd(TAG,"[deleteUSIMGroupMembersAllSim]Contact has no group. Contact_id:"
									+ contactId + " ||simId:" + simId + " ||slotId:" + slotId);
							continue;
						}
					} else {
						if (simSlotMap[SINGLE_SLOT] == simId && grpId > 0) {
							slotId = SINGLE_SLOT;
							deleteUSIMGroupMember(slotId, simIndex, grpId);
						} else {
							long contactId = c.getLong(c.getColumnIndexOrThrow(Contacts._ID));
							logd(TAG,"[deleteUSIMGroupMembersAllSim]Contact has no group. Contact_id:"
									+ contactId + " ||simId:" + simId + " ||slotId:" + slotId);
							continue;
						}
					}
				}
			} finally {
				c.close();
			}
		}
		public static int deleteUSIMGroup(int slotId, String name) {
			final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
			int errCode = -2;
			try {
				int grpId = hasExistGroup(slotId, name);
				if (grpId > 0) {
				    if (GnIIccPhoneBook.removeUSIMGroupById(iIccPhb, grpId))	{
				    	ugrpListArray.removeItem(slotId, grpId);
	                    errCode = 0;
				    } else
				    	errCode = -1;
				}
			} catch (android.os.RemoteException e) {
				logd(TAG, "catched exception");
			}
            return errCode;
		}
		
		/**
		 * Sync USIM Group info. If a group has no member, it will be deleted.
		 *  
		 * @param context
		 * @param slotId
		 * @param simId
		 */
		public static void syncUSIMGroupDelIfNoMember(Context context, int slotId, int simId) {
			Log.i(TAG, "[syncUSIMGroupDelIfNoMember]slotId: " + slotId + " || simId:" + simId);
			//if (!ContactsUtils.hasSimContactsImported(slotId)) {
			//	return;
			//}
			//Check SIM state of a slot, and remove the cached group id if not ready.
			if (!checkSimStateBySlot(context, slotId)) {
				ugrpListArray.get(slotId).clear();
				return;
			}
			final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
			ArrayList<UsimGroup> ugrpList = ugrpListArray.get(slotId);
			if (ugrpList.isEmpty()) {
				try {
				List<UsimGroup> uList = GnIIccPhoneBook.getUsimGroups(iIccPhb);
				for(UsimGroup ug: uList) {
					String gName = ug.getAlphaTag();
					int gIndex = ug.getRecordIndex();
					if (!TextUtils.isEmpty(gName) && gIndex > 0) {
						ugrpList.add(new UsimGroup(gIndex, gName));
					}
				}
				} catch (Exception e) {
					logd(TAG, "catched exception");
					e.printStackTrace();
				}
			}
			ContentResolver cr = context.getContentResolver();
			ArrayList<Integer> delArray = new ArrayList<Integer>();
			for (UsimGroup usimGroup: ugrpList) {
				String gName = usimGroup.getAlphaTag();
				Log.i(TAG, "[syncUSIMGroupDelIfNoMember]group name: " + gName);
				Uri groupUri = Uri.withAppendedPath(Contacts.CONTENT_GROUP_URI, gName);
				Cursor c = cr.query(groupUri, new String[]{Contacts._ID}, 
						Contacts.INDICATE_PHONE_SIM + "=" + simId, null, null);
				Log.i(TAG, "[syncUSIMGroupDelIfNoMember]cursor count:" + c.getCount());
				if (c.getCount() < 1 ) {
					try {
						int ugrpId = usimGroup.getRecordIndex();
						if (GnIIccPhoneBook.removeUSIMGroupById(iIccPhb, ugrpId)) {
							delArray.add(ugrpId);
					    }
					} catch (Exception e) {
						logd(TAG, "catched exception");
					}
				}
				c.close();
			}
			for(int i: delArray) {
				ugrpListArray.removeItem(slotId, i);
			}
		}
		
		/**
		 * To call this function need to check slot, SIM and group info before calling.
		 * 
		 * @param context
		 * @param slotId
		 * @param simId
		 * @param gName
		 * @param ugroupId
		 */
		public static void syncExistUSIMGroupDelIfNoMember(Context context,
				int slotId, int simId, String gName, int ugroupId) {
			Log.i(TAG, "[syncUSIMGroupDelIfNoMember]group name: " + gName);
			//Check whether SIM contacts have been imported. If not, do nothing.
			//if (!ContactsUtils.hasSimContactsImported(slotId)) {
			//	return;
			//}
			//Check SIM state of a slot, and remove the cached group id if not ready.
			if (!checkSimStateBySlot(context, slotId)) {
				ugrpListArray.removeItem(slotId, ugroupId);
				return;
			}
			Uri groupUri = Uri.withAppendedPath(Contacts.CONTENT_GROUP_URI, gName);
			Cursor c = context.getContentResolver().query(groupUri, new String[]{Contacts._ID}, 
					Contacts.INDICATE_PHONE_SIM + "=" + simId, null, null);
			Log.i(TAG, "[syncUSIMGroupDelIfNoMember]cursor count:" + c.getCount());
			if (c.getCount() < 1 ) {
				try {
					final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
					if (GnIIccPhoneBook.removeUSIMGroupById(iIccPhb, ugroupId)) {
						ugrpListArray.removeItem(slotId, ugroupId);
				    }
				} catch (Exception e) {
					logd(TAG, "catched exception");
				}
			}
			c.close();
		}
		
		public static boolean addUSIMGroupMember(int slotId, int simIndex, int grpId) {
			boolean succFlag = false;
			try {
				if (grpId > 0) {
					final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
					if (iIccPhb != null) {
						succFlag = GnIIccPhoneBook.addContactToGroup(iIccPhb, simIndex, grpId);//qc--mtk
						succFlag = true;//Only for test, should be removed after framework is ready.
					}
				}
			} catch (Exception e) {
				logd(TAG, "catched exception");
				succFlag = false;
			}
			logd(TAG, "[addUSIMGroupMember]succFlag" + succFlag);
			return succFlag;
		}
		
		public static boolean deleteUSIMGroupMember(int slotId, int simIndex, int grpId) {
		    Log.i(TAG, slotId+"-----deleteUSIMGroupMember[slotId]"); 
		    Log.i(TAG, simIndex+"-----deleteUSIMGroupMember[simIndex]");
		    Log.i(TAG, grpId+"-----deleteUSIMGroupMember[grpId]");
			boolean succFlag = false;
			try {
				if (grpId > 0) {
					final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
					if (iIccPhb != null) {
						succFlag = GnIIccPhoneBook.removeContactFromGroup(iIccPhb, simIndex, grpId);//qc--mtk
						succFlag = true;
					}
				}
			} catch (Exception e) {
				logd(TAG, "catched exception.");
				succFlag = false;
			}
			return succFlag;
		}
		
		
		public static long[] getUSIMIdArray(Context context) {
			long[] simIdArray = new long[SLOT_COUNT];
			for(int i=0;i<SLOT_COUNT;i++)
				simIdArray[i] = -1;
			final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
					.getService(Context.TELEPHONY_SERVICE));
			if (iTel == null) {
				logd(TAG,"iTel is " + iTel);
				return null;
			}
			try {
				ArrayList<SIMInfo> simInfoArray = (ArrayList<SIMInfo>)SIMInfo.getInsertedSIMList(context);
				logd(TAG,"Inserted SIM count:" + simInfoArray.size());
				if (FeatureOption.MTK_GEMINI_SUPPORT) {
					for(SIMInfo simInfo:simInfoArray) {
						if (simInfo.mSlot == GEMINI_SLOT1
						        //qc--mtk
								//&& SIM_TYPE_USIM.equals(iTel.getIccCardTypeGemini(simInfo.mSlot))) {
						        && SIM_TYPE_USIM.equals(AuroraCardTypeUtils.getIccCardTypeGemini(iTel, simInfo.mSlot))) {
							simIdArray[GEMINI_SLOT1] = simInfo.mSimId;
						}
						if (simInfo.mSlot == GEMINI_SLOT2
						        //qc--mtk
								//&& SIM_TYPE_USIM.equals(iTel.getIccCardTypeGemini(simInfo.mSlot))) {
						        && SIM_TYPE_USIM.equals(AuroraCardTypeUtils.getIccCardTypeGemini(iTel, simInfo.mSlot))) {
							simIdArray[GEMINI_SLOT2] = simInfo.mSimId;
						}
					}
				} else {
				    //qc--mtk
					//if (SIM_TYPE_USIM.equals(iTel.getIccCardType())) {
				    if (SIM_TYPE_USIM.equals(AuroraCardTypeUtils.getIccCardType(iTel))) {
						simIdArray[SINGLE_SLOT] = simInfoArray.get(0).mSimId;
					}
				}
			} catch (Exception e) {
				logd(TAG,"catched exception.");
				e.printStackTrace();
			}
			return simIdArray;
		}
		
		public static String buildUSIMIdArrayString(Context context) {
			long[] resArray = getUSIMIdArray(context);
			if (resArray.length < 1)
				return "";
			StringBuilder sbuilder = new StringBuilder();
			for(int i=0,count=resArray.length;i<count;i++) {
				//Check whether SIM contacts have been imported. If not, return null string.
				//if (!ContactsUtils.hasSimContactsImported(i)) {
				//	continue;
				//}
				sbuilder.append(resArray[i]);
				sbuilder.append(",");
			}
			if (sbuilder.length()>0) {
				sbuilder.deleteCharAt(sbuilder.length()-1);
			}
			return sbuilder.toString();
		}
		
		public static String buildUSIMIdArrayStringWithPhone(Context context) {
			String simIdStr = buildUSIMIdArrayString(context);
			String resStr;
			if (TextUtils.isEmpty(simIdStr)) {
				resStr = RawContacts.INDICATE_PHONE + "";
			} else {
				resStr = simIdStr + "," + RawContacts.INDICATE_PHONE; 
			}
			return resStr; 
		}
		
		public static void lockAllUGRP() {
			UGRP_SLOT_LOCK = SLOT_COUNT;
		}
		public static void lockUGRP () {
			UGRP_SLOT_LOCK++;
		}
		public static void unLockUGRP() {
			UGRP_SLOT_LOCK--;
		}
		public static boolean isUGRPLocked() {
			return UGRP_SLOT_LOCK >0;
		}
		
		
		/**
		 * Sync USIM group
		 * @param context
		 * @param slotId
		 * @param grpIdMap The pass in varible must not be null.
		 */
        public static synchronized void syncUSIMGroupContactsGroup(Context context,
                final ServiceWorkData workData, HashMap<Integer, Integer> grpIdMap) {
			logd(TAG, "syncUSIMGroupContactsGroup begin");
			
			if (workData.mSimType != SimCardUtils.SimType.SIM_TYPE_USIM)
			    return;
			final int slotId = workData.mSlotId;
			final int simId = workData.mSimId;
			final int workType = workData.mWorkType;
			
			ArrayList<UsimGroup> ugrpList = ugrpListArray.get(slotId);
			if (workType == AbstractStartSIMService.SERVICE_WORK_REMOVE) {
			    deleteUSIMGroupOnPhone(context, slotId);
			    ugrpList.clear();
			    logd(TAG, "syncUSIMGroupContactsGroup end. deleteUSIMGroupOnPhone.");
			    return;
			}
			//Get All groups in USIM   
			ugrpList.clear();
			final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
			// Gionee:wangth 20120705 add for CR00637245 begin
			if (iIccPhb == null) {
			    return;
			}
			// Gionee:wangth 20120705 add for CR00637245 end
			try {
				List<UsimGroup> uList = GnIIccPhoneBook.getUsimGroups(iIccPhb);
				for (UsimGroup ug : uList) {
					String gName = ug.getAlphaTag();
					int gIndex = ug.getRecordIndex();
                    Log.i(TAG, "[syncUSIMGroupContactsGroup]gName:" + gName + "|gIndex: " + gIndex);
					
					if (!TextUtils.isEmpty(gName) && gIndex > 0) {
						ugrpList.add(new UsimGroup(gIndex, gName));
					}
				}
			} catch (Exception e) {
				logd(TAG, "catched exception");
				e.printStackTrace();
			}
			
			try {
				Log.i(TAG, "getUSIMGrpMaxNameLen begin");
				MAX_USIM_GROUP_NAME_LENGTH[slotId] = GnIIccPhoneBook.getUSIMGrpMaxNameLen(iIccPhb);//qc--mtk
                Log.i(TAG, "getUSIMGrpMaxNameLen end. slot:" + slotId
                        + "|NAME_LENGTH:"
                        + MAX_USIM_GROUP_NAME_LENGTH[slotId]);
				Log.i(TAG, "getUSIMGrpMaxCount begin.");
				MAX_USIM_GROUP_COUNT[slotId] = GnIIccPhoneBook.getUSIMGrpMaxCount(iIccPhb);//qc--mtk
                Log.i(TAG, "getUSIMGrpMaxCount end. slot:" + slotId
                        + "|GROUP_COUNT:"
                        + MAX_USIM_GROUP_COUNT[slotId]);
			} catch (Exception e) {
				MAX_USIM_GROUP_NAME_LENGTH[slotId] = -1;
				MAX_USIM_GROUP_COUNT[slotId] = -1;
			}

			//Query SIM info to get simId
			//Query to get all groups in Phone
			ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(Groups.CONTENT_SUMMARY_URI, null,
                    Groups.DELETED + "=0 AND " + 
                    Groups.ACCOUNT_TYPE + "='USIM Account' AND " 
                    + Groups.ACCOUNT_NAME + "=" + "'USIM" + slotId + "'", null, null);
			//Query all Group including deleted group
            
            // Gionee:wangth 20121129 add for CR00737337 begin
            if (null == c) {
                return;
            }
            // Gionee:wangth 20121129 add end
            
            HashMap<String, Integer> noneMatchedMap = new HashMap<String, Integer>();
            c.moveToPosition(-1);
            while(c.moveToNext()) {
                String grpName = c.getString(c.getColumnIndexOrThrow(Groups.TITLE));
                int grpId = c.getInt(c.getColumnIndexOrThrow(Groups._ID));
                if (!noneMatchedMap.containsKey(grpName))
                    noneMatchedMap.put(grpName, grpId);
            }
            c.close();
            
			if (ugrpList != null) {
			    boolean hasMerged = false;
    			for (UsimGroup ugrp: ugrpList) {
    				String ugName = ugrp.getAlphaTag();
    				hasMerged = false;
    				long groupId = -1;
    				if (!TextUtils.isEmpty(ugName)) {
    					int ugId = ugrp.getRecordIndex();
    					if (noneMatchedMap.containsKey(ugName)) {
    					    groupId = noneMatchedMap.get(ugName);
    					    noneMatchedMap.remove(ugName);
    					    hasMerged = true;
    					}
    					
    					if (!hasMerged) {
    						//Need to create on phone
    						ContentValues values = new ContentValues();
    						values.put(Groups.TITLE, ugName);
    						values.put(Groups.GROUP_VISIBLE, 1);
    						values.put(Groups.SYSTEM_ID, 0);
    						values.put(Groups.ACCOUNT_NAME, "USIM" + slotId);
    	                    values.put(Groups.ACCOUNT_TYPE, "USIM Account");
    						Uri uri = cr.insert(Groups.CONTENT_URI, values);
    						groupId = (uri == null) ? 0 : ContentUris.parseId(uri);
    					}
    					if (groupId > 0)
    					    grpIdMap.put(ugId, (int) groupId);
    				}
    			}
    			
    			if (noneMatchedMap.size() > 0) {
    			    Integer [] groupIdArray = noneMatchedMap.values().toArray(new Integer[0]);
    			    StringBuilder delGroupIdStr = new StringBuilder();
                    for (Integer i:groupIdArray) {
                        int delGroupId = i;
                        delGroupIdStr.append(delGroupId).append(",");
                    }
                    if (delGroupIdStr.length() > 0) {
                        delGroupIdStr.deleteCharAt(delGroupIdStr.length() - 1);
                    }
                    if (delGroupIdStr.length() > 0) {
                        cr.delete(Groups.CONTENT_URI, Groups._ID + " IN ("
                                + delGroupIdStr.toString() + ")", null);
                    }
    			}
    			logd(TAG, "syncUSIMGroupContactsGroup end");
    		} else {
    		    deleteUSIMGroupOnPhone(context, slotId);
    		}
		}
		
		public static int getUSIMGrpMaxNameLen(int slot) {
			if (slot<0 || slot >1)
				return -1;
			logd(TAG, "[getUSIMGrpMaxNameLen]slot:" + slot 
			        + "|maxNameLen:" + MAX_USIM_GROUP_NAME_LENGTH[slot]);
			if (MAX_USIM_GROUP_NAME_LENGTH[slot] < 0) {
				try {
					final IIccPhoneBook iIccPhb = getIIccPhoneBook(slot);
					if (iIccPhb != null) {
						MAX_USIM_GROUP_NAME_LENGTH[slot] = GnIIccPhoneBook.getUSIMGrpMaxNameLen(iIccPhb);//qc--mtk
					}
				} catch (Exception e) {
					logd(TAG, "catched exception.");
					MAX_USIM_GROUP_NAME_LENGTH[slot] = -1;
				}
			}
			logd(TAG, "[getUSIMGrpMaxNameLen]end slot:" + slot 
                    + "|maxNameLen:" + MAX_USIM_GROUP_NAME_LENGTH[slot]);
			return MAX_USIM_GROUP_NAME_LENGTH[slot];
		}
		
		public static int getUSIMGrpMaxCount(int slot) {
			if (slot<0 || slot >1)
				return -1;
			logd(TAG, "[getUSIMGrpMaxCount]slot:" + slot 
                    + "|maxGroupCount:" + MAX_USIM_GROUP_COUNT[slot]);
			if (MAX_USIM_GROUP_COUNT[slot] < 0) {
				try {
					final IIccPhoneBook iIccPhb = getIIccPhoneBook(slot);
					if (iIccPhb != null) {
						MAX_USIM_GROUP_COUNT[slot] = GnIIccPhoneBook.getUSIMGrpMaxCount(iIccPhb);//qc--mtk
					}
				} catch (Exception e) {
					logd(TAG, "catched exception.");
					MAX_USIM_GROUP_COUNT[slot] = -1;
				}
			}
			logd(TAG, "[getUSIMGrpMaxCount]end slot:" + slot 
                    + "|maxGroupCount:" + MAX_USIM_GROUP_COUNT[slot]);
			return MAX_USIM_GROUP_COUNT[slot];
		}
		
		/**
		 * Check the SIM state of a slot whether is ready.
		 *  
		 * @param context
		 * @param slotId		
		 * @return			If SIM is locked or not ready, it will return false.
		 */
		public static boolean checkSimStateBySlot(Context context, final int slotId) {
			try {
				final ITelephony iTel = ITelephony.Stub
						.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
				if (iTel == null)
					return false;
				TelephonyManager telephonyMgr = TelephonyManager.getDefault();
				ContentResolver resolver = context.getContentResolver();
				if (FeatureOption.MTK_GEMINI_SUPPORT) {
					boolean simPUKReq = TelephonyManager.SIM_STATE_PUK_REQUIRED == GnTelephonyManager.getSimStateGemini(slotId);
					boolean simPINReq = TelephonyManager.SIM_STATE_PIN_REQUIRED == GnTelephonyManager.getSimStateGemini(slotId);
					// boolean simReady = ContactsUtils.simStateReady(slotId);
					int DualSimSet = Settings.System.getInt(resolver, ContactsFeatureConstants.DUAL_SIM_MODE_SETTING, 3);
					boolean isRadioOn = (Settings.System.getInt(resolver,Settings.System.AIRPLANE_MODE_ON, 0) == 0)
							&& ((slotId + 1 == DualSimSet) || (3 == DualSimSet));
					Log.d(TAG, "[checkSimState|GE]slotId: " + slotId + "simPUKReq: "
							+ simPUKReq + "||simPINReq: " + simPINReq + "||isRadioOn" + isRadioOn
							+ "||iTel.isFDNEnabled()" + GnITelephony.isFDNEnabled(iTel));
					if (simPUKReq || simPINReq || !isRadioOn || GnITelephony.isFDNEnabledGemini(iTel, slotId)) {
						return false;
					}
				} else {
					boolean simPUKReq = TelephonyManager.SIM_STATE_PUK_REQUIRED == telephonyMgr.getSimState();
					boolean simPINReq = TelephonyManager.SIM_STATE_PIN_REQUIRED == telephonyMgr.getSimState();
					// boolean simReady = ContactsUtils.simStateReady();
					boolean isRadioOn = Settings.System.getInt(resolver, Settings.System.AIRPLANE_MODE_ON, 0) == 0;
					Log.d(TAG, "[checkSimState|SL]simPUKReq: " + simPUKReq 
							+ "||simPINReq: " + simPINReq + "||isRadioOn" + isRadioOn
							+ "||iTel.isFDNEnabled()" + GnITelephony.isFDNEnabled(iTel));
					if (simPUKReq || simPINReq || !isRadioOn || GnITelephony.isFDNEnabled(iTel)) {
						return false;
					}
				}
			} catch (Exception e) {
				Log.d(TAG, "catched Exception");
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		
		public static void deleteUSIMGroupOnPhone(Context context, int slotId) {
		    ContentResolver cr = context.getContentResolver();
		    Builder builder = Groups.CONTENT_URI.buildUpon();
		    builder.appendQueryParameter(RawContacts.ACCOUNT_NAME, "USIM" + slotId);
		    builder.appendQueryParameter(RawContacts.ACCOUNT_TYPE, "USIM Account");
		    cr.delete(builder.build(), null, null);
		}
		
	}

	public class SIMSlotMap{
		private static final String SIM_TYPE_USIM = USIMGroup.SIM_TYPE_USIM;
		private HashMap<Long,Integer> mSimSlotCache = new HashMap<Long,Integer>();
		private Context mContext;
		
		public SIMSlotMap(Context context) {
			mContext = context.getApplicationContext();
		}
		
		public int getSlotIdBySimId(int simId) {
			if (mSimSlotCache.isEmpty())
				loadCache();
			Integer res = mSimSlotCache.get((long)simId);
			return (res == null)?-1:res;
		}
		
		public int getUSIMSlot(int simId) {
			if (mSimSlotCache.isEmpty())
				loadCache();
			Integer res = mSimSlotCache.get((long)simId);
			int slotId = (res == null)?-1:res;
			int uSlotId = -1;
			try {
				final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
								.getService(Context.TELEPHONY_SERVICE));
				if (FeatureOption.MTK_GEMINI_SUPPORT) {
				    //qc--mtk
					//if (iTel != null && SIM_TYPE_USIM.equals(iTel.getIccCardTypeGemini(slotId))) {
				    if (iTel != null && SIM_TYPE_USIM.equals(AuroraCardTypeUtils.getIccCardTypeGemini(iTel, slotId))) {
						uSlotId = slotId;
					}
				} else {
				    //qc--mtk
					//if (iTel != null && SIM_TYPE_USIM.equals(iTel.getIccCardType())) {
				    if (iTel != null && SIM_TYPE_USIM.equals(AuroraCardTypeUtils.getIccCardType(iTel))) {
						uSlotId = slotId;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return uSlotId;
		}

		public boolean isEmpty() {
			return mSimSlotCache.isEmpty();
		}
		
		public void reloadCache() {
			logd(TAG, "[SIMSlotMap]reloadCache");
			mSimSlotCache.clear();
		}
		
		private void loadCache() {
			logd(TAG, "[SIMSlotMap]loadCache");
			ArrayList<SIMInfo> simInfoArray = (ArrayList<SIMInfo>) SIMInfo.getInsertedSIMList(mContext);
			for (SIMInfo simInfo : simInfoArray) {
				logd(TAG, "mSimId:" + simInfo.mSimId + " || mSlot:" + simInfo.mSlot);
				mSimSlotCache.put(simInfo.mSimId, simInfo.mSlot);
			}
		}
		
	}
	
	public static class USIMGroupException extends Exception {

		private static final long serialVersionUID = 1L;
		
		public static final String ERROR_STR_GRP_NAME_OUTOFBOUND = "Group name out of bound";
		public static final String ERROR_STR_GRP_COUNT_OUTOFBOUND = "Group count out of bound";
		public static final int GROUP_NAME_OUT_OF_BOUND = 1;
		public static final int GROUP_NUMBER_OUT_OF_BOUND = 2;
		//Exception type definination in framework. 
		public static final int USIM_ERROR_NAME_LEN = ContactsFeatureConstants.USIM_ERROR_NAME_LEN;
		public static final int USIM_ERROR_GROUP_COUNT = ContactsFeatureConstants.USIM_ERROR_GROUP_COUNT;
		
		int mErrorType;
		int mSlotId;
		
		USIMGroupException() {
			super();
		}

		USIMGroupException(String msg) {
			super(msg);
		}
		
		USIMGroupException(String msg, int errorType, int slotId) {
			super(msg);
			mErrorType = errorType;
			mSlotId = slotId;
		}
		
		public int getErrorType() {
			return mErrorType;
		}
		
		public int getErrorSlotId() {
			return mSlotId;
		}

		@Override
		public String getMessage() {
			return "Details message: errorType:" + mErrorType + "\n"
					+ super.getMessage();
		}
	}
	
	static void logd(String TAG, String msg) {
		if (DBG) {
			Log.d(TAG, msg);
		}
	}
	
	
	public static class ContactsGroupArrayData {
		private int mSimIndex;
		private int mSimIndexPhoneOrSim;
		public int getmSimIndex() {
			return mSimIndex;
		}
		public void setmSimIndex(int mSimIndex) {
			this.mSimIndex = mSimIndex;
		}
		public int getmSimIndexPhoneOrSim() {
			return mSimIndexPhoneOrSim;
		}
		public void setmSimIndexPhoneOrSim(int mSimIndexPhoneOrSim) {
			this.mSimIndexPhoneOrSim = mSimIndexPhoneOrSim;
		}
		public ContactsGroupArrayData initData(int simIndex, int mSimIndexPhoneorSim){
			mSimIndex = simIndex;
			mSimIndexPhoneOrSim = mSimIndexPhoneorSim;
			return this;
		}
	}
	
	public static final String SELECTION_MOVE_GROUP_DATA = RawContacts.CONTACT_ID
	                            + " IN (%1) AND "
	                            + Data.MIMETYPE
	                            + "='"
	                            + GroupMembership.CONTENT_ITEM_TYPE
	                            + "' AND "
	                            + GroupMembership.GROUP_ROW_ID + "='%2'";
	
	private static final int MAX_OP_COUNT_IN_ONE_BATCH = 150;
	
	public static void doMove(ContentResolver resolver, String fromUgroupName, int slotId,
			String toUgroupName, HashMap<Long, ContactsGroupArrayData> selectedContactsMap,
			long fromPgroupId, long toPgroupId) {

		int fromUgroupId = -1;
		int toUgroupId = -1;
		if (slotId >= 0) {
	      try {
	          fromUgroupId = ContactsGroupUtils.USIMGroup.hasExistGroup(slotId, fromUgroupName);
	          toUgroupId = ContactsGroupUtils.USIMGroup.hasExistGroup(slotId, toUgroupName);
	         } catch (RemoteException e) {
	             //log
	             fromUgroupId = -1;
	             toUgroupId = -1;
	         }
		}

		
		Uri uri = Contacts.CONTENT_GROUP_URI.buildUpon().appendPath(
				toUgroupName).build();
		Cursor c = resolver.query(uri, new String[] { Contacts._ID }, null,
				null, null);
		HashSet<Long> set = new HashSet<Long>();
		while (c != null && c.moveToNext()) {
			long contactId = c.getLong(0);
			Log.i(TAG, contactId+"--------contactId");
			set.add(contactId);
		}
		if (c != null)
			c.close();

		ContentValues values = new ContentValues();
		StringBuilder idBuilder = new StringBuilder();
		StringBuilder idBuilderDel = new StringBuilder();
		// USIM group begin
		boolean isInTargetGroup = false;
		// USIM group end
		Iterator<Entry<Long, ContactsGroupArrayData>> iter = selectedContactsMap.entrySet()
				.iterator();
		int moveCount = 0;

		while (iter.hasNext()) {
			Entry<Long, ContactsGroupArrayData> entry = iter.next();
			long id = entry.getKey();
			Log.i(TAG, id+"--------entry.getKey()");
			// USIM Group begin
			isInTargetGroup = set.contains(id);
			
			
			int tsimId = entry.getValue().mSimIndexPhoneOrSim;
			
			if(DEBUG)Log.i(TAG, "contactsId--------------"+id);
			if(DEBUG)Log.i(TAG, "mSimIndexPhoneOrSim--------------"+tsimId);
			if(DEBUG)Log.i(TAG, "mSimIndex--------------"+entry.getValue().mSimIndex);
			if (tsimId > 0
					&& !moveUSIMGroupMember(entry.getValue(), slotId,
							 isInTargetGroup, fromUgroupId, toUgroupId)) {
				// failed to move USIM contacts from one group to another
				Log.d(TAG,"Failed to move USIM contacts from one group to another");
				continue;
			}
			
			if (isInTargetGroup) { // mark as need to be delete later
				if (idBuilderDel.length() > 0) {
					idBuilderDel.append(",");
				}
				idBuilderDel.append(id);
			} else { // mark as need to be update later
				if (idBuilder.length() > 0) {
					idBuilder.append(",");
				}
				idBuilder.append(id);
			}
			// USIM Group end
			moveCount++;
			if (moveCount > MAX_OP_COUNT_IN_ONE_BATCH) {
				int count = 0;
				if (idBuilder.length() > 0) {
					String where = SELECTION_MOVE_GROUP_DATA.replace("%1",
							idBuilder.toString()).replace("%2",
							String.valueOf(fromPgroupId));
					Log.i(TAG, "[doMove]where: " + where);
					values.put(CommonDataKinds.GroupMembership.GROUP_ROW_ID,
							toPgroupId);

					count = resolver.update(GnContactsContract.Data.CONTENT_URI,
							values, where, null);
					idBuilder.setLength(0);
				}
				Log.i(TAG, "[doMove]move data count:" + count);
				count = 0;
				if (idBuilderDel.length() > 0) {
					String whereDel = SELECTION_MOVE_GROUP_DATA.replace("%1",
							idBuilderDel.toString());
					whereDel = whereDel.replace("%2", String
							.valueOf(fromPgroupId));
					Log.i(TAG, "[doMove]whereDel: " + whereDel);
					count = resolver.delete(GnContactsContract.Data.CONTENT_URI,
							whereDel, null);
					Log.i(TAG, "[doMove]delete repeat contact:"
							+ whereDel.toString());
					idBuilderDel.setLength(0);
				}
				Log.i(TAG, "[doMove]delete repeat data count:" + count);
				moveCount = 0;
			}
		}
		int count = 0;
		if (idBuilder.length() > 0) {
			String where = SELECTION_MOVE_GROUP_DATA.replace("%1",
					idBuilder.toString()).replace("%2",
					String.valueOf(fromPgroupId));
			Log.i(TAG, "[doMove]End where: " + where);
			values
					.put(CommonDataKinds.GroupMembership.GROUP_ROW_ID,
							toPgroupId);

			count = resolver.update(GnContactsContract.Data.CONTENT_URI, values,
					where, null);
			idBuilder.setLength(0);
		}
		Log.i(TAG, "[doMove]End move data count:" + count);
		count = 0;
		if (idBuilderDel.length() > 0) {
			String whereDel = SELECTION_MOVE_GROUP_DATA.replace("%1",
					idBuilderDel.toString());
			whereDel = whereDel.replace("%2", String.valueOf(fromPgroupId));
			Log.i(TAG, "[doMove]End whereDel: " + whereDel);
			count = resolver.delete(GnContactsContract.Data.CONTENT_URI,
					whereDel, null);
			idBuilderDel.setLength(0);
		}
		Log.i(TAG, "[doMove]End delete repeat data count:" + count);
	}

	/**
	 * Move contacts from one USIM group to another
	 * 
	 * @param data
	 *            Contacts data.
	 * @param ugrpIdArray
	 *            Must be created before calling, and the array length must 4.
	 *            the first one indicates old USIM group id, and the second one
	 *            indicates the target USIM group id.
	 * @param isInTargetGroup
	 *            This variable indicates whether a contacts is already in
	 *            target group.
	 */
	public static boolean moveUSIMGroupMember(ContactsGroupArrayData data, int slotId,
			boolean isInTargetGroup, int fromUgrpId, int toUgrpId) {
		boolean ret = false;
		int simId = data.mSimIndexPhoneOrSim;
		if(DEBUG)Log.i(TAG, simId+"--------simId[moveUSIMGroupMember]");
		if(DEBUG)Log.i(TAG, slotId+"--------slotId[moveUSIMGroupMember]");
		if (simId >=0) {
			if (slotId >= 0) {

				// Add group data into new USIM group if it is not in new USIM
				// group
				boolean addSucess = false;

				if (!isInTargetGroup) {
                   
                    if(DEBUG)Log.i(TAG, slotId+"--------slotId");
                    if(DEBUG)Log.i(TAG, data.mSimIndex+"--------data.mSimIndex");
                    if(DEBUG)Log.i(TAG, toUgrpId+"--------toUgrpId");
					addSucess = ContactsGroupUtils.USIMGroup.addUSIMGroupMember(slotId, data.mSimIndex,
									toUgrpId);		
					if(DEBUG)Log.i(TAG, addSucess+"--------addSucess");

				}
				// Delete group data in old USIM group.
				if (isInTargetGroup
						|| (fromUgrpId > 0 && toUgrpId > 0 && addSucess)) {
					ret = true;
					ContactsGroupUtils.USIMGroup.deleteUSIMGroupMember(slotId,data.mSimIndex, fromUgrpId);				
					if(DEBUG)Log.i(TAG, fromUgrpId+"--------fromUgrpId");
				}
			}
		}
		return ret;
	}
}
 
