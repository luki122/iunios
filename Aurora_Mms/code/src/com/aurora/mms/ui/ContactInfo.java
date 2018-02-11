package com.aurora.mms.ui;

public class ContactInfo {

    public String displayName;

    public String number;

    public boolean hasBeenSelected;

    public long contactId;

    public String privacy = "0"; // default value is 0

    public String getPrivacy() {
        return privacy;
    }

    public String getName() {
        return displayName;
    }

    public String getNumber() {
        return number;
    }

    public boolean hasBeenSelected () {
        return hasBeenSelected;
    }

    public void setHasBeenSelected(boolean status) {
        hasBeenSelected = status;
    }
}
