package com.aurora.thememanager.utils.themeloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.parser.Parser;
import com.aurora.thememanager.utils.ThemeConfig;
/**
 * theme load task ,we should extends this class to implements our
 * load detail logic
 * @author alexluo
 *
 */
public abstract class ThemeLoader extends AsyncTask<String, Integer, Boolean> implements Loader{
	
	
	
	private static final String TAG = "ThemeLoader";
	
	protected ZipFile mThemeFile;
	
	protected ThemeLoadListener mCallBack;
	
	protected String mInfoKey;
	
	protected String mLoadedInfoPath;
	
	protected String mLoadedPreviewPath;
	
	protected String mLoadedAvatarPath;
	
	private Parser mThemeInfoParser;
	
	protected ArrayList<String> mPreviewKeys = new ArrayList<String>();
	
	protected HashMap<String,ZipEntry> mCache = new HashMap<String,ZipEntry>();
	
	protected ArrayList<Theme> mInfos = new ArrayList<Theme>();
	
	protected List<Drawable> mPreviews = new ArrayList<Drawable>();
	
	protected Context mContext;
	
	private int mProgress;
	
	
	public ThemeLoader(ThemeLoadListener listener,Context context){
		mCallBack = listener;
		mContext = context;
		mThemeInfoParser = initThemeInfoParser();
	}

	/**
	 * create theme information parser here,@see{com.aurora.thememamager.parser.Parser}
	 * @return
	 */
	public abstract Parser initThemeInfoParser();
	
	/**
	 * implements this method to copy theme file to
	 * the path that associate with theme loader
	 * @param file
	 */
	protected abstract void openThemeFile(File file);
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		mInfos.clear();
		mPreviews.clear();
		if(mCallBack != null){
			mCallBack.onStartLoad();
		}
	}
	
	protected void setLoadedInfoPath(String path){
		mLoadedInfoPath = path;
	}
	
	protected void setLoadedPreviewPath(String path){
		mLoadedPreviewPath = path;
	}
	
	protected void setLoadedAvatarPath(String path){
		mLoadedAvatarPath = path;
	}
	
	/**
	 * parse theme information here
	 * @param path
	 * @return
	 */
	@SuppressWarnings("resource")
	protected abstract Theme loadThemeInternal(String path,Parser parser) ;
	
	
	

	

	@Override
	protected Boolean doInBackground(String... path) {
		// TODO Auto-generated method stub
		for(String p:path){
			mThemeInfoParser.printParserName();
			Theme theme = loadThemeInternal(p,mThemeInfoParser);
			if(mCallBack != null){
				if(theme != null){
					mCallBack.onSuccess(true, ThemeConfig.ThemeStatus.STATUS_THEME_LOAD_SUCCESS, theme);
					mInfos.add(theme);
				}else{
					mCallBack.onSuccess(false, ThemeConfig.ThemeStatus.STATUS_THEME_LOAD_IS_NOT_IUNI_THEME, theme);
				}
			}
			
		}
		return mInfos.size() > 0;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		// TODO Auto-generated method stub
		if(mCallBack != null){
			mCallBack.onProgress(values);
		}
	}
	
	
	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
	
	}
	
	
	
	
	public Theme getTheme(int index){
		if(mInfos.size() > 0){
			return mInfos.get(index);
		}
		return null;
	}
	
	
	public ArrayList<Theme> getThemes(){
		return mInfos;
	}
	
		
	/**
	 * copy download theme to the directory that associate with
	 * theme loader
	 * @param input
	 * @param output
	 * @return
	 */
	protected  long loadTheme(File input, File output) {
		long extractedSize = 0L;
		Enumeration<ZipEntry> entries;
		ZipFile zip = null;
		final String baseDir = output.getAbsolutePath();
		String zipName = input.getName();
		int zipIndex =zipName.indexOf(".zip");
		final File out = new File(baseDir,zipName.substring(0,zipIndex));
		try {
			zip = new ZipFile(input);
			long uncompressedSize = getOriginalSize(zip);
			publishProgress(0, (int) uncompressedSize);
			entries = (Enumeration<ZipEntry>) zip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory()) {
					continue;
				}
				String name = entry.getName();
				if(name.endsWith("info")){
					output = new File(out, "info/");
				}else if(name.contains("previews")){
					output = new File(out, "previews/");
				}else if(name.contains("avatar")){
					output = new File(out, "avatar/");
				}else{
					continue;
				}
				int indexRealFile = name.lastIndexOf("/");
				if(indexRealFile != -1){
					name = name.substring(indexRealFile,name.length());
				}
			    File destination = new File(output,name);
				if (!destination.getParentFile().exists()) {
					Log.e(TAG, "make="+ destination.getParentFile().getAbsolutePath());
					destination.getParentFile().mkdirs();
				}
				ProgressReportingOutputStream outStream = new ProgressReportingOutputStream(
						destination);
				extractedSize += copy(zip.getInputStream(entry), outStream);
				outStream.close();
			}
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(zip != null){
					zip.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return extractedSize;

	}

	protected  long getOriginalSize(ZipFile file) {
		Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) file.entries();
		long originalSize = 0l;
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.getSize() >= 0) {
				originalSize += entry.getSize();
			}
		}

		return originalSize;

	}


	private  int copy(InputStream input, OutputStream output) {
		byte[] buffer = new byte[1024 * 8];
		BufferedInputStream in = new BufferedInputStream(input, 1024 * 8);
		BufferedOutputStream out = new BufferedOutputStream(output, 1024 * 8);
		int count = 0, n = 0;
		try {
			while ((n = in.read(buffer, 0, 1024 * 8)) != -1) {
				out.write(buffer, 0, n);
				count += n;
			}
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();

			}

			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return count;

	}


	private final class ProgressReportingOutputStream extends FileOutputStream {
		public ProgressReportingOutputStream(File file)
		throws FileNotFoundException {
			super(file);
		}

		@Override
		public void write(byte[] buffer, int byteOffset, int byteCount)throws IOException {
			super.write(buffer, byteOffset, byteCount);
			mProgress += byteCount;
			publishProgress(mProgress);

		}

	}
	
	
	
	
	
	

}
