package com.android.phone;

import com.android.phone.IPhoneRecordStateListener;

interface IPhoneRecorder {
    void listen(IPhoneRecordStateListener callback);
    void remove();
    void startRecord();
    void stopRecord(boolean isMount);
}
