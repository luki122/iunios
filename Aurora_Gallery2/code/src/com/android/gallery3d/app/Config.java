/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import com.android.gallery3d.R;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.fragmentapp.GridViewUtil;
import com.android.gallery3d.fragmentutil.MySelfBuildConfig;
import com.android.gallery3d.ui.AlbumSetSlotRenderer;
import com.android.gallery3d.ui.SlotView;
import com.android.gallery3d.util.GalleryUtils;

final class Config {
    public static class AlbumSetPage {
        private static AlbumSetPage sInstance;

        public SlotView.Spec slotViewSpec;
        public AlbumSetSlotRenderer.LabelSpec labelSpec;
        public int paddingTop;
        public int paddingBottom;
        public int placeholderColor;

        public static synchronized AlbumSetPage get(Context context) {
            if (sInstance == null) {
                sInstance = new AlbumSetPage(context);
            }
            return sInstance;
        }

        private AlbumSetPage(Context context) {
            Resources r = context.getResources();

            placeholderColor = r.getColor(R.color.albumset_placeholder);

            slotViewSpec = new SlotView.Spec();
            slotViewSpec.rowsLand = r.getInteger(R.integer.albumset_rows_land);
            slotViewSpec.rowsPort = r.getInteger(R.integer.albumset_rows_port);
            slotViewSpec.slotGap = r.getDimensionPixelSize(R.dimen.albumset_slot_gap);
            slotViewSpec.slotHeightAdditional = 0;

            paddingTop = r.getDimensionPixelSize(R.dimen.albumset_padding_top);
            paddingBottom = r.getDimensionPixelSize(R.dimen.albumset_padding_bottom);

            labelSpec = new AlbumSetSlotRenderer.LabelSpec();
            labelSpec.labelBackgroundHeight = r.getDimensionPixelSize(
                    R.dimen.albumset_label_background_height);
            labelSpec.titleOffset = r.getDimensionPixelSize(
                    R.dimen.albumset_title_offset);
            labelSpec.countOffset = r.getDimensionPixelSize(
                    R.dimen.albumset_count_offset);
            labelSpec.titleFontSize = r.getDimensionPixelSize(
                    R.dimen.albumset_title_font_size);
            labelSpec.countFontSize = r.getDimensionPixelSize(
                    R.dimen.albumset_count_font_size);
            labelSpec.leftMargin = r.getDimensionPixelSize(
                    R.dimen.albumset_left_margin);
            labelSpec.titleRightMargin = r.getDimensionPixelSize(
                    R.dimen.albumset_title_right_margin);
            labelSpec.iconSize = r.getDimensionPixelSize(
                    R.dimen.albumset_icon_size);
            labelSpec.backgroundColor = r.getColor(
                    R.color.albumset_label_background);
            labelSpec.titleColor = r.getColor(R.color.albumset_label_title);
            labelSpec.countColor = r.getColor(R.color.albumset_label_count);
        }
    }

    public static class AlbumPage {
        private static AlbumPage sInstance;

        public SlotView.Spec slotViewSpec;
        public int placeholderColor;

        public static synchronized AlbumPage get(Context context) {
        	if (MySelfBuildConfig.USEGALLERY3D_FLAG) {
        		sInstance = new AlbumPage(context);
	            return sInstance;
			} else {
				if (sInstance == null) {
	                sInstance = new AlbumPage(context);
	            }
	            return sInstance;
			}
            
        }

        private AlbumPage(Context context) {
            Resources r = context.getResources();

            placeholderColor = r.getColor(R.color.album_placeholder);

            slotViewSpec = new SlotView.Spec();
            slotViewSpec.rowsLand = r.getInteger(R.integer.album_rows_land);
            slotViewSpec.rowsPort = r.getInteger(R.integer.album_rows_port);
            slotViewSpec.slotGap = r.getDimensionPixelSize(R.dimen.album_slot_gap);
            
            //Iuni <lory><2014-01-16> add for test
            initRect(context);
        }
        
