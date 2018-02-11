/*
 * Copyright (C) 2010 mAPPn.Inc
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
package com.aurora.worldtime;

/**
 * global constants
 * 
 * @author andrew.wang
 * @date 2011-4-20
 * @since Version 0.7.0
 * 
 */
@SuppressWarnings("unused")
public interface Constants {

    static final String CITY_NAME = "city_name";//sharedPreferences xml name
//    static final String KEY_NAME = "keyName";
    static final String KEY_ID = "keyId";//the tag that the cityid in sharedPreferences
    static final String KEY = "key";
    static final int DIALOG_DELETE_NOTEPAD_LIST = 1;

    static final int FRAGMENT_ALARM = 0;
    static final int FRAGMENT_CHRONOMETER = FRAGMENT_ALARM + 1;
    static final int FRAGMENT_STOPWATCH = FRAGMENT_ALARM + 2;
    static final int FRAGMENT_WORLDTIME = FRAGMENT_ALARM + 3;

    static final int TIME_ZONE_SEARCH = 200;// Go to  WorldTimeSearchActivity for searching tag
    static final int TIME_ZONE_UPDATE = 201;// Go to WorldTimeSearchActivity for updating tag
    
    static final int RESULT = 10;//return WorldTimeFragment tag
    static final String XMLTAG_TIMEZONE = "timezone";//xml tag
    static final String KEY_DISPLAYID = "id"; // city id
    static final String KEY_DISPLAYNAME = "name"; // city name
    static final String KEY_GMT = "gmt"; // city GMT
    static final String KEY_OFFSET = "offset"; // city offset

    static final int HOURS_1 = 60 * 60000;

}
