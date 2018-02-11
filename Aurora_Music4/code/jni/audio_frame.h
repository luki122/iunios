#ifndef _VSEA_AUDIO_FRAME
#define _VSEA_AUDIO_FRAME
#include <stdlib.h>

class AudioFrame
{
public:
    int64_t mTimeUs;
    void *mData; // 16PCM interleaved
    size_t mSize; // Size of mData in bytes
    int32_t mAudioChannels;
    int32_t mAudioSampleRate;

    void clear()
    {
        mTimeUs = 0;
        ::free(mData);
        mData = NULL;
        mSize = 0;
        mAudioChannels = 0;
        mAudioSampleRate = 0;
    }

    AudioFrame() : mTimeUs(0), mData(NULL), mSize(0), mAudioChannels(0), mAudioSampleRate(0)
    {
    }

    AudioFrame(AudioFrame &&other) :
        mTimeUs(other.mTimeUs),
        mData(other.mData),
        mSize(other.mSize),
        mAudioChannels(other.mAudioChannels),
        mAudioSampleRate(other.mAudioSampleRate)
    {
        other.mTimeUs = 0;
        other.mData = NULL;
        other.mSize = 0;
        other.mAudioChannels = 0;
        other.mAudioSampleRate = 0;
    }

    AudioFrame &operator=(AudioFrame &&other)
    {
        if(this == &other)
            return *this;
        mTimeUs = other.mTimeUs;
        mData = other.mData;
        mSize = other.mSize;
        mAudioChannels = other.mAudioChannels;
        mAudioSampleRate = other.mAudioSampleRate;

        other.mTimeUs = 0;
        other.mData = NULL;
        other.mSize = 0;
        other.mAudioChannels = 0;
        other.mAudioSampleRate = 0;
        return *this;
    }

    ~AudioFrame()
    {
        ::free(mData);
    }

    AudioFrame(const AudioFrame &) = delete;
    void operator=(const AudioFrame &) = delete;
};

#endif
