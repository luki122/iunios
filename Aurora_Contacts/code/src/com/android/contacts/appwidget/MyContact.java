package com.android.contacts.appwidget;

public class MyContact {
	String name;
	String number;
	String lookupUri;
	String photoUri;
	int count;
	long date;
	long conatctId;

	public MyContact(String name,String number,String lookupUri,String photoUri,int count,long date,long contactId){
		this.name=name;
		this.number=number;
		this.lookupUri=lookupUri;
		this.photoUri=photoUri;
		this.count=count;
		this.date=date;
		this.conatctId=contactId;
	}
	
	

	public long getConatctId() {
		return conatctId;
	}



	public void setConatctId(long conatctId) {
		this.conatctId = conatctId;
	}



	public String getName() {
		return name;
	}

	public String getNumber() {
		return number;
	}

	public String getLookupUri() {
		return lookupUri;
	}

	public String getPhotoUri() {
		return photoUri;
	}

	public int getCount() {
		return count;
	}

	public long getDate() {
		return date;
	}		

}
