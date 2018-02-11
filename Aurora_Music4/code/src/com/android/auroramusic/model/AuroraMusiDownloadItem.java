package com.android.auroramusic.model;

public class AuroraMusiDownloadItem {

	private long songId;
	private String artistname;
	private String albumname;
	private String title;
	private String imgurl;
	private int status;
	private long totalbytes;
	private long currentbytes;
	private String filepath;
	private String bitrate;
	
	public AuroraMusiDownloadItem() {

	}

	public AuroraMusiDownloadItem(long id, String title, String artist,
			String album, String url, int status,long total,long current,String path) {
		this.songId = id;
		this.artistname = artist;
		this.albumname = album;
		this.title = title;
		this.imgurl = url;
		this.status = status;
		this.totalbytes=total;
		this.currentbytes=current;
		this.filepath=path;
	}

	public AuroraMusiDownloadItem(long id, String title, String artist,
			String album, String url, int status,String bitrate,String path) {
		this.songId = id;
		this.artistname = artist;
		this.albumname = album;
		this.title = title;
		this.imgurl = url;
		this.status = status;
		this.bitrate=bitrate;
		this.filepath=path;
	}
	
	public String getBitrate() {
		return bitrate;
	}

	public void setBitrate(String bitrate) {
		this.bitrate = bitrate;
	}

	public long getTotalbytes() {
		return totalbytes;
	}

	public void setTotalbytes(long totalbytes) {
		this.totalbytes = totalbytes;
	}

	public long getCurrentbytes() {
		return currentbytes;
	}

	public void setCurrentbytes(long currentbytes) {
		this.currentbytes = currentbytes;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	@Override
	public String toString() {
		return "AuroraMusiDownloadItem [songId=" + songId + ", artistname="
				+ artistname + ", albumname=" + albumname + ", title=" + title
				+ ", imgurl=" + imgurl + ", status=" + status + ", totalbytes="
				+ totalbytes + ", currentbytes=" + currentbytes + ", filepath="
				+ filepath + ", bitrate=" + bitrate + "]";
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getImgurl() {
		return imgurl;
	}

	public void setImgurl(String imgurl) {
		this.imgurl = imgurl;
	}

	public long getSongId() {
		return songId;
	}

	public void setSongId(long songId) {
		this.songId = songId;
	}

	public String getArtistname() {
		return artistname;
	}

	public void setArtistname(String artistname) {
		this.artistname = artistname;
	}

	public String getAlbumname() {
		return albumname;
	}

	public void setAlbumname(String albumname) {
		this.albumname = albumname;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
