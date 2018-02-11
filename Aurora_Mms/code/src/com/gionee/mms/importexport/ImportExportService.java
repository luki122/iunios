/*
 *
 * Copyright (C) 2011 gionee Inc
 *
 * Author: fangbin
 *
 * Description:
 *
 * history
 * name                              date                                      description
 *
 */

package com.gionee.mms.importexport;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class ImportExportService extends Service {
    private static final String TAG = "ImportExportSmsService";
    private ServiceBinder mBinder = new ServiceBinder();

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        Log.d(TAG, "ImportExportService-------------onBind");
        return mBinder;
    }

    class ServiceBinder extends Binder implements ImportExportInterface {
        private ImportExportInterface mComponent = null;
        private ServiceCallBack mCallBack = null;
        @Override
        public void importComponent(final String path) {
            // TODO Auto-generated method stub
            if (null != mComponent) {
                new Thread() {
                    public void run() {
                        Looper.prepare();
                        mComponent.importComponent(path);
                        Looper.loop();
                    }
                }.start();
            }
        }

        @Override
        public void exportComponent() {
            // TODO Auto-generated method stub
            if (null != mComponent) {
                new Thread() {
                    public void run() {
                        Looper.prepare();
                        mComponent.exportComponent();
                        Looper.loop();
                    }
                }.start();

            }
        }

        @Override
        public void setCallBack(ServiceCallBack callBack) {
            mCallBack = callBack;
            if (null != mComponent && null != mCallBack) {
                mComponent.setCallBack(mCallBack);
            }
        }

        @Override
        public void setImportExportComponent(ImportExportInterface component) {
            mComponent = component;
            if (null != mComponent && null != mCallBack) {
                mComponent.setCallBack(mCallBack);
            }
        }
    }

}
