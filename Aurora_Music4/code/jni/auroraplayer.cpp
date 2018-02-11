#include "auroraplayer.h"
#include <sys/types.h>
#include <string.h>

#include "BackportsLog.h"
#include "common/wrap.h"
#define LOG_TAG "chenhl"


using namespace std;

class PlayerEvent: public EventQueue::Event {
public:
	PlayerEvent(AuroraPlayer *player, void (AuroraPlayer::*method)()) :
			mPlayer(player), mMethod(method) {
	}

	virtual void fire(EventQueue *queue, int64_t /* now_us */) {
		(mPlayer->*mMethod)();
	}

private:
	AuroraPlayer *mPlayer;
	void (AuroraPlayer::*mMethod)();

	PlayerEvent(const PlayerEvent &);
	PlayerEvent &operator=(const PlayerEvent &);
};

class SeekEvent: public EventQueue::Event {
public:
	SeekEvent(AuroraPlayer *player, int64_t seekTimeUs) :
			mPlayer(player), mSeekTimeUs(seekTimeUs) {
	}
protected:
	virtual void fire(EventQueue *queue, int64_t /* now_us */) {
		mPlayer->onSeekEvent(mSeekTimeUs);
	}

private:
	AuroraPlayer *mPlayer;
	int64_t mSeekTimeUs;
};

////////////////////////////////////////////////////////////////////

AuroraPlayer::AuroraPlayer() :
		mRestoreFromUs(0), mOmxCreateOption(
				kKeyDefaultOmxOption | kKeyDisableNativeSoftOmxDec), mDecoder(
				NULL), mAudioPlayer(NULL), mTimeSource(NULL), mSystemTimesource(
				NULL), mEventQueue(new EventQueue()), mVideoEvent(
				shared_ptr < PlayerEvent
						> (new PlayerEvent(this, &AuroraPlayer::onVideoEvent))), mVideoEventPending(
				false), mAudioEnd(false), mAudioValid(false), mError(false) ,mDownloadsize(0){
	mStats.playback_state = NOT_INIT;
	mStats.duration_us = 0;
	mStats.pos_us = 0;
	mStats.buffer_time_us = 0;
	mStats.seeking = false;
	mStats.isprepared = false;
}

AuroraPlayer::~AuroraPlayer() {
	reset();
}

int AuroraPlayer::setListener(ListenerInterface *listener) {
	lock_guard < mutex > guard(mStatsLock);
	if (mStats.playback_state > NOT_INIT)
		return -1;
	mMsgHandler.reset(listener);
	return 0;
}

int AuroraPlayer::setDataSource(const char *uri) {
	lock_guard < mutex > guard(mStatsLock);
	if (mStats.playback_state > NOT_INIT)
		return -1;
	mUri = uri;
	exit_interrupt_reset();
	return 0;
}

int AuroraPlayer::prepareAsync(int64_t startTimeUs) {
	lock_guard < mutex > guard(mStatsLock);
	if (mStats.playback_state != NOT_INIT)
		return -1;
	mStats.playback_state = PREPARING;
	mStats.pos_us = startTimeUs;
	mRestoreFromUs = startTimeUs;
	mEventQueue->start();
	mEventQueue->postEvent(shared_ptr < PlayerEvent
					> (new PlayerEvent(this, &AuroraPlayer::onPrepareEvent)));
	return 0;
}

int AuroraPlayer::prepare(int64_t restoreTimeUs) {
	int err = prepareAsync(restoreTimeUs);
	if (err != 0)
		return err;
	for (;;) {
		usleep(10000);
		lock_guard < mutex > guard(mStatsLock);
		if (mStats.playback_state != PREPARING || exit_interrupt_check()) {
			err = mStats.playback_state == PREPARED ? 0 : -1;
			break;
		}
	}
	return err;
}

int AuroraPlayer::play() {
	lock_guard < mutex > guard(mStatsLock);
	if (mStats.playback_state < PREPARED)
		return -1;
	if (mStats.playback_state == PLAYING)
		return 0;
	if (mStats.playback_state == PAUSE)
		mEventQueue->postEvent(
				shared_ptr < PlayerEvent
						> (new PlayerEvent(this, &AuroraPlayer::onPlayEvent)));
	else {
		assert(mStats.playback_state == PREPARED);
		mEventQueue->postEvent(
				shared_ptr < PlayerEvent
						> (new PlayerEvent(this, &AuroraPlayer::onStartEvent)));
	}
	mStats.playback_state = PLAYING;

	return 0;
}

int AuroraPlayer::pause() {
	lock_guard < mutex > guard(mStatsLock);
	if (mStats.playback_state < PAUSE)
		return -1;
	if (mStats.playback_state == PAUSE)
		return 0;
	mStats.playback_state = PAUSE;
	mEventQueue->postEvent(
			shared_ptr < PlayerEvent
					> (new PlayerEvent(this, &AuroraPlayer::onPauseEvent)));
	return 0;
}

