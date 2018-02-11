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

#undef __STRICT_ANSI__
#define __STDINT_LIMITS
#define __STDC_INT64__
#define __STDC_LIMIT_MACROS
#include <stdint.h>
#include <sys/time.h>
#include <assert.h>
#include <errno.h>

#include "EventQueue.h"
#include "BackportsLog.h"

#define LOG_TAG "AuroraPlayer-eventqueue"

EventQueue::EventQueue()
    : mNextEventID(1),
      mRunning(false),
      mStopped(false),
      mStopEvent(new StopEvent)
{
    pthread_mutex_init(&mLock, NULL);
    pthread_cond_init(&mQueueNotEmptyCondition, NULL);
    pthread_cond_init(&mQueueHeadChangedCondition, NULL);
}

EventQueue::~EventQueue()
{
    stop();
    mStopEvent = NULL;

    pthread_cond_destroy(&mQueueHeadChangedCondition);
    pthread_cond_destroy(&mQueueNotEmptyCondition);
    pthread_mutex_destroy(&mLock);
}

void EventQueue::start()
{
    if (mRunning)
    {
        return;
    }

    mStopped = false;

    pthread_attr_t attr;
    pthread_attr_init(&attr);
    pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);

    pthread_create(&mThread, &attr, ThreadWrapper, this);

    pthread_attr_destroy(&attr);

    mRunning = true;
}

void EventQueue::stop(bool flush)
{
    if (!mRunning)
    {
        return;
    }

    if (flush)
    {
        postEventToBack(mStopEvent);
    }
    else
    {
        postTimedEvent(mStopEvent, INT64_MIN);
    }

    void *dummy;
    pthread_join(mThread, &dummy);

    mQueue.clear();

    mRunning = false;
}

EventQueue::event_id EventQueue::postEvent(shared_ptr<Event> event)
{
    // Reserve an earlier timeslot an INT64_MIN to be able to post
    // the StopEvent to the absolute head of the queue.
    return postTimedEvent(event, INT64_MIN + 1);
}

EventQueue::event_id EventQueue::postEventToBack(
    shared_ptr<Event> event)
{
    return postTimedEvent(event, INT64_MAX);
}

EventQueue::event_id EventQueue::postEventWithDelay(
    shared_ptr<Event> event, int64_t delay_us)
{
    assert(delay_us >= 0);
    return postTimedEvent(event, getRealTimeUs() + delay_us);
}

EventQueue::event_id EventQueue::postTimedEvent(
    shared_ptr<Event> event, int64_t realtime_us)
{
    pthread_mutex_lock(&mLock);

    event->setEventID(mNextEventID++);

    std::list<QueueItem>::iterator it = mQueue.begin();
    while (it != mQueue.end() && realtime_us >= (*it).realtime_us)
    {
        ++it;
    }

    QueueItem item;
    item.event = event;
    item.realtime_us = realtime_us;

    if (it == mQueue.begin())
    {
        pthread_cond_broadcast(&mQueueHeadChangedCondition);
    }

    mQueue.insert(it, item);
    pthread_cond_broadcast(&mQueueNotEmptyCondition);

    event_id id = event->eventID();
    pthread_mutex_unlock(&mLock);
    return id;
}

static bool MatchesEventID(
    void *cookie, shared_ptr<EventQueue::Event> event)
{
    EventQueue::event_id *id =
        static_cast<EventQueue::event_id *>(cookie);

    if (event->eventID() != *id)
    {
        return false;
    }

    *id = 0;

    return true;
}

bool EventQueue::cancelEvent(event_id id)
{
    if (id == 0)
    {
        return false;
    }

    cancelEvents(&MatchesEventID, &id, true /* stopAfterFirstMatch */);

    // if MatchesEventID found a match, it will have set id to 0
    // (which is not a valid event_id).

    return id == 0;
}

void EventQueue::cancelEvents(
    bool (*predicate)(void *cookie, shared_ptr<Event> event),
    void *cookie,
    bool stopAfterFirstMatch)
{
    pthread_mutex_lock(&mLock);

    std::list<QueueItem>::iterator it = mQueue.begin();
    while (it != mQueue.end())
    {
        if (!(*predicate)(cookie, (*it).event))
        {
            ++it;
            continue;
        }

        if (it == mQueue.begin())
        {
            pthread_cond_broadcast(&mQueueHeadChangedCondition);
        }

        LOGV("cancelling event %d", (*it).event->eventID());

        (*it).event->setEventID(0);
        it = mQueue.erase(it);

        if (stopAfterFirstMatch)
        {
            break;
        }
    }

    pthread_mutex_unlock(&mLock);
}

