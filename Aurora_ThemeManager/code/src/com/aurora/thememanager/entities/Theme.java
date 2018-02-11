package com.aurora.thememanager.entities;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.aurora.thememanager.utils.download.DownloadData;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.SparseArray;

/**
 * All theme type of theme that defined in {@link Type} can use this to
 * represent
 * 
 * @author alexluo
 *
 */
public class Theme extends DownloadData implements Parcelable, Comparable<Theme> {

	/**
	 *以下类型分别和从服务器去到的主题的ID按位或，得到的结果保存到
	 *数据库中，在取主题id上传到服务器请求数据时再反过来
	 */
	public static final int TYPE_THEME_PKG = 0x00000000;
	public static final int TYPE_WALLPAPER = 0x00004000;
	public static final int TYPE_RINGTONG = 0x00002000;
	public static final int TYPE_TIME_WALLPAPER = 0x00008000;
	public static final int TYPE_THEME_IMPORT = 0x00010000;
	private static final String KEY_FOR_THEME_KEY = "theme_key";
	/**
	 * name of current theme
	 */
	public String name = "";

	/**
	 * 主题唯一ID
	 */
	public int themeId;
	

	/**
	 * 备用
	 */
	public ThemeKey themeKey;


	/**
	 * 应用后的本地存放路径
	 */
	public String usedPath;

	/**
	 * 主题大小
	 */
	public long size;

	/**
	 * 主题类型（主题包、壁纸、音效、时光锁屏）
	 */
	public int type = -1;

	/**
	 * 主题作者
	 */
	public String author="";
	
	/**
	 * 主题作者头像
	 */
	public String authorIcon;

	/**
	 * 主题版本
	 */
	public String version="";
	

	/**
	 * 主题秒速
	 */
	public String description="";

	/**
	 * 下载次数
	 */
	public String downloadCount;

	/**
	 * 评分
	 */
	public double grade;

	/**
	 *用于表示从网络获取到的预览图的url
	 */
	public String preview;
	/**
	 * 用于表示从本地导入主题包后的预览图的本地路径
	 */
	public String previewPath;
	/**
	 * 用于表示从网络获取的所有预览图的集合
	 */
	public String[] previews;
	/**
	 * 标签，用于分类
	 */
	public String label;
	/**
	 * 大小（字符串形式）
	 */
	public String sizeStr = "";
	
	public long createTime;
	
	/**
	 * 用于保存从本地导入的路径
	 */
	public String importPathName;
	
	public boolean soundEffect = false;
	
	public String hasSoundEffect = "false";
	
	public Theme(){
		
	}

	public Theme(String name,int id,ThemeKey key,String downloadpath,String usedPath,
			long size,String sizeStr,int type,String author,String version,String des,String downloadTime,double grade,String previewPath,String label,
			long createTime,float versionCode,String importPathName,String authorIcon){
		this.name =name;
		this.themeId =id;//in.writeLong();
		this.themeKey = key;
		this.downloadPath =downloadpath;
		this.usedPath = usedPath;
		this.size =size;
		this.type = type;
		this.author =author;
		this.version = version;
		this.description = des;
		this.downloadCount = downloadTime;
		this.grade =grade;
		this.previewPath = previewPath;
		this.label = label;
		this.sizeStr = sizeStr;
		this.createTime = createTime;
		this.versionCode = versionCode;
		this.importPathName = importPathName;
		this.authorIcon = authorIcon;
	}
	public Theme(Parcel in) {
		this.name = in.readString();
		this.themeId = in.readInt();//in.writeLong();
		Bundle b = in.readBundle();
		if(b != null){
			this.themeKey = (ThemeKey) b.get(KEY_FOR_THEME_KEY);
		}
		this.downloadPath = in.readString();
		this.usedPath = in.readString();
		this.size = in.readLong();
		this.type = in.readInt();
		this.author = in.readString();
		this.version = in.readString();
		this.description = in.readString();
		this.downloadCount = in.readString();
		this.grade = in.readDouble();
		this.previewPath = in.readString();
		this.label = in.readString();
		this.sizeStr = in.readString();
		this.previews =in.readStringArray();
		this.createTime = in.readLong();
		
		this.status = in.readInt();
		this.fileDir = in.readString();
		this.fileName = in.readString();
		this.finishTime = in.readLong();
		this.versionCode = in.readFloat();
		this.importPathName = in.readString();
		this.authorIcon = in.readString();
		this.hasSoundEffect = in.readString();
		this.soundEffect = Boolean.parseBoolean(hasSoundEffect);
		this.downloadId = in.readInt();
		this.downloadPath = in.readString();
	}

