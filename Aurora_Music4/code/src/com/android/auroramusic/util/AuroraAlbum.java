package com.android.auroramusic.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

/**
 * @author liumx 单张专辑的信息
 */
public class AuroraAlbum extends AuroraListItem implements Parcelable {

	private long albumId;
	private String albumName;
	private String artistName;
	private int trackNumber;
	private String albumArt;
	private String releaseDate;
	
	private int type = 0;// 0:表示local music；1:表示net download music；

	public AuroraAlbum() {
	}

	public AuroraAlbum(long albumId, String albumName) {
		this.albumId = albumId;
		this.albumName = albumName;
	}

	public AuroraAlbum(long albumId, String albumName, String artistName,
			int trackNumber, String albumArt, String releaseDate) {
		super();
		this.albumId = albumId;
		this.albumName = albumName;
		this.artistName = artistName;
		this.trackNumber = trackNumber;
		this.albumArt = albumArt;
		this.releaseDate = releaseDate;
	}
	
	public AuroraAlbum(Parcel in) {
		albumId = in.readLong();
		albumName = in.readString();
		artistName = in.readString();
		trackNumber = in.readInt();
		albumArt = in.readString();
		releaseDate = in.readString();
		
		type = in.readInt();
	}
	
	public  static final Parcelable.Creator<AuroraAlbum> CREATOR = new Creator<AuroraAlbum>() {  
        @Override  
        public AuroraAlbum createFromParcel(Parcel source) {  
        	return new AuroraAlbum(source);
        }  
        @Override  
        public AuroraAlbum[] newArray(int size) {  
            return new AuroraAlbum[size];  
        }  
    };

	public long getAlbumId() {
		return albumId;
	}

	public void setAlbumId(long albumId) {
		this.albumId = albumId;
	}

	public String getAlbumName() {
		return albumName;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	public int getTrackNumber() {
		return trackNumber;
	}

	public void setTrackNumber(int trackNumber) {
		this.trackNumber = trackNumber;
	}

	public String getAlbumArt() {
		return albumArt;
	}

	public void setAlbumArt(String albumArt) {
		this.albumArt = albumArt;
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	@Override
	public long getSongId() {
		return -1;
	}

	@Override
	public String toString() {
		return "AuroraAlbum [albumId=" + albumId + ", albumName=" + albumName
				+ ", artistName=" + artistName + ", trackNumber=" + trackNumber
				+ ", albumArt=" + albumArt + ", releaseDate=" + releaseDate
				+ "]";
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.albumId);
		dest.writeString(this.albumName);
		dest.writeString(this.artistName);
		dest.writeInt(this.trackNumber);
		dest.writeString(this.albumArt);
		dest.writeString(this.releaseDate);
		dest.writeInt(this.type);
		
	}
}
