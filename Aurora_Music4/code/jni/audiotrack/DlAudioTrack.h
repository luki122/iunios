#ifndef _DLAUDIOTRACK_H_
#define _DLAUDIOTRACK_H_

#include <dlfcn.h>
#include <stdint.h>
#include <assert.h>

#include <stdlib.h>
#include <pthread.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
// for native asset manager
#include <sys/types.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

extern int __android_version;;

#if 0
class Buffer_froyo
{
public:
    enum {
        MUTE    = 0x00000001
    };
    uint32_t    flags;
    int         channelCount;
    int         format;
    size_t      frameCount;
    size_t      size;
    union {
        void*       raw;
        short*      i16;
        int8_t*     i8;
    };
};


class Buffer_jb
{
public:
    enum {
        MUTE    = 0x00000001
    };
    uint32_t    flags;        // 0 or MUTE
    audio_format_t format; // but AUDIO_FORMAT_PCM_8_BIT -> AUDIO_FORMAT_PCM_16_BIT
    // accessed directly by WebKit ANP callback
    int         channelCount; // will be removed in the future, do not use

    size_t      frameCount;   // number of sample frames corresponding to size;
                                // on input it is the number of frames desired,
                                // on output is the number of frames actually filled

    size_t      size;         // input/output in byte units
    union {
        void*       raw;
        short*      i16;    // signed 16-bit
        int8_t*     i8;     // unsigned 8-bit, offset by 0x80
    };
};
#endif

#define AUDIOTRACK_EVENT_MORE_DATA 0 /*AudioTrack::EVENT_MORE_DATA*/
#define AUDIO_STREAM_MUSIC 3 /*in froyo, is AudioSystem::MUSIC */
#define AUDIO_FORMAT_PCM_16_BIT (0x0 | 0x1) /*(AUDIO_FORMAT_PCM(0x0)| AUDIO_FORMAT_PCM_SUB_16_BIT(0x1)) */
#define AUDIO_CHANNEL_OUT_STEREO (0x1 | 0x2) /*(AUDIO_CHANNEL_OUT_FRONT_LEFT(0x1) | AUDIO_CHANNEL_OUT_FRONT_RIGHT(0x2)) */
#define AUDIO_CHANNEL_OUT_MONO 0x1  /*(AUDIO_CHANNEL_OUT_FRONT_LEFT)*/

//2.3.6
#define ANDROID_2XX_CHANNEL_OUT_FRONT_LEFT  0x4
#define ANDROID_2XX_CHANNEL_OUT_FRONT_RIGHT  0x8

inline int ChannelMask(bool isStereo)
{
    assert(__android_version > 0);
    if(__android_version <= 10)
    {
        if(isStereo)
            return ANDROID_2XX_CHANNEL_OUT_FRONT_LEFT | ANDROID_2XX_CHANNEL_OUT_FRONT_RIGHT;
        else
            return ANDROID_2XX_CHANNEL_OUT_FRONT_LEFT;
    }
    else
    {
        if(isStereo)
            return AUDIO_CHANNEL_OUT_STEREO;
        else
            return AUDIO_CHANNEL_OUT_MONO;
    }
}


class DlAudioTrack
{
public:
    /* Only size and raw used.
    * TODO:may diffrent in new version(current android 4.2.2)
    * 目前的一些版本，AudioTrack::Buffer定义有些许不同（如前），但我们用到的成员(size, raw)的偏移是相同的
    */
    class Buffer
    {
    public:
        enum {
            MUTE    = 0x00000001
        };
        uint32_t    flags;
        int         format;
        int         channelCount; // will be removed in the future, do not use
        size_t      frameCount;
        size_t      size;  /*we use */
        union {
            void*       raw; /*we use*/
            short*      i16;
            int8_t*     i8;
        };
    };

    enum Status {OK = 0, NO_INIT, DLOPEN_ERR, DLSYM_ERR};

private:
    void *m_audioTrack;
    int refCount;

public:
    int initCheck() const;
    uint32_t     latency() const;
    int         frameSize() const;
    void        init();
    void        start(void *data,int size);
    void        stop();
    void        flush();
    void        resume();
    void        pause();
	void 		setVolume(float vol);
    typedef void (*callback_t)(int event, void* user, void *info);
    DlAudioTrack( int streamType,
                uint32_t sampleRate  = 0,
                int format           = 0,
                int channelMask      = 0,
                int frameCount       = 0,
                uint32_t flags       = 0,
                callback_t cbf       = 0,
                void* user           = 0,
                int notificationFrames = 0,
                int sessionId = 0);
    ~DlAudioTrack();
    
private:
    
};

#endif
