package com.aurora.thememanager.utils.themehelper;
import com.aurora.thememanager.entities.Theme;
public interface ThemeOperator {
	
	/**
	 * apply theme to IUNI OS
	 * @param theme
	 * @return
	 */
	public void apply(Theme theme);
	
	public void setCallBack(ThemeOperationCallBack callback);

	public void deleteTheme(Theme... theme);

}
