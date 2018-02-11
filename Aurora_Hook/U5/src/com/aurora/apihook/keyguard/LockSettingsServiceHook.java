package com.aurora.apihook.keyguard;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import android.os.Build;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.widget.LockPatternUtils;

import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;

public class LockSettingsServiceHook implements Hook{
	private static final String TAG = "LockSettingsServiceHook";
	
	public void before_checkPassword(MethodHookParam param) throws RemoteException {
		String device = Build.BRAND;
		Log.d(TAG, TAG + "---before_checkPassword..." + device);
		if (device != null && device.equals("samsung") && Build.VERSION.SDK_INT > 18) {
			int userId = (Integer) param.args[1];
			String password = (String) param.args[0];
			ClassHelper.callMethod(param.thisObject, "checkPasswordReadPermission", userId);

	        try {
	        	String lpfn = (String) ClassHelper.callMethod(param.thisObject, "getLockPasswordFilename", userId);
	            // Read all the bytes from the file
	            RandomAccessFile raf = new RandomAccessFile(lpfn, "r");
	            final byte[] stored = new byte[(int) raf.length()];
	            int got = raf.read(stored, 0, stored.length);
	            raf.close();
	            if (got <= 0) {
	                param.setResult(true);
	            }
	            LockPatternUtils mLockPatternUtils = (LockPatternUtils) ClassHelper.getObjectField(param.thisObject, "mLockPatternUtils");
	            // Compare the hash from the file with the entered password's hash
	            final byte[] hash = mLockPatternUtils.passwordToHash(password,mLockPatternUtils.getCurrentUser());
	            final boolean matched = Arrays.equals(stored, hash);
	            if (matched && !TextUtils.isEmpty(password)) {
	            	ClassHelper.callMethod(param.thisObject, "maybeUpdateKeystore", password, userId);
	            }
	            param.setResult(matched);
	        } catch (FileNotFoundException fnfe) {
	            Log.e(TAG, "Cannot read file " + fnfe);
	        } catch (IOException ioe) {
	        	Log.e(TAG, "Cannot read file " + ioe);
	        }
	        param.setResult(true);
		}
	}
}
