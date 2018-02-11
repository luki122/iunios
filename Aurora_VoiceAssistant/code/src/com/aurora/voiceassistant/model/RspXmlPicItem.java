package com.aurora.voiceassistant.model;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;


public class RspXmlPicItem implements Parcelable
{
	private int height;
	private int width;
	private int size ;
	
	private String name;
	private String type;
	private String imagelink;
	private String locimagelink;
	private Bitmap bitmap;
	//private byte[] byteDraw;
	
	
	public int getHeight() 
	{
		return height;
	}
	public void setHeight(int height) 
	{
		this.height = height;
	}
	public int getWidth() 
	{
		return width;
	}
	public void setWidth(int width)
	{
		this.width = width;
	}
	public int getSize() 
	{
		return size;
	}
	public void setSize(int size) 
	{
		this.size = size;
	}
	public String getName() 
	{
		return name;
	}
	public void setName(String name) 
	{
		this.name = name;
	}
	public String getType() 
	{
		return type;
	}
	public void setType(String type) 
	{
		this.type = type;
	}
	public String getImagelink() 
	{
		return imagelink;
	}
	public void setImagelink(String imagelink) 
	{
		this.imagelink = imagelink;
	}
	public String getLocimagelink() 
	{
		return locimagelink;
	}
	public void setLocimagelink(String locimagelink) 
	{
		this.locimagelink = locimagelink;
	}
	
	public Bitmap getBitmap() 
	{
		return bitmap;
	}
	
	public void setBitmap(Bitmap bitmap) 
	{
		this.bitmap = bitmap;
	}
	
	@Override
	public int describeContents() 
	{
		return 0;
	}

	
	public static final Parcelable.Creator<RspXmlPicItem> CREATOR  = new Creator<RspXmlPicItem>(){
		@Override
		public RspXmlPicItem createFromParcel(Parcel source) 
		{
			RspXmlPicItem app=  new RspXmlPicItem();
			
			app.height = source.readInt();
			app.width = source.readInt();
			app.size =  source.readInt();
			app.name = source.readString();
			app.type = source.readString();
			app.imagelink = source.readString();
			app.locimagelink = source.readString();
			
			//source.readByteArray(app.byteDraw);
			//app.setBitmap(getBitmap(app.byteDraw));
			  
			return app;
		}
		@Override
		public RspXmlPicItem[] newArray(int size) 
		{
			return new RspXmlPicItem[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) 
	{
		// TODO Auto-generated method stub
		dest.writeInt(height);
		dest.writeInt(width);
		dest.writeInt(size);
		
		dest.writeString(name);
		dest.writeString(type);
		dest.writeString(imagelink);
		dest.writeString(locimagelink);
		
		//byteDraw = getBytes(bitmap);
		//dest.writeByteArray(byteDraw);
	}
	
	/*
	private static Bitmap getBitmap(byte[] data) 
	{
		return BitmapFactory.decodeByteArray(data, 0, data.length);
	}
			 
	private byte[] getBytes(Bitmap bitmap) 
	{
		ByteArrayOutputStream baops = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 0, baops);
		return baops.toByteArray();
	}*/
	
}