void EventQueue::cancelAllEvents()
{
    pthread_mutex_lock(&mLock);

    std::list<QueueItem>::iterator it = mQueue.begin();
    while (it != mQueue.end())
    {
        if (it == mQueue.begin())
        {
            pthread_cond_broadcast(&mQueueHeadChangedCondition);
        }

        LOGV("cancelling event %d", (*it).event->eventID());

        (*it).event->setEventID(0);
        it = mQueue.erase(it);
    }
    pthread_mutex_unlock(&mLock);
}


// static
int64_t EventQueue::getRealTimeUs()
{
    struct timeval tv;
    gettimeofday(&tv, NULL);

    return (int64_t)tv.tv_sec * 1000000ll + tv.tv_usec;
}

// static
void *EventQueue::ThreadWrapper(void *me)
{
    static_cast<EventQueue *>(me)->threadEntry();
    return NULL;
}

void EventQueue::threadEntry()
{
//     prctl(PR_SET_NAME, (unsigned long)"EventQueue", 0, 0, 0);

    for (;;)
    {
        int64_t now_us = 0;
        shared_ptr<Event> event;
        int id;
        {
            pthread_mutex_lock(&mLock);

            if (mStopped)
            {
                pthread_mutex_unlock(&mLock);
                break;
            }

            while (mQueue.empty())
            {
                pthread_cond_wait(&mQueueNotEmptyCondition, &mLock);
            }

            event_id eventID = 0;
            for (;;)
            {
                if (mQueue.empty())
                {
                    // The only event in the queue could have been cancelled
                    // while we were waiting for its scheduled time.
                    break;
                }

                std::list<QueueItem>::iterator it = mQueue.begin();
                eventID = (*it).event->eventID();

                now_us = getRealTimeUs();
                int64_t when_us = (*it).realtime_us;

                int64_t delay_us;
                if (when_us < 0 || when_us == INT64_MAX)
                {
                    delay_us = 0;
                }
                else
                {
                    delay_us = when_us - now_us;
                }

                if (delay_us <= 0)
                {
                    break;
                }

                static int64_t kMaxTimeoutUs = 10000000ll;  // 10 secs
                bool timeoutCapped = false;
                if (delay_us > kMaxTimeoutUs)
                {
                    LOGW("delay_us exceeds max timeout: %lld us", delay_us);

                    // We'll never block for more than 10 secs, instead
                    // we will split up the full timeout into chunks of
                    // 10 secs at a time. This will also avoid overflow
                    // when converting from us to ns.
                    delay_us = kMaxTimeoutUs;
                    timeoutCapped = true;
                }

                struct timeval now;
                struct timespec timeout;

                gettimeofday(&now, NULL);
                timeout.tv_sec = now.tv_sec + delay_us / 1000000ll;
                timeout.tv_nsec = now.tv_usec * 1000ll + delay_us % 1000000ll * 1000ll;
                int err = pthread_cond_timedwait(&mQueueHeadChangedCondition, &mLock, &timeout);

                if (!timeoutCapped && err == ETIMEDOUT)
                {
                    // We finally hit the time this event is supposed to
                    // trigger.
                    now_us = getRealTimeUs();
                    break;
                }
            }

            // The event w/ this id may have been cancelled while we're
            // waiting for its trigger-time, in that case
            // removeEventFromQueue_l will return NULL.
            // Otherwise, the QueueItem will be removed
            // from the queue and the referenced event returned.
            event = removeEventFromQueue_l(eventID);
            id = eventID;
            
            pthread_mutex_unlock(&mLock);
        }

        // too simple, too naive...
        // maybe when calling removeEventFromQueue_l(line: 339), event not canceled,
        // but after it unlock (line: 342), event canceled
        // our cancel api must make sure after the cancel function return, no canceled
        // event running
        // TODO, FIXME: this EventQueue is from android source
        // we need a new implement to fix this problem
        if (event != NULL)
        {
            // Fire event with the lock NOT held.
            // LOGD("fire event:%d", (int)id);
            event->fire(this, now_us);
        }
    }
}

shared_ptr<EventQueue::Event> EventQueue::removeEventFromQueue_l( event_id id)
{
    for (std::list<QueueItem>::iterator it = mQueue.begin();
            it != mQueue.end(); ++it)
    {
        if ((*it).event->eventID() == id)
        {
            shared_ptr<Event> event = (*it).event;
            event->setEventID(0);

            mQueue.erase(it);

            return event;
        }
    }

    LOGW("Event %d was not found in the queue, already cancelled?", id);

    return NULL;
}

