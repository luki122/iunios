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

import android.content.Context;

public class ImportExportFactory {
    public static final int SMS_COMPONENT = 0;
    private static ImportExportInterface mComponent = null;

    public static ImportExportInterface getImportExportComponentInstance(Context context, int flag) {
        switch (flag) {
            case SMS_COMPONENT:
                mComponent = new ImportExportSms(context);
                break;

            default:
                mComponent = null;
                break;
        }
        return mComponent;
    }
}
