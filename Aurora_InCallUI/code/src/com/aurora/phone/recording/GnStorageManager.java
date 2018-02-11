/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gionee.os.storage;

import android.os.Environment;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.os.StatFs;
import android.os.storage.IMountService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.os.storage.StorageManager;
import android.content.Context;
import android.os.storage.StorageVolume;

import java.lang.reflect.Method;

import com.android.incallui.InCallApp;
/**
 * StorageManager is the interface to the systems storage service. The storage
 * manager handles storage-related items such as Opaque Binary Blobs (OBBs).
 * <p>
 * OBBs contain a filesystem that maybe be encrypted on disk and mounted
 * on-demand from an application. OBBs are a good way of providing large amounts
 * of binary assets without packaging them into APKs as they may be multiple
 * gigabytes in size. However, due to their size, they're most likely stored in
 * a shared storage pool accessible from all programs. The system does not
 * guarantee the security of the OBB file itself: if any program modifies the
 * OBB, there is no guarantee that a read from that OBB will produce the
 * expected output.
 * <p>
 * Get an instance of this class by calling
 * {@link android.content.Context#getSystemService(java.lang.String)} with an
 * argument of {@link android.content.Context#STORAGE_SERVICE}.
 */

public class GnStorageManager
{
    private static final String TAG = "StorageManager";
    private static final String ICS_STORAGE_PATH_SD1 = "/mnt/sdcard";
    private static final String ICS_STORAGE_PATH_SD2 = "/mnt/sdcard2";
    private static final String STORAGE_PATH_SD1 = "/storage/sdcard0";
    private static final String STORAGE_PATH_SD2 = "/storage/sdcard1";
    /// M: @{
        private static GnStorageManager sMe;
	private StorageManager mstorage;
	private String[] path_canuse;
	public String[] getMountedSDCard()
	{

		StorageVolume[] list=mstorage.getVolumeList();
		ArrayList<String> alsv=new ArrayList<String>();
		if(list!=null)
		{
			for(int i=0;i<list.length;i++)
			{
				String s=mstorage.getVolumeState(list[i].getPath());
				if(s!=null&&s.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
				{
					alsv.add(list[i].getPath());
				}
			}
		}
		
		if(alsv.size()>0)
		{
			path_canuse=new String[alsv.size()];
			for(int i=0;i<path_canuse.length;i++)
				path_canuse[i]=alsv.get(i);
		}
		return path_canuse;
		
	}
	
	public static GnStorageManager getInstance(Context context)
	{
		if(sMe==null)
		{
			sMe=new GnStorageManager(InCallApp.getInstance());
			
		}
		return sMe;
	}
	public GnStorageManager(Context context)
	{
		mstorage=(StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
                path_canuse=getMountedSDCard();
	}
	public String getGnAvailableExternalStoragePath_ex(long requireSize) {

        if(path_canuse==null||path_canuse.length==0)
        	return null;
        for(int i=0;i<path_canuse.length;i++)
        {
            if (Environment.MEDIA_MOUNTED.equals(getVolumeState(path_canuse[i]))) {
                StatFs stat = new StatFs(path_canuse[i]);
                if ((long) stat.getAvailableBlocks()
                        * (long) stat.getBlockSize() > requireSize) {
                    return path_canuse[i];
                }
            }        	
        }
        
        return null;
    }
	
    public static String getVolumeState(String mountPoint) {

		IMountService mMountService;        
        mMountService = IMountService.Stub.asInterface(ServiceManager.getService("mount"));
        if (mMountService == null) {
            Log.e(TAG, "Unable to connect to mount service! - is it running yet?");
            return null;
        }        
        
        try {
            return mMountService.getVolumeState(mountPoint);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get volume state", e);
            return null;
        }
    }
    public boolean isSDExist_ex() {
	   if(path_canuse!=null&&path_canuse.length>0) 
		   return true;
	   return false;
    }
    public String getDefaultPath_ex() {
    	if(path_canuse!=null&&path_canuse.length>0) 
		    return path_canuse[0];
    	return null;
	}    
    
    public File getMTKExternalCacheDir(String packageName) {
        if (null == packageName) {
            return null;
        }
        File mMTKExternalCacheDir=null;
        String path = getDefaultPath();
        if (path.equals(Environment.getExternalStorageDirectory().getPath())) {
            if(android.os.Build.VERSION.SDK_INT<19)
            {
             try{

               Class<?> pc=Class.forName("android.os.Environment");
               Method method=pc.getMethod("getExternalStorageAppCacheDirectory",String.class);
               mMTKExternalCacheDir=(File)method.invoke(null,packageName);  
               }catch(Exception e){}
            }
            else
            {
              try{

               Class<?> pc=Class.forName("android.os.Environment");
               Method method=pc.getMethod("buildExternalStorageAppCacheDirs",String.class);
               mMTKExternalCacheDir=((File[])method.invoke(null,packageName))[0];  
               }catch(Exception e){}
            }
        if (mMTKExternalCacheDir!=null&&!mMTKExternalCacheDir.exists()) {
                try {
                     File dir=null;
                      if(android.os.Build.VERSION.SDK_INT<19)
			    {
			     try{

			       Class<?> pc=Class.forName("android.os.Environment");
			       Method method=pc.getMethod("getExternalStorageAndroidDataDir");
			       dir=(File)method.invoke(null);  
			       }catch(Exception e){}
			    }
			    else
			    {
			      try{

			       Class<?> pc=Class.forName("android.os.Environment");
			       Method method=pc.getMethod("buildExternalStorageAndroidDataDirs");
			       dir=((File[])method.invoke(null))[0];  
			       }catch(Exception e){}
			    }   
                    (new File(dir,
                            ".nomedia")).createNewFile();
                } catch (IOException e) {
                    // do nothing
                }
                if (!mMTKExternalCacheDir.mkdirs()) {
                    return null;
                }
            }
        } else {
            mMTKExternalCacheDir = new File(new File(new File(new File(new File(path), 
                "Android"), "data"), packageName), "cache");
            if (!mMTKExternalCacheDir.exists()) {
                try {
                    (new File(new File(new File(new File(path),
                    "Android"), "data"), ".nomedia")).createNewFile();
                } catch (IOException e) {
                    // do nothing
                }
                if (!mMTKExternalCacheDir.mkdirs()) {
                    return null;
                }
            }
        }
        return mMTKExternalCacheDir;
    }    
    public String getSdCardPath(int index)
    {
    	if(path_canuse==null||path_canuse.length==0||index>=path_canuse.length)
    		return null;
    	return path_canuse[index];
    }
	public int getSdCardNum()
	{
		if(path_canuse==null||path_canuse.length==0)
			return 0;
		return path_canuse.length;
	}
        public String getInternalStoragePath()
       {
           if(path_canuse==null||path_canuse.length==0)
                return null;
         //  return  path_canuse[0];
         String s=path_canuse[0];//android.os.SystemProperties.get("ro.internal.storage");
         if(s==null||s.length()==0)
             return null;
         String state="";
         try{    
		    state=mstorage.getVolumeState(s);
		 }catch(Exception e){return null;}
		if(state!=null&&state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
		{
			return s;
		}
		else
		{
            return null;
		}

       }
        public String getExternalStoragePath()
       {
           if(path_canuse==null||path_canuse.length<2)
                return null;
           //return  path_canuse[1];
          String s=path_canuse[1];//android.os.SystemProperties.get("ro.external.storage");
         if(s==null||s.length()==0)
             return null;
        String state="";
        try{     
		       state=mstorage.getVolumeState(s);
		     }catch(Exception e){return null;}
		if(state!=null&&state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
		{
			return s;
		}
		else
		{
            return null;
		}		 

       }   
    public static String getGnAvailableExternalStoragePath(long requireSize) {
        if (Environment.MEDIA_MOUNTED.equals(getVolumeState(STORAGE_PATH_SD1))) {
            StatFs stat = new StatFs(STORAGE_PATH_SD1);
            if ((long) stat.getAvailableBlocks()
                    * (long) stat.getBlockSize() > requireSize) {
                return STORAGE_PATH_SD1;
            }
        }

        if (Environment.MEDIA_MOUNTED.equals(getVolumeState(STORAGE_PATH_SD2))) {
            StatFs stat2 = new StatFs(STORAGE_PATH_SD2);
            if ((long) stat2.getAvailableBlocks()
                    * (long) stat2.getBlockSize() > requireSize) {
                return STORAGE_PATH_SD2;
            }
        }
        return null;
    }

    /**
     * reture true if there is external sd card in device and ALREADY SWAP.
     */
    public static boolean isSDExist() {
	return false;
    }

    /**
    * Returns default path for writing.
    */
	public static String getDefaultPath() {
		return STORAGE_PATH_SD1;
	}        
}
