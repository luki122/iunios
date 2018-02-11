package com.android.auroramusic.util;

import android.graphics.Bitmap;
import android.os.Parcelable;

public interface AuroraIListItem extends Parcelable{
	//public int getId();//lory del duplicate 2014.5.20 start 
	public int getIsDownLoadType();
	public long getSongId();
	public long getAlbumId();
	public Bitmap getBitmap();
    public String getTitle();
    public String getAlbumName();
    public String getArtistName();
    public String getUri();
    public String getFilePath();
    public int getM_playlist_order(); // add by chenhl 20140522
    
    public String getAlbumImgUri();
    public String getLrcUri();
    public String getLrcAuthor();
    
    public boolean isAvailable();
    
}
