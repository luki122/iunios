#ifndef _AUDIO_H_
#define _AUDIO_H_

#include <memory>
#include <mutex>
#include <functional>
#include <stdio.h>
#include "audiotrack/DlAudioTrack.h"
#include "mediadef.h"
#include "audio_frame.h"
#include "PlayerTimeSource.h"

// #define DUMP_PCM

typedef std::function<void()> AudioEOSCb;
typedef std::function<void(int32_t channels, int32_t samplerate)>  AudioParamsChangeCb;

class AudioPlayer : public TimeSource
{
public:

    AudioPlayer(Decoder *source);
    virtual ~AudioPlayer();
    void setAudioEOSCb(AudioEOSCb cb);
    void setAudioParamsChangeCb(AudioParamsChangeCb cb);

    virtual int64_t getMediaTimeUs();
    virtual void pause();
    virtual void resume();

    bool start();
    bool isPlaying();
    bool reopen();
    void flush();

private:
    Decoder *mSource; /*not owner*/
    DlAudioTrack *mAudioTrack;
    int32_t mSampleRate;
    int32_t mChannels;
    bool mWaitReopen;

    AudioFrame mAudioFrame;
    int mAudioFrameReadPos;
    int64_t mFrameTimeUs;
    
    bool mEOF;
    bool mPlaying;

    std::mutex mMutex;
    int64_t mTime;

    AudioEOSCb mEOSCb;
    AudioParamsChangeCb mAudioParamsChangeCb;

#ifdef DUMP_PCM
    FILE *mDumpFile;
#endif
    
    static void AudioCallback(int event, void *user, void *info);
    void AudioCallback(int event, void *info);

    size_t fillBuffer(void *data, size_t size);
    bool init();

    AudioPlayer(const AudioPlayer &);
    AudioPlayer &operator=(const AudioPlayer &);
};

#endif
