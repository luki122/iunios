package com.android.calendar;

import aurora.app.AuroraActivity;

public abstract class AbstractCalendarActivity extends AuroraActivity {
    protected AsyncQueryService mService;

    public synchronized AsyncQueryService getAsyncQueryService() {
        if (mService == null) {
            mService = new AsyncQueryService(this);
        }
        return mService;
    }
}