int AuroraPlayer::seekTo(int64_t timeUs) {
	lock_guard < mutex > guard(mStatsLock);
	if (mStats.playback_state < PREPARING)
		return -1;
	if (mStats.seeking) {
		LOGE("seeking, only one seek request could handle at one time");
		return -1;
	}
	mStats.seeking = true;
	mStats.pos_us = timeUs;
	mStats.buffer_time_us = timeUs;
	mEventQueue->postEvent(
			shared_ptr < SeekEvent > (new SeekEvent(this, timeUs)));
	return 0;
}

bool AuroraPlayer::isPlaying() {
	lock_guard < mutex > guard(mStatsLock);
	return mStats.playback_state == PLAYING;
}

int AuroraPlayer::getDuration(int64_t *durationUs) {
	lock_guard < mutex > guard(mStatsLock);
	*durationUs = mStats.duration_us;
	return 0;
}

int AuroraPlayer::getPosition(int64_t *positionUs) {
	lock_guard < mutex > guard(mStatsLock);
	*positionUs = mStats.pos_us;
	return 0;
}

void AuroraPlayer::reset() {
	LOGI("reset player...");
	exit_interrupt_set();
	{
		lock_guard < mutex > guard(mStatsLock);
		if (mStats.playback_state <= NOT_INIT)
			return;
		mStats.playback_state = RESETTING;
	}
	mEventQueue->postEvent(
			shared_ptr < PlayerEvent
					> (new PlayerEvent(this, &AuroraPlayer::onResetEvent)));
	for (;;) {
		usleep(10000);
		lock_guard < mutex > guard(mStatsLock);
		if (mStats.playback_state == NOT_INIT)
			break;
	}
	mEventQueue->stop();
	LOGI("reset player done");
}

void AuroraPlayer::postVideoEvent(int64_t delayUs) {
	if (mVideoEventPending) {
		LOGW("video event pending! can't post it");
		return;
	}

	mVideoEventPending = true;
	if (delayUs <= 0)
		mEventQueue->postEvent(mVideoEvent);
	else
		mEventQueue->postEventWithDelay(mVideoEvent, delayUs);
	return;
}

bool AuroraPlayer::resumeError(int64_t timeUs) {
	bool ret = false;

	// flush video render

	// flush audio playback
	bool needsResumeAudio = false;
	if (mAudioPlayer) {
		if (mAudioPlayer->isPlaying()) {
			needsResumeAudio = true;
			mAudioPlayer->pause();
		}
		LOGI("flush audio player");
		mAudioPlayer->flush();
	}

	// decoder should flush all data imediatly and prepare the first frame at seek pos
	int64_t positionUs = 0;
	ret = mDecoder->Seek(mDecoder, timeUs,&positionUs);

	if (needsResumeAudio) {
		mAudioPlayer->resume();
	}

	if (!ret) {
		LOGE("zll try resume to: %lldus(%.2f secs) from error failed, ", (long long)timeUs, timeUs / 1E6);
	} else {
		LOGI("zll resume to %lldus(%.2f secs) from error successed", (long long)timeUs, timeUs / 1E6);
	}

	postVideoEvent(10000);
	return ret;
}
typedef bool (*CreateAVDecoderType)(Decoder *aDecoder, const char *psz_uri, int64_t startPlaybackTimeUs);
static pthread_once_t avdec_once;
static CreateAVDecoderType pfCreateAVDecoder = NULL;
static void AVDecoderModuleInit()
{
    pfCreateAVDecoder = (CreateAVDecoderType)dlsym(RTLD_DEFAULT, "CreateAVDecoderImpl");
    if(pfCreateAVDecoder == NULL)
        LOGE("zll failed to init avdecoder module");
}

bool CreateAVDecoder(Decoder *aDecoder, const char *psz_uri, int64_t startTimeUs)
{
    pthread_once(&avdec_once, AVDecoderModuleInit);
    LOGE("zll CreateAVDecoder 1");
    if(pfCreateAVDecoder == NULL)
        return false;

    LOGE("zll CreateAVDecoder 2");
    return pfCreateAVDecoder(aDecoder, psz_uri, startTimeUs);
}

