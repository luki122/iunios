#include "Audio.h"
#include <errno.h>

#define LOG_TAG "AuroraPlayer-audio"
#define MIN(x,y) ((x) <= (y) ? (x) : (y))

using namespace std;
static void *m_audioTrack;
static size_t MAX = 256;
AudioPlayer::AudioPlayer(Decoder *source) :
		mSource(source), mAudioTrack(NULL), mSampleRate(0), mChannels(0), mWaitReopen(
				false), mTime(-1), mAudioFrameReadPos(0), mFrameTimeUs(-1), mEOF(
				false), mPlaying(false)
#ifdef DUMP_PCM
, mDumpFile(NULL)
#endif
{
#ifdef DUMP_PCM
	mDumpFile = fopen("/sdcard/audio.pcm", "wb");
	if(!mDumpFile)
	LOGE("failed open /sdcard/audio.pcm", strerror(errno));
#endif
}

AudioPlayer::~AudioPlayer() {

	if(NULL != m_audioTrack){
		free(m_audioTrack);
		m_audioTrack = NULL;
	}

	if (mAudioTrack) {
		mAudioTrack->stop();
		mAudioTrack->flush();
		delete mAudioTrack;
		mAudioTrack = NULL;
	}

#ifdef DUMP_PCM
	if(mDumpFile)
	fclose(mDumpFile);
#endif
}

void AudioPlayer::setAudioEOSCb(AudioEOSCb cb) {
	mEOSCb = cb;
}

void AudioPlayer::setAudioParamsChangeCb(AudioParamsChangeCb cb) {
	mAudioParamsChangeCb = cb;
}

int64_t AudioPlayer::getMediaTimeUs() {
	lock_guard < mutex > guard(mMutex);
	return mTime;
}

bool AudioPlayer::init() {
	if (!mSource->HasAudio(mSource)) {
		LOGE("no audio");
		return false;
	}

	if (mAudioTrack) {
		LOGE("AudioTrack created");
		return false;
	}

	mSource->GetAudioParameters(mSource, &mChannels, &mSampleRate);

	if (mChannels <= 0 || mSampleRate <= 0) {
		LOGE("invalid channels or sample rate:%d,%d", mChannels, mSampleRate);
		return false;
	}
	mAudioTrack = new DlAudioTrack(
			AUDIO_STREAM_MUSIC, //TODO
			mSampleRate, AUDIO_FORMAT_PCM_16_BIT,
			(mChannels == 2) ? ChannelMask(true) : ChannelMask(false), 0, 0,
			&AudioCallback, this, 0);
	return true;
}
static int frameSize = 0;
bool AudioPlayer::start() {
	if (!init())
		return false;
	bool isOk = mSource->ReadAudio(mSource, &mAudioFrame);
	frameSize =  mAudioFrame.mSize;
	if(NULL != m_audioTrack){
		free(m_audioTrack);
		m_audioTrack = NULL;
	}
	m_audioTrack = malloc(frameSize);
	memcpy(m_audioTrack, mAudioFrame.mData, frameSize);
	mAudioTrack->start(m_audioTrack, frameSize);
	mPlaying = true;
	return true;
}

bool AudioPlayer::reopen() {
	LOGI("audio player re-open");
	delete mAudioTrack;
	mAudioTrack = NULL;
	mChannels = 0;
	mSampleRate = 0;

	mWaitReopen = false;
	bool isOk = init();
	if (!isOk)
		return false;
	if (mChannels != mAudioFrame.mAudioChannels
			|| mSampleRate != mAudioFrame.mAudioSampleRate) {
		LOGE("zll params from audio frame is different with decoder's audio params\n"
				"zll decoder: %d, %d, audio frame: %d, %d", mChannels, mSampleRate, mAudioFrame.mAudioChannels, mAudioFrame.mAudioSampleRate);
		return false;
	}

	mAudioTrack->start(m_audioTrack, 0);
	return true;
}

void AudioPlayer::pause() {
	mAudioTrack->pause();
	mPlaying = false;
}

