package com.aurora.change.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.aurora.change.receiver.ChangeReceiver;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

public class CommonUtil {
	
	//shigq add start
	public static enum NetWorkType {
		NO_NET, MOBILE_ONLY, WIFI
	};
	//shigq add end

    private static final CommonLog log = LogFactory.createLog();

    public static boolean hasSDCard() {
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            return false;
        }
        return true;
    }

    public static String getRootFilePath() {
        if (hasSDCard()) {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/";// filePath:/sdcard/
        } else {
            return Environment.getDataDirectory().getAbsolutePath() + "/data/"; // filePath: /data/data/
        }
    }

    public static boolean checkNetState(Context context) {
        boolean netstate = false;
        ConnectivityManager connectivity = ( ConnectivityManager ) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        netstate = true;
                        break;
                    }
                }
            }
        }
        return netstate;
    }

    public static void showToast(Context context, String tip) {
        Toast.makeText(context, tip, Toast.LENGTH_SHORT).show();
    }

    public static int getScreenWidth(Context context) {
        WindowManager manager = ( WindowManager ) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getWidth();
    }

    public static int getScreenHeight(Context context) {
        WindowManager manager = ( WindowManager ) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getHeight();
    }

    public static boolean chmodFile(String path) {
        if (Build.VERSION.SDK_INT > 16) {
            Process p = null;
            try {
                p = Runtime.getRuntime().exec("chmod 777 " + path);
                int status = p.waitFor();
                if (status == 0) {
                	// chmod succeed
                    return true;
                } else {
                    // chmod failed
                	Log.d(ChangeReceiver.LOCK_TAG, "chmodFile status = : "+status);
                    return false;
                }
            } catch (IOException e) {
            	Log.d(ChangeReceiver.LOCK_TAG, "chmodFile IOException: "+e.toString());
                try {
                    p = Runtime.getRuntime().exec("chmod 777 " + path);
                    int status = p.waitFor();
                    if (status == 0) {
                    	// chmod succeed
                        return true;
                    } else {
                        // chmod failed
                    	Log.d(ChangeReceiver.LOCK_TAG, "2-->chmodFile status = : "+status);
                        return false;
                    }
                } catch (IOException e2) {
                	Log.d(ChangeReceiver.LOCK_TAG, "2-->chmodFile IOException: "+e2.toString());
                	return false;
                } catch (InterruptedException e3) {
                	Log.d(ChangeReceiver.LOCK_TAG, "2-->chmodFile InterruptedException: "+e3.toString());
                    return false;
                } catch (Exception e4) {
                	Log.d(ChangeReceiver.LOCK_TAG, "2-->chmodFile Exception: "+e4.toString());
                    return false;
                }
            } catch (InterruptedException e) {
            	Log.d(ChangeReceiver.LOCK_TAG, "chmodFile InterruptedException: "+e.toString());
                return false;
            } catch (Exception e) {
            	Log.d(ChangeReceiver.LOCK_TAG, "chmodFile Exception: "+e.toString());
                return false;
            } finally{ 
                if (p != null) p.destroy();   
            } 
        } else {
            return true;
        }
    }
    
    //shigq add start
    public static String getCurrentTime() {
    	String tempDate = new SimpleDateFormat("yyyyMMdd HH:mm").format(System.currentTimeMillis());
    	return tempDate;
    }
    
    public static String getDateFromCurrent(int delta) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(new Date());
    	calendar.add(Calendar.DAY_OF_MONTH, delta);
//    	Date lastMonth = calendar.getTime();
    	return new SimpleDateFormat("yyyyMMdd").format(calendar.getTimeInMillis());
    }
    
    public static String messageDiestBuilder(String str) {
    	MessageDigest md5;
    	StringBuffer resultBuffer = new StringBuffer();
		try {
			md5 = MessageDigest.getInstance("MD5");
			md5.reset();
			md5.update(str.getBytes());
	        byte[] byteArray = md5.digest();
	        
	        for (int i = 0; i < byteArray.length; i++) {
				if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
					resultBuffer.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
				} else {
					resultBuffer.append(Integer.toHexString(0xFF & byteArray[i]));       
	      		}
			}
	        
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			Log.d("Wallpaper_DEBUG", "CommonUtil---------messageDiestBuilder----------NoSuchAlgorithmException = "+e);
		}
		
    	return resultBuffer.toString();
    }
    
    public static NetWorkType getNetWorkType(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        //M:shigq get the state of network on Android5.0 platform begin
        if(VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
        	NetworkInfo info = connMgr.getActiveNetworkInfo();
            
            if (info == null) {
            	Log.d("Wallpaper_DEBUG", "CommonUtil---------getActiveNetworkInfo----------NetWorkType.NO_NET");
            	return NetWorkType.NO_NET;
			} else if (ConnectivityManager.TYPE_WIFI == info.getType()) {
				Log.d("Wallpaper_DEBUG", "CommonUtil---------getActiveNetworkInfo----------NetWorkType.WIFI");
				return NetWorkType.WIFI;
			} else if (ConnectivityManager.TYPE_MOBILE == info.getType()) {
				Log.d("Wallpaper_DEBUG", "CommonUtil---------getActiveNetworkInfo----------NetWorkType.MOBILE_ONLY");
				return NetWorkType.MOBILE_ONLY;
			}
        }
        //M:shigq get the state of network on Android5.0 platform end

        final android.net.NetworkInfo wifi =connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile =connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);	//mobile.getState() == NetworkInfo.State.CONNECTED
