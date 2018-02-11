#ifndef _VSEAPLAYER_H
#define _VSEAPLAYER_H
#include <stdint.h>
#include <string>
#include <mutex>

#include "Audio.h"
#include "mediadef.h"
#include "EventQueue.h"
#include "AuroraListener.h"
#include "PlayerTimeSource.h"
using std::mutex;
using std::shared_ptr;
using std::string;

class VideoEvent;
class SeekEvent;
class VideoSizeChangedEvent;

class AuroraPlayer
{
public:
    friend class VideoEvent;
    friend class SeekEvent;
    friend class VideoSizeChangedEvent;
    
    AuroraPlayer();
    ~AuroraPlayer();

    int setListener(ListenerInterface *listener); /*owner,should delete it*/
    int setDataSource(const char *uri);
    int setDisplay(void *nativeSurface, ANativeWindow *nativeWindow);
    int prepareAsync(int64_t restoreTimeUs = 0);
    int prepare(int64_t restoreTimeUs = 0);

    int play();
    int pause();
    int seekTo(int64_t timeUs);

    bool isPlaying();
    int getDuration(int64_t *durationUs);
    int getPosition(int64_t *positionUs);

    void notify(int msg, int ext1 = 0, int ext2 = 0, void *other = NULL);

    /* reset all resource after setDataSource,that's reback all states to AuroraPlayer
     * constructor called
     */
    void reset();
	void setVolume(float vol);
	void setDownloadSize(int size);

private:
    static void _audioEOSCb(void *pThis);

    void initRenderer();
    void postVideoEvent(int64_t delayUs = 0);
    bool resumeError(int64_t timeUs);

    void onPrepareEvent();
    void onStartEvent();
    void onPlayEvent();
    void onPauseEvent();
    void onSeekEvent(int64_t timeUs);
    void onResetEvent();

    void onVideoEvent();
    void onStatsUpdateEvent();

    void onAudioEOSEvent();
    void onAudioParamsChangedEvent();

private:
    enum PlaybackState{
        RESETTING,
        NOT_INIT,
        PREPARING,
        PREPARED,
        PAUSE,
        PLAYING
    };

    mutex mStatsLock;
    struct Stats
    {
        PlaybackState playback_state;
        int64_t duration_us;
        int64_t pos_us; //NOTE:when seeking, pos_us should not update
        int64_t buffer_time_us;
        bool seeking;
		bool isprepared;
    } mStats;

    string mUri;
    int64_t mRestoreFromUs;
    uint32_t mOmxCreateOption;


    // those members only use in event loop thread, lock is not need.
    shared_ptr<ListenerInterface> mMsgHandler;
    // NOTE: uppper code should make sure native surface and so on
    // for renderer(DisplayInfo) valid all the time, otherwise it may
    // cause player crash. If surface have desctroyed ,the upper should
    // call setDisplay to reset.
    Decoder *mDecoder;
    AudioPlayer *mAudioPlayer;
    TimeSource *mTimeSource;  /*ref mAudioPlayer or mVideoTimeSource */
    SystemTimeSource *mSystemTimesource;
    EventQueue *mEventQueue;

    //those members only use in event loop
    shared_ptr<EventQueue::Event> mVideoEvent;
    bool mVideoEventPending;  //only one video event could queue in event queue
    bool mAudioEnd;
    bool mAudioValid;
    bool mError;
	int mDownloadsize;
    //avoid copy
private:
    AuroraPlayer(const AuroraPlayer &);
    AuroraPlayer &operator=(const AuroraPlayer &);
};

#endif // HLSPLAYER_H
