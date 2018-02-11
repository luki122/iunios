package com.android.mms.util;
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



import com.android.mms.R;

import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

public abstract class GnCustomMenu implements OnMenuItemClickListener {
    private Context mContext;
    private PopupMenu mPopupMenu;
    
    public GnCustomMenu(Context context, View anchor, int menuId) {
        mContext = context;
        mPopupMenu = new PopupMenu(context, anchor);
        mPopupMenu.inflate(menuId);
        mPopupMenu.setOnMenuItemClickListener(this);
        
        bind(anchor);
    }
    
    public abstract void onPrepareMenuItem(Menu menu);
    
    private void bind(View implementer) {        
        /*implementer.setBackgroundDrawable(mContext.getResources().getDrawable(
                R.drawable.gn_dropdown_holo_dark));*/
        implementer.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                onPrepareMenuItem(mPopupMenu.getMenu());
                mPopupMenu.show();
            }
        });
    }
}
