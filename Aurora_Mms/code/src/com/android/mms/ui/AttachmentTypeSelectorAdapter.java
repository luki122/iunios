/*
 * Copyright (C) 2008 Esmertec AG.
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

package com.android.mms.ui;

//gionee gaoj 2012-6-6 added for CR00614586 start
import com.android.mms.MmsApp;
//gionee gaoj 2012-6-6 added for CR00614586 end
import com.android.mms.MmsConfig;
import com.android.mms.R;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

//gionee luoguangming 2012.08.29 modify for CR00682236 begin
import android.os.SystemProperties;
//gionee luoguangming 2012.08.29 modify for CR00682236  end 
/**
 * An adapter to store icons and strings for attachment type list.
 */
public class AttachmentTypeSelectorAdapter extends IconListAdapter {
    public final static int MODE_WITH_SLIDESHOW    = 0;
    public final static int MODE_WITHOUT_SLIDESHOW = 1;
    // add for vCard
    public final static int MODE_WITH_VCARD        = 2;
    public final static int MODE_WITHOUT_VCARD     = 4;

    public final static int ADD_IMAGE               = 0;
    public final static int TAKE_PICTURE            = 1;
    public final static int ADD_VIDEO               = 2;
    public final static int RECORD_VIDEO            = 3;
    public final static int ADD_SOUND               = 4;
    public final static int RECORD_SOUND            = 5;
    public final static int ADD_SLIDESHOW           = 6;
    // add for vCard
    public final static int ADD_VCARD               = 7;

    //gionee luoguangming 2012.08.29 modify for CR00682236 begin
    public final static int  MENU_ADD_INSERT_GPS      = 8;
    //gionee luoguangming 2012.08.29 modify for CR00682236  end 

    public AttachmentTypeSelectorAdapter(Context context, int mode) {
        super(context, getData(mode, context));
    }
    
    public int buttonToCommand(int whichButton) {
        AttachmentListItem item = (AttachmentListItem)getItem(whichButton);
        return item.getCommand();
    }

//gionee luoguangming 2012.08.29 modify for CR00682236 begin
private static final boolean gnNGMflag = SystemProperties.get("ro.gn.oversea.custom").equals("ITALY_NGM"); 
//gionee luoguangming 2012.08.29 modify for CR00682236  end 

