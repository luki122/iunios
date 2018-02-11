package com.aurora.account.activity;

import android.os.Bundle;

import com.aurora.account.events.EventObserver;
import com.aurora.account.events.EventsObservable;

import java.lang.ref.WeakReference;

public abstract class BaseObserverActivity extends BaseActivity {
	private ActivityEventObserver activityEventObserver;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityEventObserver = new ActivityEventObserver(this);
        registerBussinessEventObserver(activityEventObserver);
    }
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBussinessEventObserver(activityEventObserver);
    }

    /**
     * 实现当前页面数据刷新方法(观察者模式通知的事件时调用)
     * 该函数在UI线程中被调用，所以耗时操作请单独开线程
     * @param eventType 通知的事件类型
     */
    protected abstract void onChange(String eventType);
    /**
     * 实现返回需要观察者监听的业务事件类型
     * @return 该界面所监听的事件类型
     */
    protected abstract String[] getObserverEventType();
    
    /**注册观察者*/
    private void registerBussinessEventObserver(EventObserver eventObserver) {
        final String[] observerEventTypes = getObserverEventType();
        if (observerEventTypes != null && observerEventTypes.length > 0) {
            final EventsObservable eventsObservable = EventsObservable.getInstance();
            for(String eventType:observerEventTypes){
                eventsObservable.registerObserver(eventType, eventObserver);
            }
        }
    }
    
    /**反注册观察者*/
    private void unregisterBussinessEventObserver(EventObserver eventObserver) {
        final String[] observerEventTypes = getObserverEventType();
        if (observerEventTypes != null && observerEventTypes.length > 0) {
            final EventsObservable eventsObservable = EventsObservable.getInstance();
            for(String eventType:observerEventTypes){
                eventsObservable.unregisterObserver(eventType, eventObserver);
            }
        }
    }
    
    private static class ActivityEventObserver extends EventObserver {
        private final WeakReference<BaseObserverActivity> mActivity;
        public ActivityEventObserver(BaseObserverActivity activity) {
            super();
            mActivity=new WeakReference<BaseObserverActivity>(activity);
        }
        @Override
        public void onChange(String eventType) {
            BaseObserverActivity activity=mActivity.get();
            if(activity!=null){
                activity.onChange(eventType);
            }
        }
    }
}