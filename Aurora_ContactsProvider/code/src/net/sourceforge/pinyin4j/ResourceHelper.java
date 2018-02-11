/**
 * This file is part of pinyin4j (http://sourceforge.net/projects/pinyin4j/) 
 * and distributed under GNU GENERAL PUBLIC LICENSE (GPL).
 * 
 * pinyin4j is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 2 of the License, or 
 * (at your option) any later version. 
 * 
 * pinyin4j is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License 
 * along with pinyin4j.
 */

package net.sourceforge.pinyin4j;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.android.providers.contacts.ContactsProvider2;
import com.android.providers.contacts.ContactsProvidersApplication;

/**
 * Helper class for file resources
 * 
 * @author Li Min (xmlerlimin@gmail.com)
 * 
 */
public class ResourceHelper
{
    /**
     * @param resourceName
     * @return resource (mainly file in file system or file in compressed
     *         package) as BufferedInputStream
     */
	public static BufferedInputStream getResourceInputStream(String resourceName)
    {
		//Gionee:huangzy 20121129 modify for CR00710695 start
/*//		InputStream is = ResourceHelper.class.getResourceAsStream(resourceName);
		InputStream is = null;
		try {
		    // Gionee:wangth 20121114 modify for CR00729186 begin
		    if (ContactsProvidersApplication.getInstance() == null) {
		        is = ContactsProvider2.mContext.getAssets().open(resourceName);
		    } else {
		        is = ContactsProvidersApplication.getInstance().getAssets().open(resourceName);   
		    }
		    // Gionee:wangth 20121114 modify for CR00729186 end
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		resourceName = "/assets/pinyindb/unicode_to_hanyu_pinyin.txt";
		InputStream is = ResourceHelper.class.getResourceAsStream(resourceName);
		//Gionee:huangzy 20121129 modify for CR00710695 end
		return new BufferedInputStream(is);
    }
}