        private void initRect(Context context) {
        	float m_leftlayoutwidth = (float)Math.ceil( context.getResources().getDimension(R.dimen.date_layout_margin));//64.3dp 192.9
        	//m_leftlayoutwidth = 0;
    		int m_horizontalSpacing = (int)context.getResources().getDimension(R.dimen.gridview_horizontalSpacing);
    		int m_verticalSpacing = m_horizontalSpacing;
    		
    		int m_paddingright = (int)context.getResources().getDimension(R.dimen.gridview_paddingright);
    		int m_HeaderPadding = (int)context.getResources().getDimension(R.dimen.header_hight);
    		int m_HeaderPadding2 = m_HeaderPadding + m_horizontalSpacing*2;
    		
        	DisplayMetrics dm = new DisplayMetrics();
        	((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        	int m_Displaywidth = dm.widthPixels;
        	int m_Displayheight = dm.heightPixels;
    		int mNumColumns = MySelfBuildConfig.NUM_CLOUMNS;
    		
    		slotViewSpec.displaywidth = m_Displaywidth;
    		slotViewSpec.displayheight = m_Displayheight;
    		
    		//Aurora <SQF> <2014-03-31> for visual design begin
    		slotViewSpec.headerHeight = (int) Math.ceil(context.getResources().getDimension(R.dimen.header_height));//aurora_dimension.xml 13.3dp
    		//Log.i("SQF_LOG", "zll -----------> m_Displaywidth: " + m_Displaywidth+",m_Displayheight:"+m_Displayheight);
    		
    		slotViewSpec.headerExtraHeight = (int)Math.ceil(context.getResources().getDimension(R.dimen.header_extra_height));//aurora_dimension.xml 3dp-->9px
    		slotViewSpec.slotViewRightMargin3Column = (int)context.getResources().getDimension(R.dimen.slot_view_right_margin_3_column);//aurora_dimension.xml
    		slotViewSpec.slotViewRightMargin6Column = (int)context.getResources().getDimension(R.dimen.slot_view_right_margin_6_column);//aurora_dimension.xml
    		//Aurora <SQF> <2014-03-31> for visual design end
        	
    		//slotViewSpec.slotWidth = (m_Displaywidth-m_leftlayoutwidth-(mNumColumns-1)*m_horizontalSpacing- m_horizontalSpacing)/mNumColumns;
    		boolean isPortrait = GalleryUtils.isPortrait(context);
    		if (! isPortrait) {//heng ping
    			slotViewSpec.slotWidth = (int)Math.ceil((m_Displayheight-m_leftlayoutwidth-(mNumColumns-1)*m_horizontalSpacing - slotViewSpec.slotViewRightMargin3Column)/mNumColumns);
    			MySelfBuildConfig.NUM_CLOUMNS_HORIZONTAL = true;
    		} else {
    			slotViewSpec.slotWidth = (int)Math.ceil((m_Displaywidth-m_leftlayoutwidth-(mNumColumns-1)*m_horizontalSpacing - slotViewSpec.slotViewRightMargin3Column)/mNumColumns);
			}
    		
    		MySelfBuildConfig.mCaheWidth = (int)Math.ceil((m_Displaywidth-m_leftlayoutwidth-m_horizontalSpacing - slotViewSpec.slotViewRightMargin3Column)/2)-10;
    		//MediaItem.TYPE_CUST01_SIZE = MySelfBuildConfig.mCaheWidth;
    		//slotViewSpec.slotWidth = (int)Math.ceil((m_Displaywidth-m_leftlayoutwidth-(mNumColumns-1)*m_horizontalSpacing - slotViewSpec.slotViewRightMargin3Column)/mNumColumns);
    		slotViewSpec.slotHeight = slotViewSpec.slotWidth;
    		slotViewSpec.leftlayout = (int)m_leftlayoutwidth;
    		/*
    		Log.i("SQF_LOG", "Config::initRect ---- slotViewSpec.slotWidth:" + slotViewSpec.slotWidth + 
    				         " slotViewSpec.slotHeight:" + slotViewSpec.slotHeight + 
    				         " slotViewSpec.leftlayout:" + slotViewSpec.leftlayout + 
    				         " slotViewSpec.slotViewRightMargin3Column:" + slotViewSpec.slotViewRightMargin3Column +
    				         " slotViewSpec.slotViewRightMargin6Column:" + slotViewSpec.slotViewRightMargin6Column);
    				         */
    		slotViewSpec.actionbarHeight = GalleryUtils.dpToPixel(55);
		}
    }

    public static class ManageCachePage extends AlbumSetPage {
        private static ManageCachePage sInstance;

        public final int cachePinSize;
        public final int cachePinMargin;

        public static synchronized ManageCachePage get(Context context) {
            if (sInstance == null) {
                sInstance = new ManageCachePage(context);
            }
            return sInstance;
        }

        public ManageCachePage(Context context) {
            super(context);
            Resources r = context.getResources();
            cachePinSize = r.getDimensionPixelSize(R.dimen.cache_pin_size);
            cachePinMargin = r.getDimensionPixelSize(R.dimen.cache_pin_margin);
        }
    }
}

