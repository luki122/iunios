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


public interface ImportExportInterface {
    public void importComponent(String path);
    public void exportComponent();
    public void setCallBack(ServiceCallBack callBack);
    public void setImportExportComponent(ImportExportInterface component);
}
