package datas;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class WeatherWarningInfo implements Parcelable {

	private String title;

	private String weather;

	private String level;

	private String detail;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getWeather() {
		return weather;
	}

	public void setWeather(String weather) {
		this.weather = weather;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public WeatherWarningInfo(){}
	
	public WeatherWarningInfo(Parcel in){
		title = in.readString();
		weather = in.readString();
		level = in.readString();
		detail = in.readString();
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof WeatherWarningInfo)
		{
			WeatherWarningInfo warnInfo = (WeatherWarningInfo)o;
			if(warnInfo.getDetail().equals(detail)&&warnInfo.getLevel().equals(level)
				&&warnInfo.getTitle().equals(title)&&warnInfo.getWeather().equals(weather))
			{
				return true;
			}
			
		}
		return false;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(title);
		dest.writeString(weather);
		dest.writeString(level);
		dest.writeString(detail);
	}
	
	public static final Parcelable.Creator<WeatherWarningInfo> CREATOR = new Creator<WeatherWarningInfo>() {
		
		@Override
		public WeatherWarningInfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return new WeatherWarningInfo[size];
		}
		
		@Override
		public WeatherWarningInfo createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new WeatherWarningInfo(source);
		}
	};
	
	
}
