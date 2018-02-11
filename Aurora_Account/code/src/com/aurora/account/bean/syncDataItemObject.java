package com.aurora.account.bean;



import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;


@SuppressLint("ParcelCreator")
public class syncDataItemObject implements Parcelable {

	
	
	// 各模块的字段集合
	private String body;
	private String op;
	private String id;
	private String date;

	private String syncid;
	//服务器返回的结果
	private String result;
	// 附件数组
	private accessoryObj accOjb = new accessoryObj();
	public syncDataItemObject()
	{
		
	}
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public accessoryObj getAccOjb() {
		return accOjb;
	}

	public void setAccOjb(accessoryObj accOjb) {
		this.accOjb = accOjb;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getSyncid() {
		return syncid;
	}

	public void setSyncid(String syncid) {
		this.syncid = syncid;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(body);
		dest.writeString(op);
		dest.writeString(id);
		dest.writeString(date);
		dest.writeString(syncid);
		dest.writeString(result);
		dest.writeParcelable(accOjb, flags);

	}

	public static final Parcelable.Creator<syncDataItemObject> CREATOR = new Creator<syncDataItemObject>() {
		@Override
		public syncDataItemObject createFromParcel(Parcel source) {
			return new syncDataItemObject(source);
		}

		@Override
		public syncDataItemObject[] newArray(int size) {
			return new syncDataItemObject[size];
		}
	};

	public syncDataItemObject(Parcel in) {

		body = in.readString();
		op = in.readString();
		id = in.readString();
		date = in.readString();
		syncid = in.readString();
		result = in.readString();
		accOjb = in.readParcelable(accessoryObj.class.getClassLoader());

	}

}
