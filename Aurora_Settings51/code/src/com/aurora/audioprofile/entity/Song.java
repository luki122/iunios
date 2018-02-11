package com.aurora.audioprofile.entity;

import android.os.Parcel;
import android.os.Parcelable;


public class Song implements Parcelable {

	private int id;// id	
//	private String name;
	private String displayName;	
	private String filePath;
	private String orderName;
	
	

	public Song() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	
	/*public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}*/

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	

	public String getOrderName() {
		return orderName;
	}

	public void setOrderName(String orderName) {
		this.orderName = orderName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	
	
	public static final Parcelable.Creator<Song> CREATOR = new Creator<Song>() {

		public Song createFromParcel(Parcel source) {
			Song song = new Song();
			song.id = source.readInt();			
//			song.name=source.readString();
			song.displayName = source.readString();			
			song.filePath = source.readString();			
			return song;
		}

		public Song[] newArray(int size) {
			return new Song[size];
		}

	};

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		
//		dest.writeString(name);
		dest.writeString(displayName);		
		dest.writeString(filePath);		
	}

}