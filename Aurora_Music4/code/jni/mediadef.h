#ifndef _MEDIADEF_H_
#define _MEDIADEF_H_
#include <dlfcn.h>
#include <pthread.h>
#include <stdint.h>
#include <stdlib.h>
#include <jni.h>
#include "list.h"

struct ANativeWindow;

// transfer ownership
#define PASS(ptr) \
    ( { void *tmp = (void*)ptr; ptr = 0; (typeof(ptr))tmp; } )


enum media_event_type {
    MEDIA_NOP               = 0, // interface test message
    MEDIA_PREPARED          = 1,
    MEDIA_PLAYBACK_COMPLETE = 2,
    MEDIA_BUFFERING_UPDATE  = 3,
    MEDIA_SEEK_COMPLETE     = 4,
    MEDIA_SET_VIDEO_SIZE    = 5,
    MEDIA_LIVE_ENDTIME      = 6,
    AUDIO_EOS               = 60,
    VIDEO_EOS               = 61,
    MEDIA_TIMED_TEXT        = 99,
    MEDIA_ERROR             = 100,
    MEDIA_INFO              = 200,
};

enum media_info_type {
    INFO_NETWORK_RESUMED = 600,
};

enum media_error {
    INVALID_OPERATION    = 1,
    PERMISSION_DENIED    = 2,
};

// similar with kKeyxxx in media/stagefirght/MetaData.h
// set in FFMpegExtractor, could use in stagefirght_avdec
enum {
    kKeyLibavExtraData  = 'aved',
};
enum {
    kTypeLibav = 'lbav',
};


enum OmxOption {
    kKeyDefaultOmxOption = 0x0,
    kKeyDisableNativeOmxDec = 0x1,
    kKeyDisableLibAVDec = 0x1 << 1,
    kKeyDisableNativeRenderer = 0x1 << 2,
    kKeyDisableNativeSoftOmxDec = 0x1 << 3,
};

// TODO, read/write thread safe
struct Var {
    int64_t resume_time;
};

void exit_interrupt_reset();
int exit_interrupt_check();
int exit_interrupt_set();


////////////////////////////////////////////////////////////////////////////////
class DisplayInfo;
class VideoFrame;
class AudioFrame;

struct Decoder {
  Var mVar;
  void *mPrivate;

  bool (*LiveStream)(Decoder *aDecoder);
  void (*GetLiveCurrentStartTimeUs)(Decoder *aDecoder, int64_t *liveStartTimeUs);
  void (*GetDuration)(Decoder *aDecoder, int64_t *durationUs);
  void (*GetBufferedTimeUs)(Decoder *aDecoder, int64_t *timeUs);
  void (*GetVideoParameters)(Decoder *aDecoder, int32_t *aWidth, int32_t *aHeight);
  void (*GetAudioParameters)(Decoder *aDecoder, int32_t *aNumChannels, int32_t *aSampleRate);
  bool (*HasVideo)(Decoder *aDecoder);
  bool (*HasAudio)(Decoder *aDecoder);
//  bool (*Seek)(Decoder *aDecoder, int64_t timeUs);
  bool (*Seek)(Decoder *aDecoder, int64_t timeUs,int64_t *seekTime);
  bool (*ReadAudio)(Decoder *aDecoder, AudioFrame *aFrame);
  void (*DestroyDecoder)(Decoder *);
  void (*setDownloadSize)(Decoder *aDecoder,int downsize);//add by chenhl
};

bool CreateOmxDecoder(Decoder *aDecoder,
        const char *psz_uri,
        int64_t restoreTimeUs,
        DisplayInfo *displayInfo,
        uint32_t option);
bool CreateAVDecoder(Decoder *aDecoder, const char *psz_uri, int64_t startTimeUs);

////////////////////////////////////////////////////////////////////////////////

struct DisplayInfo
{
    void *native_surface;
    ANativeWindow *native_window;
};

////////////////////////////////////////////////////////////////////////////////
void *Surface_getSurface(jobject surface);

#endif
