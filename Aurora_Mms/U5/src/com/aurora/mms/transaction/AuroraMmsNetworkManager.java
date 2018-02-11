package com.aurora.mms.transaction;
// Aurora xuyong 2015-08-19 created for bug #15408
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.util.Log;

import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

public class AuroraMmsNetworkManager {
	
	private static final String TAG = "Mms/Txn";
    // Timeout used to call ConnectivityManager.requestNetwork
    private static final int NETWORK_REQUEST_TIMEOUT_MILLIS = 60 * 1000;
    // Wait timeout for this class, a little bit longer than the above timeout
    // to make sure we don't bail prematurely
    private static final int NETWORK_ACQUIRE_TIMEOUT_MILLIS =
            NETWORK_REQUEST_TIMEOUT_MILLIS + (5 * 1000);
    
    private final Context mContext;
    // The requested MMS {@link android.net.Network} we are holding
    // We need this when we unbind from it. This is also used to indicate if the
    // MMS network is available.
    private Network mNetwork;
    // The current count of MMS requests that require the MMS network
    // If mMmsRequestCount is 0, we should release the MMS network.
    private int mMmsRequestCount;
    // This is really just for using the capability
    private final NetworkRequest mNetworkRequest;
    // The callback to register when we request MMS network
    private ConnectivityManager.NetworkCallback mNetworkCallback;

    private volatile ConnectivityManager mConnectivityManager;
    
    // The SIM ID which we use to connect
    private final int mSubId;

    private boolean mIsNetworkLost;
    
    public AuroraMmsNetworkManager(Context context, int subId) {
        mContext = context;
        mNetworkCallback = null;
        mNetwork = null;
        mMmsRequestCount = 0;
        mConnectivityManager = null;
        mSubId = subId;
        mIsNetworkLost = false;
        mNetworkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_MMS)
                .setNetworkSpecifier(Integer.toString(mSubId))
                .build();
    }
    /**
     * Acquire the MMS network
     *
     * @throws com.android.mms.service.exception.Exception if we fail to acquire it
     */
    public void acquireNetwork() throws Exception {
        Log.d(TAG, "MmsNetworkManager: acquireNetwork start");
        synchronized (this) {
            mMmsRequestCount += 1;
            mIsNetworkLost = false;
            if (mNetwork != null) {
                // Already available
                Log.d(TAG, "MmsNetworkManager: already available");
                return;
            }
            Log.d(TAG, "MmsNetworkManager: start new network request");
            // Not available, so start a new request
            newRequest();
            final long shouldEnd = SystemClock.elapsedRealtime() + NETWORK_ACQUIRE_TIMEOUT_MILLIS;
            long waitTime = NETWORK_ACQUIRE_TIMEOUT_MILLIS;
            while (waitTime > 0) {
                try {
                    this.wait(waitTime);
                } catch (InterruptedException e) {
                    Log.w(TAG, "MmsNetworkManager: acquire network wait interrupted");
                }
                if (mNetwork != null) {
                    // Success
                    return;
                }
                // if the network lost, no need to wait.
                if (mIsNetworkLost) {
                    Log.d(TAG, "MmsNetworkManager: network already lost!");
                    break;
                }
                // Calculate remaining waiting time to make sure we wait the full timeout period
                waitTime = shouldEnd - SystemClock.elapsedRealtime();
            }
            // Timed out, so release the request and fail
            Log.d(TAG, "MmsNetworkManager: timed out");
            releaseRequestLocked(mNetworkCallback);
            throw new Exception("Acquiring network timed out");
        }
    }

    /**
     * Release the MMS network when nobody is holding on to it.
     */
    public void releaseNetwork() {
        synchronized (this) {
            if (mMmsRequestCount > 0) {
                mMmsRequestCount -= 1;
                Log.d(TAG, "MmsNetworkManager: release, count=" + mMmsRequestCount);
                if (mMmsRequestCount < 1) {
                    releaseRequestLocked(mNetworkCallback);
                }
            }
        }
    }

    /**
     * Start a new {@link android.net.NetworkRequest} for MMS
     */
    private void newRequest() {
        final ConnectivityManager connectivityManager = getConnectivityManager();
        mNetworkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                Log.d(TAG, "NetworkCallbackListener.onAvailable: network=" + network);
                synchronized (AuroraMmsNetworkManager.this) {
                    mNetwork = network;
                    AuroraMmsNetworkManager.this.notifyAll();
                }
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                Log.d(TAG, "NetworkCallbackListener.onLost: network=" + network);
                synchronized (AuroraMmsNetworkManager.this) {
                    mIsNetworkLost = true;
                    releaseRequestLocked(this);
                    AuroraMmsNetworkManager.this.notifyAll();
                }
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                Log.d(TAG, "NetworkCallbackListener.onUnavailable");
                synchronized (AuroraMmsNetworkManager.this) {
                    mIsNetworkLost = true;
                    releaseRequestLocked(this);
                    AuroraMmsNetworkManager.this.notifyAll();
                }
            }
        };
        Log.d(TAG, "newRequest subid = " + mSubId);
        connectivityManager.requestNetwork(
                mNetworkRequest, mNetworkCallback, NETWORK_REQUEST_TIMEOUT_MILLIS);
    }

    /**
     * Release the current {@link android.net.NetworkRequest} for MMS
     *
     * @param callback the {@link android.net.ConnectivityManager.NetworkCallback} to unregister
     */
    private void releaseRequestLocked(ConnectivityManager.NetworkCallback callback) {
        if (callback != null) {
            final ConnectivityManager connectivityManager = getConnectivityManager();
            connectivityManager.unregisterNetworkCallback(callback);
        }
        resetLocked();
    }
    
    /**
     * Reset the state
     */
    private void resetLocked() {
        mNetworkCallback = null;
        mNetwork = null;
        mMmsRequestCount = 0;
    }
    
    private ConnectivityManager getConnectivityManager() {
        if (mConnectivityManager == null) {
            mConnectivityManager = (ConnectivityManager) mContext.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
        }
        return mConnectivityManager;
    }
    
}
