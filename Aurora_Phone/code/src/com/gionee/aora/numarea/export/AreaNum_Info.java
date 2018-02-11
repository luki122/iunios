package com.gionee.aora.numarea.export;

import android.os.Parcel;
import android.os.Parcelable;

public class AreaNum_Info implements Comparable< String >,Parcelable{

	private String iName;

	private String iNum;

	private String iFlag;
	public AreaNum_Info(){
		iName=new String();
		iNum=new String();
		iFlag=new String();
	}
	
	public String getiName() {
		return iName;
	}

	public void setiName(String iName) {
		this.iName = iName;
	}

	public String getiNum() {
		return iNum;
	}

	public void setiNum(String iNum) {
		this.iNum = iNum;
	}

	public String getiFlag() {
		return iFlag;
	}

	public void setiFlag(String iFlag) {
		this.iFlag = iFlag;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		return "name" + iName + ",num=" + iNum + ",iFlag=" + iFlag ;
	}
	@Override
	public int compareTo(String another) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(iName);
		dest.writeString(iNum);
		dest.writeString(iFlag);
	}

	private AreaNum_Info(Parcel in)
	{
		iName = in.readString();
		iNum = in.readString();
		iFlag = in.readString();
	}
	public static final Parcelable.Creator< AreaNum_Info > CREATOR = new Parcelable.Creator< AreaNum_Info >()
	{
		public AreaNum_Info createFromParcel(Parcel source)
		{
			return new AreaNum_Info(source);
		}

		public AreaNum_Info[] newArray(int size)
		{
			return new AreaNum_Info[ size ];
		}
	};
	
}
