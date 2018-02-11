package com.android.phone;

interface IPhoneRecordStateListener {
    void onStateChange(int state);
    void onError(int iError);
}
