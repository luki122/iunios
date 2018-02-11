package com.android.auroramusic.util;

import android.text.TextUtils;

public class Artist {

	/**
     * 歌手ID
     */
    public long mArtistId;

    /**
     * 歌手名字
     */
    public String mArtistName;

    /**
     * 专辑数目
     */
    public int mAlbumNumber;

    /**
     * 歌曲数目
     */
    public int mSongNumber;
    /**
     * 歌手中文拼音
     */
    public String mPinyin;

    /**
     * @param artistId 歌手ID
     * @param artistName 歌手名字
     * @param songNumber 歌曲数目
     * @param albumNumber 专辑数目
     */
    public Artist(final long artistId, final String artistName, final int songNumber,
            final int albumNumber,String pinyin) {
        super();
        mArtistId = artistId;
        mArtistName = artistName;
        mSongNumber = songNumber;
        mAlbumNumber = albumNumber;
        mPinyin = pinyin;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mAlbumNumber;
        result = prime * result + (int) mArtistId;
        result = prime * result + (mArtistName == null ? 0 : mArtistName.hashCode());
        result = prime * result + mSongNumber;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Artist other = (Artist)obj;
        if (mAlbumNumber != other.mAlbumNumber) {
            return false;
        }
        if (mArtistId != other.mArtistId) {
            return false;
        }
        if (!TextUtils.equals(mArtistName, other.mArtistName)) {
            return false;
        }
        if (mSongNumber != other.mSongNumber) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return mArtistName;
    }
}
