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

import java.io.Serializable;
import java.util.ArrayList;

public class SmsInfoModel implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String smsAddress;
    private String smsDate;
    private int smsType;
    private String smsBody;
    private ArrayList<SmsInfoModel> smsInfoModels = null;

    public String getSmsAddress() {
        return smsAddress;
    }

    public void setSmsAddress(String smsAddress) {
        this.smsAddress = smsAddress;
    }

    public String getSmsDate() {
        return smsDate;
    }

    public void setSmsDate(String smsDate) {
        this.smsDate = smsDate;
    }

    public int getSmsType() {
        return smsType;
    }

    public void setSmsType(int smsType) {
        this.smsType = smsType;
    }

    public String getSmsBody() {
        return smsBody;
    }

    public void setSmsBody(String smsBody) {
        this.smsBody = smsBody;
    }

    public ArrayList<SmsInfoModel> getSmsInfoModels() {
        return smsInfoModels;
    }

    public void setSmsInfoModels(ArrayList<SmsInfoModel> smsInfoModels) {
        this.smsInfoModels = smsInfoModels;
    }

    public void addSmsInfoModel(SmsInfoModel model) {
        if (null == smsInfoModels) {
            smsInfoModels = new ArrayList<SmsInfoModel>();
        }
        smsInfoModels.add(model);
    }

    public SmsInfoModel getSmsInfoModel(int position) {
        if (null != smsInfoModels && smsInfoModels.size() > position) {
            return smsInfoModels.get(position);
        }
        return null;
    }

    public void resetSmsInfoModels() {
        if (null != smsInfoModels) {
            smsInfoModels.clear();
            smsInfoModels = null;
        }
    }

    public int getCount() {
        if (null != smsInfoModels) {
            return smsInfoModels.size();
        }
        return 0;
    }
}