void AuroraPlayer::onPrepareEvent() {
	mStatsLock.lock();
	VSEA_CHECK(mStats.playback_state == PREPARING);
	mStatsLock.unlock();

	VSEA_CHECK(mDecoder == NULL);
	mDecoder = (Decoder*) malloc(sizeof(*mDecoder));
	memset(mDecoder, 0, sizeof(*mDecoder));
	bool isOk = false;
	isOk = CreateAVDecoder(mDecoder, mUri.c_str(), mRestoreFromUs);
	if (!isOk) {
		LOGE("chenhl failed to create decoder");
		mStatsLock.lock();
		mStats.playback_state = NOT_INIT;
		mStatsLock.unlock();
		free(mDecoder);
		mDecoder = NULL;
		notify(MEDIA_ERROR, -11);
		return;
	}

	LOGI("chenhl create decoder successed 1");

	int32_t channels = 0;
	int32_t sampleRate = 0;
	int64_t durationUs = 0;
	int64_t bufferTimeUs = 0;
	int32_t width = 0, height = 0;
	mDecoder->GetAudioParameters(mDecoder, &channels, &sampleRate);
	mDecoder->GetDuration(mDecoder, &durationUs);
	mDecoder->GetBufferedTimeUs(mDecoder, &bufferTimeUs);
	LOGI("chenhl create decoder successed mDownloadsize:%d",mDownloadsize);
	mDecoder->setDownloadSize(mDecoder,mDownloadsize);
	
	mStatsLock.lock();
	mStats.playback_state = PREPARED;
	mStats.duration_us = durationUs;
	mStats.buffer_time_us = bufferTimeUs;
	mStats.isprepared =true;
	mStatsLock.unlock();
	LOGI("chenhl create decoder successed 2");
	notify(MEDIA_PREPARED);
	mEventQueue->postEvent(
			shared_ptr < PlayerEvent
					> (new PlayerEvent(this, &AuroraPlayer::onStatsUpdateEvent)));
}

void AuroraPlayer::onStartEvent() {
	VSEA_CHECK(
			mAudioPlayer == NULL && mTimeSource == NULL && mSystemTimesource == NULL);

	if (mDecoder->HasAudio(mDecoder))
		mAudioValid = true;
	// init and start time source
	if (mDecoder->HasAudio(mDecoder)) {
		LOGI("start audio player as time source");
		mAudioPlayer = new AudioPlayer(mDecoder);
		mAudioPlayer->setAudioEOSCb(
				[this]()
				{
					mEventQueue->postEvent(shared_ptr<PlayerEvent>(
									new PlayerEvent(this, &AuroraPlayer::onAudioEOSEvent)));
				});
		mAudioPlayer->setAudioParamsChangeCb(
				[this](int32_t channels, int32_t sampleRate)
				{
					mEventQueue->postEvent(shared_ptr<PlayerEvent>(
									new PlayerEvent(this, &AuroraPlayer::onAudioParamsChangedEvent)));
				});
		mAudioPlayer->start();
		mTimeSource = mAudioPlayer;
	}

	// start play video
	postVideoEvent();
}

void AuroraPlayer::onPlayEvent() {
	if (mError)
		return;

	LOGI("zll resume playback mTimeSource:0x%x", mTimeSource);
	mTimeSource->resume();
}

void AuroraPlayer::onPauseEvent() {
	if (mError)
		return;

	LOGI("zll pause playback mTimeSource:0x%x",mTimeSource);
	mTimeSource->pause();
}

void AuroraPlayer::onResetEvent() {
	LOGI("cancel all events");

	if (mTimeSource) {
		LOGI("pause time source");
		mTimeSource->pause();
	}

	LOGI("release audio player");
	delete mAudioPlayer;
	mAudioPlayer = NULL;

	delete mSystemTimesource;
	mSystemTimesource = NULL;

	mTimeSource = NULL;

	if (mDecoder) {
		LOGI("release decoder");
		mDecoder->DestroyDecoder(mDecoder);
		free(mDecoder);
		mDecoder = NULL;
	}

	// cancel all events
	mEventQueue->cancelAllEvents();

	mError = false;
	mAudioEnd = false;
	mAudioValid = false;
	mRestoreFromUs = 0;

	// NOTE: we not clear those members set by setXXXX function,
	// such as mUri, mMsgHandler.
	// so after reset, we can call prepareAsync and other functions
	// to start play again

	mStatsLock.lock();
	mStats.playback_state = NOT_INIT;
	mStats.duration_us = 0;
	mStats.pos_us = 0;
	mStats.seeking = false;
	mStats.isprepared = false;
	mStatsLock.unlock();
	mDownloadsize =0;
}

