package com.aurora.thememanager.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.Context;

import com.aurora.thememanager.ThemeManagerApplication;

public class ThemeCacheUtils {

	/**
	 * Cache object into local
	 * @param ser  
	 * 				need to cached object
	 * @param file
	 * 					object name
	 * @param context
	 * @return  Cached success or not
	 */
	public static boolean saveObject(Serializable ser, String file,Context context) {
	    FileOutputStream fos = null;
	    ObjectOutputStream oos = null;
	    try {
	        fos = ThemeManagerApplication.getInstance(context).openFileOutput(file, Context.MODE_PRIVATE);
	        oos = new ObjectOutputStream(fos);
	        oos.writeObject(ser);
	        oos.flush();
	        return true;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    } finally {
	        try {
	            oos.close();
	        } catch (Exception e) {
	        }
	        try {
	            fos.close();
	        } catch (Exception e) {
	        }
	    }
	}
	
	
	
	/**
	 *Read cached object from local
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static Serializable readObject(String file,Context context) {
	    FileInputStream fis = null;
	    ObjectInputStream ois = null;
	    try {
	        fis =ThemeManagerApplication.getInstance(context).openFileInput(file);
	        ois = new ObjectInputStream(fis);
	        return (Serializable) ois.readObject();
	    } catch (FileNotFoundException e) {
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            ois.close();
	        } catch (Exception e) {
	        }
	        try {
	            fis.close();
	        } catch (Exception e) {
	        }
	    }
	    return null;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
