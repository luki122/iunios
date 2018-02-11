package com.aurora.puremanager.traffic;

import android.content.AsyncTaskLoader;
import android.net.NetworkStats;
import android.net.NetworkTemplate;
import android.content.Context;
import android.net.INetworkStatsSession;
import android.os.Bundle;
import android.os.RemoteException;

public class SummaryForAllUidLoader extends AsyncTaskLoader<NetworkStats> {
    private static final String KEY_TEMPLATE = "template";
    private static final String KEY_START = "start";
    private static final String KEY_END = "end";

    private final INetworkStatsSession mSession;
    private final Bundle mArgs;

    public static Bundle buildArgs(NetworkTemplate template, long start, long end) {
        final Bundle args = new Bundle();
        args.putParcelable(KEY_TEMPLATE, template);
        args.putLong(KEY_START, start);
        args.putLong(KEY_END, end);

        return args;
    }

    public SummaryForAllUidLoader(Context context, INetworkStatsSession session, Bundle args) {
        super(context);
        mSession = session;
        mArgs = args;
    }

    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public NetworkStats loadInBackground() {
        // TODO Auto-generated method stub
        final NetworkTemplate template = mArgs.getParcelable(KEY_TEMPLATE);
        final long start = mArgs.getLong(KEY_START);
        final long end = mArgs.getLong(KEY_END);

        try {
            return mSession.getSummaryForAllUid(template, start, end, false);
        } catch (RemoteException e) {
            return null;
        }

    }

    protected void onStopLoading() {
        super.onStopLoading();
        cancelLoad();
    }

    protected void onReset() {
        super.onReset();
        cancelLoad();
    }

}