void AudioPlayer::resume() {
	mAudioTrack->resume();
	mPlaying = true;
}

void AudioPlayer::flush() {
	bool needstart = false;
	if (mPlaying) {
		mAudioTrack->pause();
		mAudioTrack->flush();
		needstart = true;
	}

	{
		lock_guard < mutex > guard(mMutex);
		mAudioFrame.clear();
		mTime = -1;
		mFrameTimeUs = -1;
		mAudioFrameReadPos = 0;
	}

	if (needstart)
		mAudioTrack->start(m_audioTrack, 0);
}

void AudioPlayer::AudioCallback(int event, void *user, void *info) {
	static_cast<AudioPlayer *>(user)->AudioCallback(event, info);
}

void AudioPlayer::AudioCallback(int event, void *info) {
	if (event == 1) {
		size_t numBytesWritten = fillBuffer(m_audioTrack,frameSize);
		mAudioTrack->start(m_audioTrack, numBytesWritten);
		return;
	}
	DlAudioTrack::Buffer *buffer = (DlAudioTrack::Buffer *) info; //TODO:
	size_t numBytesWritten = fillBuffer(buffer->raw, buffer->size);

	buffer->size = numBytesWritten;

#ifdef DUMP_PCM
	if(mDumpFile)
	fwrite(buffer->raw, 1, buffer->size, mDumpFile);
#endif
}

size_t AudioPlayer::fillBuffer(void *data, size_t size) {
	int needRead = size;
	int64_t curTime = -1;
	int frameSize = mAudioTrack->frameSize();
	if (frameSize <= 0) {
		frameSize = 4;
	}

	do {
		lock_guard < mutex > guard(mMutex);
		if (mWaitReopen)
			break;
		if (mAudioFrame.mData && mAudioFrame.mSize > 0) {
			if (mSampleRate != mAudioFrame.mAudioSampleRate
					|| mChannels != mAudioFrame.mAudioChannels) {
				LOGW(
						"audio params changed, SampleRate:(%d->%d), Channels:(%d->%d", mSampleRate, mAudioFrame.mAudioSampleRate, mChannels, mAudioFrame.mAudioChannels);
				mWaitReopen = true;
				mAudioParamsChangeCb(mAudioFrame.mAudioSampleRate,
						mAudioFrame.mAudioChannels);
				break;
			}
			if (mFrameTimeUs == -1) {
				LOGI(
						"audio frame start with:%lld", (long long)mAudioFrame.mTimeUs);
				mFrameTimeUs = mAudioFrame.mTimeUs;
			}
			int readSize = MIN(needRead, mAudioFrame.mSize - mAudioFrameReadPos);
			if (data)
				memcpy(data + size - needRead,
						mAudioFrame.mData + mAudioFrameReadPos, readSize);
			mAudioFrameReadPos += readSize;
			needRead -= readSize;
			curTime = mFrameTimeUs
					+ (int64_t) mAudioFrameReadPos * 1000000 / frameSize
							/ mAudioFrame.mAudioSampleRate;
			int latency = mAudioTrack->latency() * 1000;
			curTime -= latency;
		}

		if (!mAudioFrame.mData || mAudioFrame.mSize <= 0
				|| mAudioFrame.mSize <= mAudioFrameReadPos) {
			if (mFrameTimeUs != -1 && mAudioFrame.mAudioSampleRate > 0)
				mFrameTimeUs += mAudioFrame.mSize * 1000000ll / frameSize
						/ mAudioFrame.mAudioSampleRate;
			mAudioFrameReadPos = 0;
			mAudioFrame.clear();
			bool isOk = mSource->ReadAudio(mSource, &mAudioFrame);
			if (!isOk)
				mEOF = true;
			else
				mEOF = false;
		}

	} while (needRead > 0 && !mEOF);

	if (curTime >= 0) {
		lock_guard < mutex > guard(mMutex);
		mTime = curTime;
	}

	if (mEOF)
		mEOSCb();
	return size - needRead;
}

bool AudioPlayer::isPlaying() {
	return mPlaying;
}
