package com.aurora.ota.reporter;
import android.content.Context;
import android.content.Intent;

public interface IReceiverHandler {
	public void handleIntent(Intent intent,Context context);
}
