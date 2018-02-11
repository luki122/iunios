package com.aurora.apihook.keyguard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManagerPolicy.OnKeyguardExitResult;

import com.android.internal.policy.IKeyguardShowCallback;
import com.android.internal.policy.IKeyguardExitCallback;
import com.android.internal.policy.IKeyguardService;
import com.android.internal.widget.LockPatternUtils;

/**
 * A local class that keeps a cache of keyguard state that can be restored in the event
 * keyguard crashes. It currently also allows runtime-selectable
 * local or remote instances of keyguard.
 */
public class AuroraKeyguardServiceDelegate {
    
    
}
