package com.aurora.puremanager.permission;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

/**
 * 
 File Description:
 * 
 * @author: Gionee-lihq
 * @see: 2013-4-22 Change List:
 */
public class SoftManagerLoader_per extends AsyncTaskLoader<Object> {

    public static final int ID_LOADER_DEFAULT = 100;
    public static final int ID_LOADER_PERMISSION = ID_LOADER_DEFAULT + 1;
    public static final int ID_LOADER_DEFAULTSOFT = ID_LOADER_DEFAULT + 2;

    static int sCurrentId = -1;
    private static boolean mNotLoad = false;
    public SoftManagerLoader_per(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Object loadInBackground() {
        // TODO Auto-generated method stub
        switch (getId()) {
            case ID_LOADER_DEFAULT:
                if (mNotLoad) {
                    mNotLoad = false;
                    break;
                }
                ApplicationsInfo.getInstance().loadAppEntries(getContext());
                break;
            case ID_LOADER_PERMISSION:
                if (sCurrentId == -1) {
                    PermissionsInfo.getInstance().init(getContext());
                } else if(sCurrentId >= 0){
                    // Gionee <liuyb> <2014-5-6> modify for CR01237582 begin
                    PermissionsInfo.getInstance().reloadPmisById(getContext(), sCurrentId, true);
                    //Gionee <liuyb> <2014-5-6> modify for CR01237582 end
                }
                break;
          /*  case ID_LOADER_DEFAULTSOFT:
                DefaultSoftSettingsInfo.getInstance().init(getContext());
                break;*/
            default:
                break;
        }
        return new Object();
    }
    
    @Override
    protected void onStartLoading() {
        // TODO Auto-generated method stub
        forceLoad();
        super.onStartLoading();
    }

//    public void setCurrentId(int id) {
//        sCurrentId = id;
//    }
//    public int getCurrentId(){
//        return sCurrentId;
//    }
}
