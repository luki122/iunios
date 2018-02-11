package com.aurora.filemanager.inter;

import com.aurora.config.AuroraConfig;
import com.aurora.dbutil.FileCategoryHelper.FileCategory;

public interface SelectPageInterface {
	/**
	 * 
	 * @param page {@link AuroraConfig} 
	 */
	public void selectPage(int page,FileCategory fileCategory);

}

