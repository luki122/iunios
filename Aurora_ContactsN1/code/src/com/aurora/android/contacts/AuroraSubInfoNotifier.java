package com.aurora.android.contacts;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;

import com.android.contacts.ContactsApplication;

import android.telephony.SubscriptionManager;

public class AuroraSubInfoNotifier {
	
	private Context mContext;
	
	private static AuroraSubInfoNotifier sMe;
	
	private Set<SubInfoListener> mListeners;
	
	private AuroraSubInfoNotifier(Context context) {
		mContext =  context;
		mSubscriptionManager = SubscriptionManager.from(context);
		mSubscriptionManager.addOnSubscriptionsChangedListener(mOnSubscriptionsChangeListener);
		mListeners = new HashSet<SubInfoListener>();
	}
	
	public static AuroraSubInfoNotifier getInstance() {
		if(sMe == null) {
			sMe = new AuroraSubInfoNotifier(ContactsApplication.getInstance());
		}
		return sMe;
	}
	
	public interface SubInfoListener {
		public void onSubscriptionsChanged();
	}
	
	public void addListener(SubInfoListener l) {
		mListeners.add(l);
	}
	
	public void removeListener(SubInfoListener l){
		mListeners.remove(l);
	}
	
	
	private SubscriptionManager mSubscriptionManager;
	private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
		@Override
		public void onSubscriptionsChanged() {
			for(SubInfoListener l:mListeners) {
				l.onSubscriptionsChanged();
			}
		}
	};	
}