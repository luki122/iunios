
package com.aurora.community.events;

import com.aurora.community.utils.FileLog;

/**
 * 通知中心
 * @author JimXia
 *
 * @date 2014-9-28 下午5:00:41
 */
public class NotificationCenter {
    private final static String TAG = "NotificationCenter";

    private final static NotificationCenter INSTANCE = new NotificationCenter();

    public static NotificationCenter getInstance() {
        return INSTANCE;
    }

    public void notify(String eventType) {
        EventsType eventsType = EventsType.getInstance();
        EventsObservable eventsObservable = EventsObservable.getInstance();
        if (eventsType.contains(eventType)) {
            FileLog.d(TAG, "notify event,eventType is " + eventType);
            eventsObservable.dispatchEvent(eventType);
        }
    }

}
