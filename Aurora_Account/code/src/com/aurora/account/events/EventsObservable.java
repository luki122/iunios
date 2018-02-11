
package com.aurora.account.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author JimXia
 * 
 * @date 2014-9-28 下午4:56:25
 */
public class EventsObservable {
//    private final static String TAG = "EventsObservable";
    private static volatile EventsObservable notificationCenter;
    private final static Map<String, ArrayList<EventObserver>> EVENT_OBSERVER_MAPS = new HashMap<String, ArrayList<EventObserver>>();

    private EventsObservable() {
    }

    public static EventsObservable getInstance() {
        if (notificationCenter == null) {
            notificationCenter = new EventsObservable();
        }
        return notificationCenter;
    }

    public synchronized void registerObserver(String eventType, EventObserver eventObserver) {
        ArrayList<EventObserver> eventObservers = EVENT_OBSERVER_MAPS.get(eventType);
        if (eventObservers == null) {
            eventObservers = new ArrayList<EventObserver>();
            EVENT_OBSERVER_MAPS.put(eventType, eventObservers);
        }
        if (eventObservers.contains(eventObserver)) {
            return;
        }
        eventObservers.add(eventObserver);
    }

    public void unregisterObserver(String eventType, EventObserver eventObserver) {

        if (eventObserver == null) {
            throw new IllegalArgumentException("The observer is null.");
        }

        synchronized (EVENT_OBSERVER_MAPS) {
            ArrayList<EventObserver> eventObservers = EVENT_OBSERVER_MAPS.get(eventType);
            if (eventObservers.indexOf(eventObserver) == -1) {
                return;
            }
            eventObservers.remove(eventObserver);
        }

    }

    public void unregisterAll() {
        synchronized (EVENT_OBSERVER_MAPS) {
            Set<Map.Entry<String, ArrayList<EventObserver>>> entries = EVENT_OBSERVER_MAPS
                    .entrySet();
            for (Map.Entry<String, ArrayList<EventObserver>> entry : entries) {
                entry.getValue().clear();
            }
            EVENT_OBSERVER_MAPS.clear();
        }
    }

    public void dispatchEvent(String eventType) {
        ArrayList<EventObserver> eventObservers = EVENT_OBSERVER_MAPS.get(eventType);
        if (eventObservers != null) {
            for (EventObserver eventObserver : eventObservers) {
                eventObserver.dispatchChange(eventType);
            }
        }
    }

}
