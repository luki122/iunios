/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.aurora.wechat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import aurora.preference.AuroraPreference;

import com.android.settings.R;

/**
 * Grid preference that allows the user to pick a color from a predefined set of
 * colors. Optionally shows a preview in the preference item.
 */
public class InfoPereference extends AuroraPreference {

    public InfoPereference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.wechat_info);
    }
}
