
package com.aurora.iunivoice.activity.account;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.aurora.iunivoice.IuniVoiceApp;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.http.data.HttpRequestGetAccountData;
import com.aurora.iunivoice.utils.AccountPreferencesUtil;
import com.aurora.iunivoice.utils.BitmapUtil;
import com.aurora.iunivoice.utils.Globals;
import com.aurora.iunivoice.utils.SystemUtils;

/**
 * 图像加载工具类
 * @author JimXia
 *
 * @param <T>
 * @date 2014年10月18日 上午11:23:58
 */
public abstract class PhotoLoader<T> extends AsyncTask<String, Integer, Bitmap> {
    private static final String TAG = "PhotoLoader";
    
    protected WeakReference<T> mTarget;
    protected int mErrorMsgResId = -1;

    public PhotoLoader(T target) {
        mTarget = new WeakReference<T>(target);
    }
    
    private static String getPhotoFileName() {
        return "."+System.currentTimeMillis()+".jpg";
    }
    
    private static String getFinalCropPhotoFileName() {
        return "."+AccountPreferencesUtil.getInstance(IuniVoiceApp.getInstance()).getUserID() + ".jpg";
    }
    
    private static String getCropPhotoFileName() {
        return "."+System.currentTimeMillis()+".jpg";
    }
    
    private static String getPickPhotoKitkatFileName() {
    	return "."+System.currentTimeMillis()+".jpg";
    }
    
    public static File getFinalIconFile() {
        if (!Globals.PHOTO_DIR.exists()) {
            Globals.PHOTO_DIR.mkdirs();
        }
        return new File(Globals.PHOTO_DIR, getFinalCropPhotoFileName());
    }
    
    public static File getPhotoFile() {
        if (!Globals.PHOTO_DIR.exists()) {
            Globals.PHOTO_DIR.mkdirs();
        }
        return new File(Globals.PHOTO_DIR, getPhotoFileName());
    }
    
    public static File getCropFile() {
        if (!Globals.PHOTO_DIR.exists()) {
            Globals.PHOTO_DIR.mkdirs();
        }
        return new File(Globals.PHOTO_DIR, getCropPhotoFileName());
    }
    
    public static File getPickPhotoKitkatFile() {
    	if (!Globals.PHOTO_DIR.exists()) {
            Globals.PHOTO_DIR.mkdirs();
        }
        return new File(Globals.PHOTO_DIR, getPickPhotoKitkatFileName());
    }
    
    /**
     * 加载本地缓存的图像
     * @return
     */
    public static Bitmap loadLocalCacheIcon() {
        File iconFile = getFinalIconFile();
        AccountPreferencesUtil pref = AccountPreferencesUtil.getInstance(IuniVoiceApp.getInstance());
        if (iconFile.exists()) {
            Bitmap src = BitmapFactory.decodeFile(iconFile.getAbsolutePath());
            if (src != null) {
                return BitmapUtil.clipCircleBitmap(src, src.getWidth());
            } else {
                // 加载失败，清空缓存，加载完用户信息后会再次加载图像
                pref.setLastUsedPhoneURL("");
            }
        } else {
            // 本地缓存的文件不存在，清空缓存的URL地址，以便重新下载图像
            pref.setLastUsedPhoneURL("");
        }
        
        return null;
    }
    
    public void loadLatestPhoto() {
        AccountPreferencesUtil pref = AccountPreferencesUtil.getInstance(IuniVoiceApp.getInstance());
        String newPhotoUrl = pref.getUserPhotoURL();
        String oldPhotoUrl = pref.getLastUsedPhotoURL();
        Log.d(TAG, "Jim, new photo url: " + newPhotoUrl + ", old photo url: " + oldPhotoUrl);
        if (!TextUtils.isEmpty(newPhotoUrl) && !newPhotoUrl.equalsIgnoreCase(oldPhotoUrl) &&
                SystemUtils.isNetworkConnected()) {
            execute(newPhotoUrl);
        }
    }

    @Override
    protected final Bitmap doInBackground(String... params) {
        AccountPreferencesUtil pref = AccountPreferencesUtil.getInstance(IuniVoiceApp.getInstance());
        if (SystemUtils.isNetworkConnected()) {
            File photoFile = getFinalIconFile();
            String photoUrl = params[0];
            try {
                File tmpPhotoFile = getPhotoFile();
                if (tmpPhotoFile.exists()) {
                    tmpPhotoFile.delete();
                }
                FileOutputStream fos = new FileOutputStream(tmpPhotoFile);
                boolean result = HttpRequestGetAccountData.downloadPhoto(photoUrl,
                        pref.getUserID(), pref.getUserKey(), fos);
                if (result) {
                    if (photoFile.exists()) {
                        photoFile.delete();
                    }
                    tmpPhotoFile.renameTo(photoFile);
                    if (photoFile.exists()) {
                    	
                        pref.setLastUsedPhoneURL(pref.getUserPhotoURL());
                        Bitmap src = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                        if (src != null) {
                            return BitmapUtil.clipCircleBitmap(src, src.getWidth());
                        } else {
                            mErrorMsgResId = R.string.account_info_error_load_icon_falied;
                        }
                    } else {
                        mErrorMsgResId = R.string.account_info_error_download_icon_falied;
                    }
                } else {
                    mErrorMsgResId = R.string.account_info_error_download_icon_falied;
                }
            } catch (Exception e) {
                mErrorMsgResId = R.string.account_info_error_download_icon_falied;
            }
        }

        return null;
    }
    
    @Override
    protected abstract void onPostExecute(Bitmap result);
}
