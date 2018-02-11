package com.aurora.puremanager.permission;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;


/**
 * 
 File Description:
 * 
 * @author: Gionee-lihq
 * @see: 2013-4-26 Change List:
 */
public class GNPermissionInfo {
    private String mPmisName;
    private String mPmisGroupName;
    
    private final List<ItemInfo> mEnable = new ArrayList<ItemInfo>();
    private final List<ItemInfo> mDisable = new ArrayList<ItemInfo>();
    // Gionee <jingjc> <2013-9-17> modify for CR00904574 begin
    private final List<ItemInfo> mRequest = new ArrayList<ItemInfo>();
    private final List<ItemInfo> mList = new ArrayList<ItemInfo>();
    // Gionee <jingjc> <2013-9-17> modify for CR00904574 end
    
    public GNPermissionInfo(String permission, String group) {
        mPmisName = permission;
        mPmisGroupName = group;
    }

    public String getPermissionName() {
        return this.mPmisName;
    }

    public String getPmisGroupName() {
        return this.mPmisGroupName;
    }

    public int getNumbers() {
    	int count = 0 ;
    	for (int i = 0; i < mList.size(); i++) {
    		//if (mList.get(i).getStatus() == 1){
    			count++;
    		//}
    	}
        return count;
    }
    
    public void addEnable(ItemInfo appInfo) {
        if (!mEnable.contains(appInfo)) {
            mEnable.add(appInfo);
        }
        if (mDisable.contains(appInfo)) {
            mDisable.remove(appInfo);
        } 
        // Gionee <jingjc> <2013-9-17> modify for CR00904574 begin
        if (mRequest.contains(appInfo)) {
        	mRequest.remove(appInfo);
        }
        // Gionee <jingjc> <2013-9-17> modify for CR00904574 end
        
    }
    
    public void addDisable(ItemInfo appInfo) {
        if (!mDisable.contains(appInfo)) {
            mDisable.add(appInfo);
        }
        if (mEnable.contains(appInfo)) {
            mEnable.remove(appInfo);
        }
        // Gionee <jingjc> <2013-9-17> modify for CR00904574 begin
        if (mRequest.contains(appInfo)) {
        	mRequest.remove(appInfo);
        }
        // Gionee <jingjc> <2013-9-17> modify for CR00904574 end
    }
    
 	// Gionee <jingjc> <2013-9-17> modify for CR00904574 begin
    public void addRequest(ItemInfo appInfo) {
 		if (!mRequest.contains(appInfo)) {
            mRequest.add(appInfo);
        }
        if (mEnable.contains(appInfo)) {
            mEnable.remove(appInfo);
        }
        if (mDisable.contains(appInfo)) {
            mDisable.remove(appInfo);
        }
        
 	}
    
    public void addList(ItemInfo appInfo) {
    	if (!mList.contains(appInfo)) {
    		mList.add(appInfo);
    	}
    }
 	// Gionee <jingjc> <2013-9-17> modify for CR00904574 end
    
    public List<ItemInfo> getEnable() {
        return this.mEnable;
    }
    
    public List<ItemInfo> getDisable() {
        return this.mDisable;
    }
    
    // Gionee <jingjc> <2013-9-17> modify for CR00904574 begin
    public List<ItemInfo> getRequest() {
    	return this.mRequest;
    }
    
    public List<ItemInfo> getList() {
    	return this.mList;
    }
    // Gionee <jingjc> <2013-9-17> modify for CR00904574 end
    
    
    
    public void swap(ItemInfo appInfo) {
        if (mEnable.contains(appInfo)) {
            mEnable.remove(appInfo);
            mDisable.add(appInfo);
        } else if (mDisable.contains(appInfo)) {
            mDisable.remove(appInfo);
            mEnable.add(appInfo);
        }
    }
    
    public void removePackage(ItemInfo appInfo) {
    	if (mList.contains(appInfo)) {
    		mList.remove(appInfo);
    	}
        if (mEnable.contains(appInfo)) {
            mEnable.remove(appInfo);
        } else if (mDisable.contains(appInfo)) {
            mDisable.remove(appInfo);
        }
        // Gionee <jingjc> <2013-9-17> modify for CR00904574 begin
        else if (mRequest.contains(appInfo)) {
        	mRequest.remove(appInfo);
        }
        // Gionee <jingjc> <2013-9-17> modify for CR00904574 end
    }
}