	public static final class ThemeKey implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 4548713125L;

	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		// TODO Auto-generated method stub
		out.writeString(name);
		out.writeInt( themeId);
		Bundle bundle = new Bundle();
		bundle.putSerializable(KEY_FOR_THEME_KEY, themeKey);
		out.writeBundle(bundle);
		out.writeString(downloadPath);
		out.writeString(usedPath);
		out.writeLong(size);
		out.writeInt(type);
		out.writeString(author);
		out.writeString(version);
		out.writeString(description);
		out.writeString(downloadCount);
		out.writeDouble(grade);
		out.writeString(previewPath);
		out.writeString(label);
		out.writeString(sizeStr);
		out.writeStringArray(previews);
		out.writeLong(createTime);
		
		out.writeInt(this.status);
		out.writeString(this.fileDir);
		out.writeString(this.fileName);
		out.writeLong(this.finishTime);
		out.writeFloat(this.versionCode);
		out.writeString(importPathName);
		out.writeString(authorIcon);
		out.writeString(hasSoundEffect);
		out.writeInt(downloadId);
		out.writeString(downloadPath);
	}

	public static void writeToParcel(Theme theme, Parcel out) {
		if (theme != null) {
			theme.writeToParcel(out, 0);
		} else {
			out.writeString(null);
		}
	}

	public static Theme readFromParcel(Parcel in) {
		return in != null ? new Theme(in) : null;
	}

	@Override
	public int compareTo(Theme arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<Theme> CREATOR = new Parcelable.Creator<Theme>() {
		public Theme createFromParcel(Parcel in) {
			return new Theme(in);
		}

		public Theme[] newArray(int size) {
			return new Theme[size];
		}
	};

	
	public void setTo(Theme newTheme){
		this.name = newTheme.name;
		this.themeId =newTheme.themeId;//in.writeLong();
		this.downloadPath = newTheme.downloadPath;
		this.size = newTheme.size;
		this.type = newTheme.type;
		this.author = newTheme.author;
		this.version =newTheme.version;
		this.description = newTheme.description;
		this.downloadCount = newTheme.downloadCount;
		this.grade = newTheme.grade;
		this.previewPath = newTheme.previewPath;
		this.label = newTheme.label;
		this.sizeStr = newTheme.sizeStr;
		this.previews =newTheme.previews;
		this.createTime = newTheme.createTime;
		
		this.status = newTheme.status;
		this.fileDir = newTheme.fileDir;
		this.fileName =newTheme.fileName;
		this.finishTime = newTheme.finishTime;
		this.versionCode =newTheme.versionCode;
		this.authorIcon = newTheme.authorIcon;
		this.hasSoundEffect = newTheme.hasSoundEffect;
		this.soundEffect = newTheme.soundEffect;
		this.downloadId = newTheme.downloadId;
	}
	
	
	@Override
	public String toString() {
		
		return "name:"+name+"  author:"+author+"  type:"+type
				+"  sizeStr:"+sizeStr+" description:"+description;
	}
	
	public boolean needUpdate(Theme other){
		return this.themeId == other.themeId &&this.name.equals(other.name)
				&&this.author.equals(other.author)&&this.type==other.type
				&&this.versionCode < other.versionCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		Theme other = (Theme)obj;
		if(other == null){
			return false;
		}
		try{
		return this.name.equals(other.name)
				&&this.author.equals(other.author)&&this.type==other.type
				&&this.sizeStr.equals(other.sizeStr)&&this.description.equals(other.description)
				&&this.downloadPath.equals(other.downloadPath)&&this.downloadId == other.downloadId;
		}catch(Exception e){
			
		}
		return false;
	}
	
}
