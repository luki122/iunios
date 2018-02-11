package com.aurora.thememanager.utils.themeloader;

import java.io.File;
import java.util.ArrayList;

import com.aurora.thememanager.entities.Theme;

/**
 * load  information from downloaded theme,or load from sdcard
 * @author alexluo
 *
 */
public interface Loader {


	public void loadTheme(Object... themes);
	
	/**
	 * get theme info object by index
	 * @param index
	 * @return
	 */
	public Theme getTheme(int index);
	
	/**
	 * get all theme info objects
	 * @return
	 */
	public ArrayList<Theme> getThemes();
	
}
