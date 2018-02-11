/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.gallery3d.app;

import android.content.Intent;
import android.os.Bundle;

import com.android.gallery3d.R;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.util.GalleryUtils;
// Aurora <zhanggp> <2013-12-21> added for gallery begin	
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.data.Path;
// Aurora <zhanggp> <2013-12-21> added for gallery end

public class AlbumPicker extends PickerActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTitle(R.string.select_album);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        Bundle data = extras == null ? new Bundle() : new Bundle(extras);

        ////Iuni <lory><2013-12-31> add begin
        if (true) {
        	int typeBits = DataManager.INCLUDE_IMAGE;//Iuni <lory><2014-01-08> add begin
        	String defultPath = Path.fromString(GalleryUtils.getMediaSetPath(typeBits) + MediaSetUtils.CAMERA_BUCKET_ID).toString();
    		data.putBoolean(Gallery.KEY_GET_CONTENT, true);
            data.putString(AlbumPage.KEY_MEDIA_PATH,defultPath);
            data.putBoolean(AlbumPage.KEY_WHETHER_IN_ROOT_PAGE, true);//Iuni <lory><2013-12-29> add begin
            data.putInt(Gallery.KEY_TYPE_BITS, typeBits); //Iuni <lory><2013-12-29> add begin
            getStateManager().startState(AlbumPage.class, data);//AlbumSetPage
		} else {
			data.putBoolean(Gallery.KEY_GET_ALBUM, true);
			// Aurora <zhanggp> <2013-12-21> modified for gallery begin	
			String defultPath = Path.fromString("/local/mergeimage/" + MediaSetUtils.CAMERA_BUCKET_ID).toString();
	        data.putString(AlbumPage.KEY_MEDIA_PATH,defultPath);
			
	        //data.putString(AlbumSetPage.KEY_MEDIA_PATH,
	        //        getDataManager().getTopSetPath(DataManager.INCLUDE_IMAGE));
	        getStateManager().startState(AlbumPage.class, data);//AlbumSetPage
			// Aurora <zhanggp> <2013-12-21> modified for gallery end
		}
        //Iuni <lory><2013-12-31> add end
        
    }
}
