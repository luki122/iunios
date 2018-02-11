/*
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

package com.aurora.launcher;

import java.util.ArrayList;

import android.content.ContentValues;

/**
 * Represents a folder containing shortcuts or apps.
 */
public class FolderInfo extends ItemInfo {

    /**
     * Whether this folder has been opened
     */
    boolean opened;

    /**
     * The apps and shortcuts
     */
    ArrayList<ShortcutInfo> contents = new ArrayList<ShortcutInfo>();
    ArrayList<ShortcutInfo> checkInfos = new ArrayList<ShortcutInfo>();

    ArrayList<FolderListener> listeners = new ArrayList<FolderListener>();

    FolderInfo() {
        itemType = LauncherSettings.Favorites.ITEM_TYPE_FOLDER;
    }

    // Aurora <haojj> <2013-9-14> add for setting checkInfos begin
    public void addCheckInfos(ShortcutInfo info){
    	checkInfos.add(info);
    }
    
    public void addCheckInfos(ArrayList<ShortcutInfo> list){
    	checkInfos.addAll(list);
    }
    
    public void removeCheckInfos(ShortcutInfo info){
    	checkInfos.remove(info);
    }
	// Aurora <haojj> <2013-9-14> end
    
    /**
     * Add an app or shortcut
     *
     * @param item
     */
    public void add(ShortcutInfo item) {
        contents.add(item);
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onAdd(item);
        }
        itemsChanged();
    }
    
    /**
     * Remove an app or shortcut. Does not change the DB.
     *
     * @param item
     */
    public void remove(ShortcutInfo item) {
        contents.remove(item);
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onRemove(item);
        }
        itemsChanged();
    }
    
    // Aurora <haojj> <2013-09-13> add for multi operator shortcut begin
    public void multiAdd() {
    	if(checkInfos.size() <= 0) return;
        contents.addAll(checkInfos);
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onMultiAdd(checkInfos);
        }
        itemsChanged();
    }
    
    public void multiAdd(ArrayList<ShortcutInfo> items) {
    	if(items.size() <= 0) return;
        contents.addAll(items);
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onMultiAdd(items);
        }
        itemsChanged();
    }
    
    public void multiRemove() {
    	if(checkInfos.size() <= 0) return;
        contents.removeAll(checkInfos);
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onMultiRemove(checkInfos);
        }
        itemsChanged();
    }
    
    public void clearChecked(){
    	if(checkInfos.size() <= 0) return;
    	checkInfos.clear();
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onClearChecked();
        }
    }
    
    // 更新图标或文字
    public void updateShortcutItem(ShortcutInfo item) {
    	for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).updateShortcutItem(item);
        }
    }
    // Aurora <haojj> <2013-09-13> add for multi operator shortcut end
    
    public void setTitle(CharSequence title) {
        this.title = title;
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onTitleChanged(title);
        }
    }

    @Override
    void onAddToDatabase(ContentValues values) {
        super.onAddToDatabase(values);
        values.put(LauncherSettings.Favorites.TITLE, title.toString());
    }

    void addListener(FolderListener listener) {
        listeners.add(listener);
    }

    void removeListener(FolderListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    void itemsChanged() {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onItemsChanged();
        }
    }

    @Override
    void unbind() {
        super.unbind();
        listeners.clear();
    }

    // Aurora <haojj> <2013-10-2> add for field begin
    void onProcessAfterUnload(){
    	for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onProcessAfterUnload();
        }
    }
    // Aurora <haojj> <2013-10-2> end
    
    interface FolderListener {
        public void onAdd(ShortcutInfo item);
        public void onRemove(ShortcutInfo item);
        // Aurora <haojj> <2013-09-13> add for batch add shortcut begin
        public void onMultiAdd(ArrayList<ShortcutInfo> items);
        public void onMultiRemove(ArrayList<ShortcutInfo> items);
        public void onClearChecked();
        public void onProcessAfterUnload();
        public void updateShortcutItem(ShortcutInfo item);
        // Aurora <haojj> <2013-09-09> add for batch add shortcut end
        public void onTitleChanged(CharSequence title);
        public void onItemsChanged();
    }
}
