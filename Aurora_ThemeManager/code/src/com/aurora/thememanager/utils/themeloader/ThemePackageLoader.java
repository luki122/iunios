package com.aurora.thememanager.utils.themeloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.parser.Parser;
import com.aurora.thememanager.parser.ThemeJsonInfoPaser;
import com.aurora.thememanager.utils.ThemeConfig;

public class ThemePackageLoader extends ThemeLoader {
	

	private static final String TAG = "ThemeLoader";

	private boolean mLoadFromLocal = false;
	public ThemePackageLoader(ThemeLoadListener listener,Context context) {
		super(listener,context);
		// TODO Auto-generated constructor stub
//		setLoadedInfoPath(ThemeConfig.THEME_LOADED_INFO);
//		setLoadedPreviewPath(ThemeConfig.THEME_LOADED_PREVIEW);
//		setLoadedAvatarPath(ThemeConfig.THEME_LOADED_AVATAR);
	}
	
	public ThemePackageLoader(ThemeLoadListener listener,Context context,boolean importFromLocal) {
		super(listener,context);
		// TODO Auto-generated constructor stub
//		setLoadedInfoPath(ThemeConfig.THEME_LOADED_INFO);
//		setLoadedPreviewPath(ThemeConfig.THEME_LOADED_PREVIEW);
//		setLoadedAvatarPath(ThemeConfig.THEME_LOADED_AVATAR);
		mLoadFromLocal = importFromLocal;
	}
	
	/**
	 * parse theme information here
	 * @param path
	 * @return
	 */
	@Override
	protected Theme loadThemeInternal(String path,Parser parser) {
		File file = new File(path);
		if(!file.exists()){
			if(mCallBack != null){
				mCallBack.onSuccess(false,ThemeConfig.ThemeStatus.STATUS_THEME_LOAD_NO_FOUND_FILE,null);
				return null;
			}
		}
		String[] splits = path.split("/");
		String name = splits[splits.length -1];
		int pointIndex = name.indexOf(".");
		String nameWithoutSuffix = name.substring(0, pointIndex);
		setLoadedInfoPath(ThemeConfig.THEME_LOADED_PATH+nameWithoutSuffix);
		setLoadedPreviewPath(ThemeConfig.THEME_LOADED_PATH+nameWithoutSuffix);
		setLoadedAvatarPath(ThemeConfig.THEME_LOADED_PATH+nameWithoutSuffix);
		File infoFile = new File(mLoadedInfoPath+"/info/info");
		File previews = new File(mLoadedPreviewPath+"/previews");
		File avatar = new File(mLoadedAvatarPath+"/avatar"); 
		
		
		/*
		 * if target theme is loaded,skip this block
		 */
		if(!infoFile.exists() || !previews.exists()){
			openThemeFile(file);
		}
		Theme theme = null;
		boolean hasInfo =false;
		boolean hasPreview = false;
		boolean hasAvatar = false;
		File[] previewFiles = null;
		InputStream infoInput = null;
		File[] avatarFiles = null;
		try {
			
			if(infoFile.exists()){
				infoInput = new FileInputStream(infoFile);
				hasInfo = true;
			}
			
			if(previews.exists()){
				previewFiles = previews.listFiles();
			}
			if(avatar.exists()){
				avatarFiles = avatar.listFiles(); 
			}
			hasPreview = previewFiles != null &&previewFiles.length>0 ;
			hasAvatar = avatarFiles != null && avatarFiles.length >0;
			if(hasInfo){
				List<Object> themes = parser.startParser(infoInput);
				if(themes == null || themes.size()==0){
					return null;
				}
				if(themes.size() > 0){
					theme = (Theme) themes.get(0);
				}
				if(hasPreview){
					theme.preview = previewFiles[0].getAbsolutePath();
					int previewCount = previewFiles.length;
					theme.previews = new String[previewCount];
					for(int i = 0;i<previewCount;i++){
						
						theme.previews[i] = previewFiles[i].getAbsolutePath();
					}
				}
				if(hasAvatar){
					theme.authorIcon = avatarFiles[0].getAbsolutePath();
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int splitDirIndex = path.lastIndexOf("/");
		if(theme != null){
			if(mLoadFromLocal){
				theme.importPathName = path;
			}
			theme.fileDir = path.substring(0, splitDirIndex);
			theme.fileName = name;
		}
		
		return theme;
	}
	
	
	
	public void loadThemeFromPath(String... paths){
		execute(paths);
	}
	
	
	@Override
	public void loadTheme(Object... files) {
		// TODO Auto-generated method stub
		int length = files.length;
		ArrayList<String> paths = new ArrayList<String>();
		for(int i = 0;i<length;i++){
			final File file = (File)files[i];
			if(!file.getAbsolutePath().endsWith(".zip")){
				continue;
			}
			paths.add(file.getAbsolutePath());
		}
		
		loadThemeFromPath(paths.toArray(new String[paths.size()]));
		
	}

	


	@Override
	public Parser initThemeInfoParser() {
		// TODO Auto-generated method stub
		return new Parser(new ThemeJsonInfoPaser());
	}


	@Override
	protected synchronized void openThemeFile(File file) {
		File output = new File(ThemeConfig.THEME_LOADED_PATH);
		loadTheme(file, output);
	}





	
	
}