    protected static List<IconListItem> getData(int mode, Context context) {
        //gionee gaoj 2012-6-26 added for CR00628201 start
        /*if (MmsApp.mDarkTheme) {
            List<IconListItem> data = new ArrayList<IconListItem>(8);
            addItem(data, context.getString(R.string.attach_image),
                    R.drawable.gn_dark_ic_attach_picture_holo_light, ADD_IMAGE);

            addItem(data, context.getString(R.string.attach_take_photo),
                    R.drawable.gn_dark_ic_attach_capture_picture_holo_light, TAKE_PICTURE);

            addItem(data, context.getString(R.string.attach_video),
                    R.drawable.gn_dark_ic_attach_video_holo_light, ADD_VIDEO);

            addItem(data, context.getString(R.string.attach_record_video),
                    R.drawable.gn_dark_ic_attach_capture_video_holo_light, RECORD_VIDEO);

            if (MmsConfig.getAllowAttachAudio()) {
                addItem(data, context.getString(R.string.attach_sound),
                        R.drawable.gn_dark_ic_attach_audio_holo_light, ADD_SOUND);
            }

            addItem(data, context.getString(R.string.attach_record_sound),
                    R.drawable.gn_dark_ic_attach_capture_audio_holo_light, RECORD_SOUND);

            if ((mode & MODE_WITH_SLIDESHOW) == MODE_WITH_SLIDESHOW) {
                addItem(data, context.getString(R.string.attach_slideshow),
                        R.drawable.gn_dark_ic_attach_slideshow_holo_light, ADD_SLIDESHOW);
            }
            return data;
        }*/
        //gionee gaoj 2012-6-26 added for CR00628201 end
        //gionee luoguangming 2012.08.29 modify for CR00682236 begin
        if(gnNGMflag == true){
            List<IconListItem> data = new ArrayList<IconListItem>(9);            
            /*addItem(data, context.getString(R.string.attach_image),
                    R.drawable.ic_attach_picture_holo_light, ADD_IMAGE);

            addItem(data, context.getString(R.string.attach_take_photo),
                    R.drawable.ic_attach_capture_picture_holo_light, TAKE_PICTURE);

            addItem(data, context.getString(R.string.attach_video),
                    R.drawable.ic_attach_video_holo_light, ADD_VIDEO);

            addItem(data, context.getString(R.string.attach_record_video),
                    R.drawable.ic_attach_capture_video_holo_light, RECORD_VIDEO);

            if (MmsConfig.getAllowAttachAudio()) {
                addItem(data, context.getString(R.string.attach_sound),
                        R.drawable.ic_attach_audio_holo_light, ADD_SOUND);
            }

            addItem(data, context.getString(R.string.attach_record_sound),
                    R.drawable.ic_attach_capture_audio_holo_light, RECORD_SOUND);

            if ((mode & MODE_WITH_SLIDESHOW) == MODE_WITH_SLIDESHOW) {
                addItem(data, context.getString(R.string.attach_slideshow),
                        R.drawable.ic_attach_slideshow_holo_light, ADD_SLIDESHOW);
            }
            
            // add for vCard
            if ((mode & MODE_WITH_VCARD) == MODE_WITH_VCARD) {
                addItem(data, context.getString(R.string.attach_vcard),
                        R.drawable.ic_vcard_attach_menu, ADD_VCARD);
            }

            //GIONEE:liuying 2012-7-24 modify for CR00655102 start
            if(gnNGMflag == true){
                addItem(data, context.getString(R.string.gn_attach_gps),
                        R.drawable.ic_attach_insert_gps_holo_light, MENU_ADD_INSERT_GPS);
            }*/
            //GIONEE:liuying 2012-7-24 modify for CR00655102 end
            return data;
        }else{
        //gionee luoguangming 2012.08.29 modify for CR00682236 end
        List<IconListItem> data = new ArrayList<IconListItem>(8);
      /*  addItem(data, context.getString(R.string.attach_image),
                R.drawable.ic_attach_picture_holo_light, ADD_IMAGE);

        addItem(data, context.getString(R.string.attach_take_photo),
                R.drawable.ic_attach_capture_picture_holo_light, TAKE_PICTURE);

        addItem(data, context.getString(R.string.attach_video),
                R.drawable.ic_attach_video_holo_light, ADD_VIDEO);

        addItem(data, context.getString(R.string.attach_record_video),
                R.drawable.ic_attach_capture_video_holo_light, RECORD_VIDEO);

        if (MmsConfig.getAllowAttachAudio()) {
            addItem(data, context.getString(R.string.attach_sound),
                    R.drawable.ic_attach_audio_holo_light, ADD_SOUND);
        }

        addItem(data, context.getString(R.string.attach_record_sound),
                R.drawable.ic_attach_capture_audio_holo_light, RECORD_SOUND);

        if ((mode & MODE_WITH_SLIDESHOW) == MODE_WITH_SLIDESHOW) {
            addItem(data, context.getString(R.string.attach_slideshow),
                    R.drawable.ic_attach_slideshow_holo_light, ADD_SLIDESHOW);
        }*/
        
        // add for vCard
        //gionee gaoj 2012-6-6 modified for CR00614586 start
        /*if (!MmsApp.mGnMessageSupport) {
            //gionee gaoj 2012-6-6 modified for CR00614586 end
        if ((mode & MODE_WITH_VCARD) == MODE_WITH_VCARD) {
            addItem(data, context.getString(R.string.attach_vcard),
                    R.drawable.ic_vcard_attach_menu, ADD_VCARD);
        }
        //gionee gaoj 2012-6-6 modified for CR00614586 start
        }*/
        //gionee gaoj 2012-6-6 modified for CR00614586 end

        return data;
    }
    //gionee luoguangming 2012.08.29 modify for CR00682236 begin
    }
    //gionee luoguangming 2012.08.29 modify for CR00682236 end
    protected static void addItem(List<IconListItem> data, String title,
            int resource, int command) {
        AttachmentListItem temp = new AttachmentListItem(title, resource, command);
        data.add(temp);
    }
    
    public static class AttachmentListItem extends IconListAdapter.IconListItem {
        private int mCommand;

        public AttachmentListItem(String title, int resource, int command) {
            super(title, resource);

            mCommand = command;
        }

        public int getCommand() {
            return mCommand;
        }
    }
}
