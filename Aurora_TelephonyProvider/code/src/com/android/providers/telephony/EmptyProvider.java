/* //device/content/providers/telephony/TelephonyProvider.java
**
** Copyright 2006, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

package com.android.providers.telephony;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Environment;
import android.os.FileUtils;
import gionee.provider.GnTelephony;
import android.util.Log;
import android.util.Xml;

import com.android.internal.telephony.BaseCommands;
import com.android.internal.telephony.Phone;
import com.android.internal.util.XmlUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import gionee.provider.GnTelephony.SimInfo;
import gionee.provider.GnTelephony.Carriers;
import gionee.provider.GnTelephony.GprsInfo;
import gionee.provider.GnTelephony.SIMInfo;
import gionee.provider.GnSettings;
import gionee.telephony.GnTelephonyManager;
import android.util.Config;
import android.os.SystemProperties;
import com.gionee.internal.telephony.GnPhone;
import com.aurora.featureoption.FeatureOption;

public class EmptyProvider extends ContentProvider
{

    private static final UriMatcher s_urlMatcher = new UriMatcher(UriMatcher.NO_MATCH);
//MTK-END [mtk04170][111215]
    static {
        //aurora add zhouxiaobing 20131206 start
    s_urlMatcher.addURI("nwkinfo", "nwkinfo/restore", 0);
    //aurora add zhouxiaobing 20131206 end

    }


    @Override
    public boolean onCreate() {
       return false;
    }

    @Override
    public Cursor query(Uri url, String[] projectionIn, String selection,
            String[] selectionArgs, String sort) {

        return null;
    }



    @Override
    public String getType(Uri url)
    {
       return "";
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues)
    {
       return null;
    }

    @Override
    public int delete(Uri url, String where, String[] whereArgs)
    {
 
        return 0;
    }

    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs)
    {
      return 0;
    }

}
