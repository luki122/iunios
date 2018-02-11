package com.aurora.account.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CountryCode implements Parcelable {

	private String countryOrRegions;
	private String countryOrRegionsCN;
	private String abbreviation;
	private String code;
	
	public CountryCode() {
		
	}
	
	public static CountryCode getDefault() {
	    CountryCode countryCode = new CountryCode();
        countryCode.setCode("86");
        countryCode.setCountryOrRegions("China");
        countryCode.setCountryOrRegionsCN("中国");
        
        return countryCode;
	}

	public String getCountryOrRegions() {
		return countryOrRegions;
	}

	public void setCountryOrRegions(String countryOrRegions) {
		this.countryOrRegions = countryOrRegions;
	}

	public String getCountryOrRegionsCN() {
		return countryOrRegionsCN;
	}

	public void setCountryOrRegionsCN(String countryOrRegionsCN) {
		this.countryOrRegionsCN = countryOrRegionsCN;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return "CountryCode [countryOrRegions=" + countryOrRegions
				+ ", countryOrRegionsCN=" + countryOrRegionsCN
				+ ", abbreviation=" + abbreviation + ", code=" + code + "]";
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.countryOrRegions);
		dest.writeString(this.countryOrRegionsCN);
		dest.writeString(this.abbreviation);
		dest.writeString(this.code);
	}
	
	public static final Parcelable.Creator<CountryCode> CREATOR = new Creator<CountryCode>() {
		@Override
		public CountryCode createFromParcel(Parcel source) {
			return new CountryCode(source);
		}

		@Override
		public CountryCode[] newArray(int size) {
			return new CountryCode[size];
		}
	};
	
	public CountryCode(Parcel in) {
		countryOrRegions = in.readString();
		countryOrRegionsCN = in.readString();
		abbreviation = in.readString();
		code = in.readString();
	}

}
