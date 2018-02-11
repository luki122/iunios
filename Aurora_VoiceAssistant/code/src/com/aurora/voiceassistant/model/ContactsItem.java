package com.aurora.voiceassistant.model;

import java.util.LinkedHashMap;

public class ContactsItem {
	private String mContactId;
	private String mLookupKey;
	private String mName;
//	private String mNumber;
//	private int mNumberCount;
	public enum NumberType {
		TBD0, NUMBER_HOME, NUMBER_MOBLIE, NUMBER_WORK, NUMBER_WORKFAX, TBD5, TBD6, NUMBER_OTHER
	}
//	private NumberType mNumberType = NumberType.NUMBER_MOBLIE;
	private LinkedHashMap<NumberType, String> mNumberMap = new LinkedHashMap<NumberType, String>();
	
	public ContactsItem() {
		// TODO Auto-generated constructor stub
	}
	
	public void setContactId(String contactId) {
		mContactId = contactId;
	}
	public void setContactLookUpKey(String lookupkey) {
		mLookupKey = lookupkey;
	}
	public void setContactName(String name) {
		mName = name;
	}
	public void setContactNumber(int type, String number) {
		switch (type) {
			case 1:
				mNumberMap.put(NumberType.NUMBER_HOME, number);
				break;
			case 2:
				mNumberMap.put(NumberType.NUMBER_MOBLIE, number);	
				break;
			case 3:
				mNumberMap.put(NumberType.NUMBER_WORK, number);
				break;
			case 4:
				mNumberMap.put(NumberType.NUMBER_WORKFAX, number);
				break;
			case 7:
				mNumberMap.put(NumberType.NUMBER_OTHER, number);
				break;
			default:
				mNumberMap.put(NumberType.NUMBER_MOBLIE, number);	
				break;
		}
	}
	/*private void setContactNumberCount(int count) {
		mNumberCount = count;
	}*/
	/*private void setContactNumberType(NumberType type) {
		mNumberType = type;
	}*/
	
	public String getContactId() {
		return mContactId;
	}
	public String getContactLookUpKey() {
		return mLookupKey;
	}
	public String getContactName() {
		return mName;
	}
	/*private String getContactNumber() {
		return mNumber;
	}*/
	public LinkedHashMap<NumberType, String> getContactNumberMap() {
		return mNumberMap;
	}
	public int getContactNumberCount() {
		if (mNumberMap != null) {
			return mNumberMap.size();
		}
		return -1;
	}

}