//        Log.d("Wallpaper_DEBUG", "CommonUtil---------isWiFiConnected----------wifi = "+wifi.isAvailable());
//        Log.d("Wallpaper_DEBUG", "CommonUtil---------isWiFiConnected----------mobile.getState = "+mobile.getState());
        
        if (!wifi.isAvailable() && mobile.getState() == NetworkInfo.State.DISCONNECTED) {
			return NetWorkType.NO_NET;
		} else if (!wifi.isAvailable() && mobile.getState() == NetworkInfo.State.CONNECTED) {
			return NetWorkType.MOBILE_ONLY;
		} else if (wifi.isAvailable()) {
			return NetWorkType.WIFI;
		} else {
			return NetWorkType.NO_NET;
		}
    }
    
    public static ContentValues getContentValues(Context context, Uri sourceUri, File file, long time, int width, int height) {
		final ContentValues values = new ContentValues();
		
		//time /= 1000;
		values.put(Images.Media.TITLE, file.getName());
		values.put(Images.Media.DISPLAY_NAME, file.getName());
		values.put(Images.Media.MIME_TYPE, "image/jpeg");
		values.put(Images.Media.DATE_TAKEN, time);
		values.put(Images.Media.DATE_MODIFIED, time);
		values.put(Images.Media.DATE_ADDED, time);
		values.put(Images.Media.ORIENTATION, 0);
		values.put(Images.Media.DATA, file.getAbsolutePath());
		values.put(Images.Media.SIZE, file.length());
		
		//Aurora <SQF> <2014-6-13>  for NEW_UI begin
		if(width != 0) values.put(Images.Media.WIDTH, width);
		if(height != 0) values.put(Images.Media.HEIGHT, height);
		//Aurora <SQF> <2014-6-13>  for NEW_UI end
		
		// This is a workaround to trigger the MediaProvider to re-generate the
		// thumbnail.
		values.put(Images.Media.MINI_THUMB_MAGIC, 0);
		
		/*final String[] projection = new String[] {
				ImageColumns.DATE_TAKEN,
				ImageColumns.LATITUDE, 
				ImageColumns.LONGITUDE,
		};
		
		querySource(context, sourceUri, projection, new ContentResolverQueryCallback() {
			@Override
			public void onCursorResult(Cursor cursor) {
				//paul del
				//values.put(Images.Media.DATE_TAKEN, cursor.getLong(0));
				
				double latitude = cursor.getDouble(1);
				double longitude = cursor.getDouble(2);
				Log.d("Wallpaper_DEBUG", "NextDayLoadingPictureTask---------------latitude1 = "+latitude);
				Log.d("Wallpaper_DEBUG", "NextDayLoadingPictureTask---------------longitude2 = "+longitude);
				// TODO: Change || to && after the default location
				// issue is fixed.
				if ((latitude != 0f) || (longitude != 0f)) {
					values.put(Images.Media.LATITUDE, latitude);
					values.put(Images.Media.LONGITUDE, longitude);
					Log.d("Wallpaper_DEBUG", "NextDayLoadingPictureTask---------------latitude2 = "+latitude);
					Log.d("Wallpaper_DEBUG", "NextDayLoadingPictureTask---------------longitude2 = "+longitude);
				}
			}
		});*/
		return values;
	}
    
    public static void querySource(Context context, Uri sourceUri, String[] projection, ContentResolverQueryCallback callback) {
        ContentResolver contentResolver = context.getContentResolver();
        querySourceFromContentResolver(contentResolver, sourceUri, projection, callback);
    }

    private static void querySourceFromContentResolver(
            ContentResolver contentResolver, Uri sourceUri, String[] projection,
            ContentResolverQueryCallback callback) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(sourceUri, projection, null, null, null);
            Log.d("Wallpaper_DEBUG", "NextDayLoadingPictureTask---------------cursor = "+cursor);
            if ((cursor != null) && cursor.moveToNext()) {
                callback.onCursorResult(cursor);
            }
        } catch (Exception e) {
            // Ignore error for lacking the data column from the source.
        	Log.d("Wallpaper_DEBUG", "NextDayLoadingPictureTask---------------Exception = "+e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.d("Wallpaper_DEBUG", "NextDayLoadingPictureTask---------------cursor1 = "+cursor);
    }
    
    public interface ContentResolverQueryCallback {
        void onCursorResult(Cursor cursor);
    }
    
    public static boolean copyFile(String fromPath, String toPath) {
    	File fromFile = new File(fromPath);
    	if (!fromFile.exists()) return false;
    	
    	File toFile = new File(toPath);
    	if (toFile.exists()) return false;
        
        try {
            FileInputStream inputStream = new FileInputStream(fromFile);
            FileOutputStream outputStream = new FileOutputStream(toFile);
            
            byte[] bt = new byte[1024];
            int c;
            while ((c = inputStream.read(bt)) > 0) {
            	outputStream.write(bt,0,c);
            }
            //close input and output stream
            inputStream.close();
            outputStream.close();
            
        } catch (FileNotFoundException e) {
        	// TODO Auto-generated catch block
        	Log.d("Wallpaper_DEBUG", "copyFile---------------FileNotFoundException = "+e);
        } catch (IOException e) {
        	// TODO Auto-generated catch block
        	Log.d("Wallpaper_DEBUG", "copyFile---------------IOException = "+e);
        }
        return true;
    }
    //shigq add end
}
