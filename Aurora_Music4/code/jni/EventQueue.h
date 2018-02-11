/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef _EVENTQUEUE_H
#define _EVENTQUEUE_H
#include <pthread.h>
#include <list>
#include <memory>
using std::shared_ptr;

struct EventQueue {

    typedef int32_t event_id;

    struct Event {
        Event()
            : mEventID(0) {
        }

        virtual ~Event() {}

        event_id eventID() {
            return mEventID;
        }

    protected:
        virtual void fire(EventQueue *queue, int64_t now_us) = 0;

    private:
        friend class EventQueue;

        event_id mEventID;

        void setEventID(event_id id) {
            mEventID = id;
        }

        Event(const Event &);
        Event &operator=(const Event &);
    };

    EventQueue();
    ~EventQueue();

    // Start executing the event loop.
    void start();

    bool isRunning() { return mRunning; }
    
    // Stop executing the event loop, if flush is false, any pending
    // events are discarded, otherwise the queue will stop (and this call
    // return) once all pending events have been handled.
    void stop(bool flush = false);

    // Posts an event to the front of the queue (after all events that
    // have previously been posted to the front but before timed events).
    event_id postEvent(shared_ptr<Event> event);

    event_id postEventToBack(shared_ptr<Event> event);

    // It is an error to post an event with a negative delay.
    event_id postEventWithDelay(shared_ptr<Event> event, int64_t delay_us);

    // If the event is to be posted at a time that has already passed,
    // it will fire as soon as possible.
    event_id postTimedEvent(shared_ptr<Event> event, int64_t realtime_us);

    // Returns true iff event is currently in the queue and has been
    // successfully cancelled. In this case the event will have been
    // removed from the queue and won't fire.
    bool cancelEvent(event_id id);

    // Cancel any pending event that satisfies the predicate.
    // If stopAfterFirstMatch is true, only cancels the first event
    // satisfying the predicate (if any).
    void cancelEvents(
            bool (*predicate)(void *cookie, shared_ptr<Event> event),
            void *cookie,
            bool stopAfterFirstMatch = false);

    void cancelAllEvents();

    static int64_t getRealTimeUs();

private:
    struct QueueItem {
        shared_ptr<Event> event;
        int64_t realtime_us;
    };

    struct StopEvent : public EventQueue::Event {
        virtual void fire(EventQueue *queue, int64_t now_us) {
            queue->mStopped = true;
        }
    };

    pthread_t mThread;
    std::list<QueueItem> mQueue;
    pthread_mutex_t mLock;
    pthread_cond_t mQueueNotEmptyCondition;
    pthread_cond_t mQueueHeadChangedCondition;
    event_id mNextEventID;

    bool mRunning;
    bool mStopped;
    shared_ptr<StopEvent> mStopEvent;

    static void *ThreadWrapper(void *me);
    void threadEntry();

    shared_ptr<Event> removeEventFromQueue_l(event_id id);

    EventQueue(const EventQueue &);
    EventQueue &operator=(const EventQueue &);
};


#endif  // TIMED_EVENT_QUEUE_H_
