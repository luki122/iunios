package com.aurora.note.activity.record;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.Process;

import com.aurora.note.R;

/**
 * 录音界面启动这个服务来提高进程的优先级，防止进程在后台的时候被回收
 * @author JimXia
 * 2014年7月10日 下午2:49:12
 */
public class ProtectService extends Service {
    private static final int ID = Process.myPid();
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        Notification.Builder notificationBuilder = new Notification.Builder(this);
//        notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.quick_record_icon));
        notificationBuilder.setSmallIcon(R.drawable.quick_record_notification_icon);
        final Resources res = getResources();
        notificationBuilder.setContentTitle(res.getString(R.string.record_notification_title));
        notificationBuilder.setContentText(res.getString(R.string.record_notification_content));
        notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0,
                new Intent(this, RecordActivity2/*QuickRecordEntryActivity*/.class), PendingIntent.FLAG_UPDATE_CURRENT));
        startForeground(ID, notificationBuilder.build());
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
