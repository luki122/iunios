package com.aurora.thememanager.entities;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.aurora.thememanager.utils.ThemeConfig;

public class ThemeAudio extends Theme {
	
	
	public static final String PATH = ThemeConfig.THEME_BASE_PATH+"audio/";
	
	public static final String PATH_RING_TONG = PATH+"ringtong/";
	
	public static final String PATH_FEELBACK = PATH+"feelback/";
	
	/**
	 * ringtong for phone call
	 */
	public static final int RINGTONE = 0;
	
	/**
	 * ringtong for message call
	 */
	public static final int MESSAGE = 2;
	
	/**
	 * audio for notification
	 */
	public static final int NOTIFICATION = 1;
	
	/**
	 * audio for alarm
	 */
	public static final int ALARM = 3;
	
	/**
	 * audio for message call and notification
	 */
	public static final int MESSAGE_AND_NOTIFICATION = 4;
	
	/**
	 * audio for feelback
	 */
	public static final int FEELBACK = 5;

	/**
	 * type for ringtong
	 */
	public int ringtongType = -1;
	
	
	
	
	public ThemeAudio() {
		
	}
	
	
	
	public ThemeAudio(Parcel in) {
		// TODO Auto-generated constructor stub
		super(in);
		ringtongType = in.readInt();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		// TODO Auto-generated method stub
		super.writeToParcel(out, flags);
		out.writeInt(ringtongType);
	}

	public static void writeToParcel(ThemeAudio theme, Parcel out) {
		if (theme != null) {
			theme.writeToParcel(out, 0);
		} else {
			out.writeString(null);
		}
	}

	public static ThemeAudio readFromParcel(Parcel in) {
		return in != null ? new ThemeAudio(in) : null;
	}

	public static final Parcelable.Creator<ThemeAudio> CREATOR = new Parcelable.Creator<ThemeAudio>() {
		public ThemeAudio createFromParcel(Parcel in) {
			return new ThemeAudio(in);
		}

		public ThemeAudio[] newArray(int size) {
			return new ThemeAudio[size];
		}
	};
	
	
	
	
	
}