void AuroraPlayer::onSeekEvent(int64_t timeUs) {
	if (mError) {
		LOGE("cann't seek as player met error!");
		mStatsLock.lock();
		mStats.seeking = false;
		mStatsLock.unlock();
		notify(MEDIA_SEEK_COMPLETE, -1);
		return;
	}

	// flush audio playback
	bool needsResumeAudio = false;
	if (mAudioPlayer) {
		if (mAudioPlayer->isPlaying()) {
			needsResumeAudio = true;
			mAudioPlayer->pause();
		}
		LOGI("flush audio player");
		mAudioPlayer->flush();
	}

	// decoder should flush all data imediatly and prepare the first frame at seek pos
	int64_t positionUs = 0;
	bool isSeekOk = mDecoder->Seek(mDecoder, timeUs,&positionUs);
	mStats.pos_us = positionUs;
	// set seeking flag false before notify MEDIA_SEEK_COMPLETE
	// as after MEDIA_SEEK_COMPLETE notification, another queued seek request
	// in MediaPlayer will send imediatly
	mStatsLock.lock();
	mStats.seeking = false;
	mStatsLock.unlock();
	if (needsResumeAudio) {
		mAudioPlayer->resume();
	}

	if (!isSeekOk) {
		LOGE(
				"seek to %lldus(%.2f secs) failed", (long long)timeUs, timeUs / 1E6);
		notify(MEDIA_SEEK_COMPLETE, -1);
	} else {
		LOGI(
				"seek to %lldus(%.2f secs) successed", (long long)timeUs, timeUs / 1E6);
		mAudioEnd = false;
		notify(MEDIA_SEEK_COMPLETE);
	}

	postVideoEvent(10000);
}

void AuroraPlayer::onStatsUpdateEvent() {
	if (mError)
		return;
	int64_t durationUs = -1;
	int64_t bufferTimeUs = -1;
	int64_t mediaTimeUs = -1;
	int64_t playlistStartTimeUs = -1;
	mDecoder->GetDuration(mDecoder, &durationUs);
	mDecoder->GetBufferedTimeUs(mDecoder, &bufferTimeUs);
	if (mTimeSource)
		mediaTimeUs = mTimeSource->getMediaTimeUs();

	{
		lock_guard < mutex > guard(mStatsLock);
		mStats.duration_us = durationUs;
		if (!mStats.seeking) {
			if (mediaTimeUs >= 0)
				mStats.pos_us = mediaTimeUs;
			if (playlistStartTimeUs >= 0)
				mStats.pos_us -= playlistStartTimeUs;
			mStats.buffer_time_us = bufferTimeUs;
			if (playlistStartTimeUs >= 0)
				mStats.buffer_time_us -= playlistStartTimeUs;
		}
	}

	if (mStats.duration_us > 0 && mStats.buffer_time_us >= 0) {
		int percent = 100.0 * mStats.buffer_time_us / mStats.duration_us;
		notify(MEDIA_BUFFERING_UPDATE, percent);
	}

	mEventQueue->postEventWithDelay(
			shared_ptr < PlayerEvent
					> (new PlayerEvent(this, &AuroraPlayer::onStatsUpdateEvent)),
			1000000);
}

void AuroraPlayer::onVideoEvent() {
	if (mError)
		return;
	VSEA_CHECK(mVideoEventPending);
	mVideoEventPending = false;
}

void AuroraPlayer::onAudioEOSEvent() {
	if (!mAudioEnd) {
		if (mDecoder->mVar.resume_time != -1) {
			int64_t timeUs = mDecoder->mVar.resume_time;
			mDecoder->mVar.resume_time = -1;
			if (resumeError(timeUs)) {
				LOGI("resume decoder error successed, audio go on");
				notify(MEDIA_INFO, INFO_NETWORK_RESUMED);
				return;
			}
		}
		LOGI("zll audio end");
		mAudioEnd = true;
		// TODO: currenly we just finish playback upon audio eos
		// may start system time source to let video play done
		LOGI("media end");
		notify(MEDIA_PLAYBACK_COMPLETE);
	}
}

void AuroraPlayer::onAudioParamsChangedEvent() {
	if (mError)
		return;
	if (!mAudioPlayer->reopen()) {
		LOGE("zll re-open audio player failed");
		mError = true;
		notify(MEDIA_ERROR);
	}
}


void AuroraPlayer::notify(int msg, int ext1, int ext2, void *other) {
	if (mMsgHandler != NULL)
		mMsgHandler->sendEvent(msg, ext1, ext2);
}

void AuroraPlayer::_audioEOSCb(void *pThis) {
	AuroraPlayer *player = (AuroraPlayer*) pThis;
	player->mEventQueue->postEvent(
			shared_ptr < PlayerEvent
					> (new PlayerEvent(player, &AuroraPlayer::onAudioEOSEvent)));
}

void AuroraPlayer::setVolume(float vol){

}

void AuroraPlayer::setDownloadSize(int size){
//	LOGI("chenhl  setDownloadSize 1:%d",size);
	mDownloadsize = size;
	if(NULL!=mDecoder && mStats.isprepared){
//		LOGI("chenhl  setDownloadSize 2:%d",size);
		mDecoder->setDownloadSize(mDecoder,size);
//		LOGI("chenhl  setDownloadSize 3:%d",size);
	}
}

