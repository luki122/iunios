
package com.aurora.iunivoice.events;

import android.os.Handler;
import android.os.Looper;

/**
 * 观察者
 * 
 * @author JimXia
 * @date 2014-9-28 下午4:55:44
 */
public abstract class EventObserver {
    private Handler mHandler;

    public EventObserver() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    /***
     * 观察者要实现的方法 用于在事件通知的时候做一些事情
     * <p>
     * 改方法是在Main Thread里执行的，对于长时间的操作要注意使用工作线程
     * 
     * @param eventType 通知事件类型
     */
    public abstract void onChange(String eventType);

    public final void dispatchChange(String eventType) {
        mHandler.post(new NotificationRunnable(eventType));
    }

    private final class NotificationRunnable implements Runnable {
        private String eventType;

        public NotificationRunnable(String eventType) {
            this.eventType = eventType;
        }

        public void run() {
            EventObserver.this.onChange(eventType);
        }
    }
}
