#ifndef _VSEATIMESOURCE_H_
#define _VSEATIMESOURCE_H_
#include "mdate.h"
#include "BackportsLog.h"
#define LOG_TAG "Player_clock"

struct TimeSource
{
    virtual void pause() = 0;
    virtual void resume() = 0;
    virtual int64_t getMediaTimeUs() = 0;
    
    virtual ~TimeSource() {}
};

struct SystemTimeSource : public TimeSource
{
    void start(int64_t startMediaTime)
    {
        reset();
        mStartMediaTime = startMediaTime;
        mSystemStartTime = mdate();
    }
    
    virtual void pause()
    {
        if(mPauseTimePos > 0)
        {
            LOGI("SystemTimeSource already paused");
            return;
        }
        
        mPauseTimePos = mdate();
    }
    
    virtual void resume()
    {
        if(mPauseTimePos > 0)
        {
            mPauseDurationUs += mdate() - mPauseTimePos;
            mPauseTimePos = 0;
        }
    }
    
    virtual int64_t getMediaTimeUs()
    {
        if(mPauseTimePos > 0)
            return mLastTime;
        if(mStartMediaTime < 0)
        {
            LOGE("SystemTimeSource not start");
            return -1;
        }
        
        mLastTime = mdate() - mSystemStartTime - mPauseDurationUs + mStartMediaTime;
        return mLastTime;
    }
    
    SystemTimeSource() { reset(); }
private:
    void reset()
    {
        mStartMediaTime = -1;
        mSystemStartTime = 0;
        mPauseDurationUs = 0; 
        mPauseTimePos = 0;
        mLastTime = 0;
    }
    
private:
    int64_t mStartMediaTime;
    int64_t mSystemStartTime;
    int64_t mPauseDurationUs;
    
    int64_t mPauseTimePos;
    int64_t mLastTime;
};

#undef LOG_TAG
#endif
