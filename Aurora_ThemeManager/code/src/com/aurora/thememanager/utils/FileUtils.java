package com.aurora.thememanager.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.aurora.thememanager.entities.Theme;
import com.aurora.utils.Utils2Icon;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class FileUtils extends android.os.FileUtils{
	private static final String THEME_DOWNLOAD_PATH = ".IUNI_Theme/download/";
	private static final String TAG = "FileUtils";
	private static final boolean DBG = true;
	
	private static  String[] mThemeDirs = {ThemeConfig.THEME_BASE_PATH,ThemeConfig.THEME_PATH
			,ThemeConfig.THEME_BACKUP_PATH,ThemeConfig.THEME_AUDIO_PATH
			/*,Config.THEME_FONTS_PATH*/,ThemeConfig.THEME_BOOT_PATH
			,ThemeConfig.THEME_WALL_PAPAER_PATH,ThemeConfig.THEME_LOCKSCREEN_PATH};
	
	public static boolean externalStorageMounted(){
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}
	 public static List<File> getFileListByDirPath(String path) {
	        File directory = new File(path);
	        List<File> resultFiles = new ArrayList<>();

	        // TODO: filter here
	        File[] files = directory.listFiles();
	        if(files != null && files.length > 0) {
	            for(File f : files) {
	                if(f.isHidden()) {
	                    continue;
	                }
					if(f.isDirectory()){
						File[] ch = f.listFiles();
						if(ch == null || ch.length <1){
							continue;
						}
					}
				    resultFiles.add(f);
	                
	            }

	            Collections.sort(resultFiles, new FileComparator());
	        }

	        return resultFiles;
	    }

	    public static String cutLastSegmentOfPath(String path) {
	        return path.substring(0, path.lastIndexOf("/"));
	    }

	    public static String getReadableFileSize(long size) {
	        if (size <= 0) return "0";
	        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
	        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
	        return new DecimalFormat("#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	    }
	public static String getPath(Context context, Uri uri) {
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = { "_data" };
			Cursor cursor = null;
			try {
				cursor = context.getContentResolver().query(uri, projection,
						null, null, null);
				int column_index = cursor.getColumnIndexOrThrow("_data");
				if (cursor.moveToFirst()) {
					return cursor.getString(column_index);
				}
			} catch (Exception e) {
				// Eat it
			}
		}
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	public static  boolean createFileUncheckedStorage(String newFilePath,boolean isDir){
		File file = new File(newFilePath);
		if(!file.exists()){
			if(isDir){
				file.mkdirs();
				Log.d(TAG, "createFile in-->"+newFilePath);
			}else{
				try {
					file.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return file.exists();
	}
	
	
	public static boolean createThemeDownloadDir(){
		if(externalStorageMounted()){
			File storageDir = Environment.getExternalStorageDirectory();
			if(storageDir.exists()){
				String path = storageDir.getAbsolutePath() +File.separator+THEME_DOWNLOAD_PATH;
				if(DBG)Log.d(TAG,"storage path-->"+path);
				return createFileUncheckedStorage(path, true);
			}
		}
		return false;
	}
	
	
	
	
	/**
	 * copy single file to target file
	 * @param sourceFile
	 * @param targetFile
	 * @throws IOException
	 */
	 public static void copySingleFile(File sourceFile,File targetFile)   
			throws IOException {
		FileInputStream input = new FileInputStream(sourceFile);
		BufferedInputStream inBuff = new BufferedInputStream(input);

		FileOutputStream output = new FileOutputStream(targetFile);
		BufferedOutputStream outBuff = new BufferedOutputStream(output);

		byte[] b = new byte[1024 * 5];
		int len;
		while ((len = inBuff.read(b)) != -1) {
			outBuff.write(b, 0, len);
		}
		outBuff.flush();

		inBuff.close();
		outBuff.close();
		output.close();
		input.close();
	}  
	 
	 /**
	  * copy directory to taget
	  * @param sourceDir
	  * @param targetDir
	  * @throws IOException
	  */
  public static void copyDirectiory(String sourceDir, String targetDir)  
			throws IOException {
		(new File(targetDir)).mkdirs();
		File[] file = (new File(sourceDir)).listFiles();
		for (int i = 0; i < file.length; i++) {
			if (file[i].isFile()) {
				File sourceFile = file[i];
				File targetFile = new File(
						new File(targetDir).getAbsolutePath() + File.separator
								+ file[i].getName());
				copySingleFile(sourceFile, targetFile);
			}
			if (file[i].isDirectory()) {
				String dir1 = sourceDir + "/" + file[i].getName();
				String dir2 = targetDir + "/" + file[i].getName();
				copyDirectiory(dir1, dir2);
			}
		}
	} 
	
	public static void createThemeDirs(){
		int length = mThemeDirs.length;
			for(int i = 0;i<length;i++){
				File f = new File(mThemeDirs[i]);
				if(!f.exists()){
					boolean success = f.mkdir();
					
				}
			}

			changeThemePermission();
	}
  
	
	public static void setPermission(String permission, String path) {
		String command = null;
		Process p;
		try {
			command = "chmod -R " + permission + " " + path;
			p = Runtime.getRuntime().exec(command);
			int status = p.waitFor();
			if (status == 0) {    
				Log.i(TAG,"exec "+command+" succeed.");
			} else {    
				Log.i(TAG,"exec "+command+" failed.");   
			}   
		} catch (IOException e) {
			Log.i(TAG,"chmod IOException.");
		} catch (InterruptedException e) {
			Log.i(TAG,"chmod InterruptedException.");
		}
	}
	
	
	/**
	 * backup the when apply new theme,if new theme apply failure,
	 * we should reuse it
	 * @param themes
	 * @return
	 */
	public static  boolean backupCurrentTheme(Theme... themes) {
		// TODO Auto-generated method stub
		Theme theme = themes[0];
		try{
			FileUtils.copyDirectiory(theme.usedPath,ThemeConfig.THEME_BACKUP_PATH);
			return true;
			}catch(Exception ex){
				Log.d(TAG, "apply theme catched exception-->"+ex);
			}
		return false;
		
	}
	
	public static boolean deleteFiles(File dir) {
		if(dir==null||(dir!=null&&!dir.exists()))return false;
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
            	File child = new File(dir, children[i]);
            	if(child.exists()){
            		
            		 boolean success = deleteFiles(child);
                     if (!success) {
                         return false;
                     }
            	}
               
            }
        }
        // 目录此时为空，可以删除
        boolean returnB = dir.delete();
        return returnB;
    }
	


	
	
//	public static  boolean deleteFiles(File dir){
//		File[] files = dir.listFiles();
//		boolean success = true;
//		if (files != null) {
//			for (File file : files) {
//				
//				if (file.isDirectory()) {
//					success &= deleteContents(file);
//				}
//				try{
//				 Libcore.os.remove(file.getAbsolutePath());
//				}catch(Exception e){
//					Log.d(TAG, ""+file.getAbsolutePath()+"-->"+ e);
//				}
////				if (!(file.delete())) {
////					Log.w("FileUtils",
////							new StringBuilder().append("Failed to delete ")
////									.append(file).toString());
////					success = false;
////				}
//			}
//		}
//		return success;
//	}
	
	/**
	 * delete theme
	 * @param theme
	 * @return
	 */
	public static boolean deleteTheme(Theme theme){
		File file = new File(theme.usedPath);
		File[] files = file.listFiles();
		if(files != null){
			for(File f:files){
				if(f.isDirectory()){
					deleteFiles(f);
					continue;
				}
				f.delete();
			}
		}
		return true;
	}
	
	/**
	 * change theme files permission to 644
	 */
	public static void changeThemePermission(){
		chmod("755",ThemeConfig.THEME_BASE_PATH);
	}
	
	
	
	public static void chmod(String permission, String path) {
		String command = null;
		Process p;
		try {
			command = "chmod -R " + permission + " " + path;
			p = Runtime.getRuntime().exec(command);
			int status = p.waitFor();
			Log.i("ThemeTask","exec "+command+" succeed."+"  status"+status);
			if (status == 0) {    
				Log.i(TAG,"exec "+command+" succeed.");
			} else {    
				Log.i(TAG,"exec "+command+" failed.");   
			}   
		} catch (IOException e) {
			Log.i(TAG,"chmod IOException.");
		} catch (InterruptedException e) {
			Log.i(TAG,"chmod InterruptedException.");
		}
	}

	public static boolean deleteFile(File file) {
		// TODO Auto-generated method stub
		return file.delete();
	}
	
	
	
	
	
	
	
	
	
	

}
