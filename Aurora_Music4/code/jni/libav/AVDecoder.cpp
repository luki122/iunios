#include "AVDecoder.h"
#include <sys/prctl.h>
#include <pthread.h>
#include <assert.h>
#define __STDC_FORMAT_MACROS 1
#include <inttypes.h>

#include <utility>

extern "C" {
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libswscale/swscale.h"
#include "libavresample/avresample.h"
#include "libavutil/opt.h"
}

#ifdef MOZ_ANDROID_FROYO
#include "common/rwlock.h"
#endif

#include "prefix.h"
#include "audio_frame.h"
#include "avhelp.h"
#include "BackportsLog.h"
#include <unistd.h>

#define LOG_TAG "AuroraPlayer-AVDecoder"

#define MAX_QUEUE_SIZE (15*1024 * 1024)
#define MIN_AUDIOQ_SIZE (20 * 16 * 1024)
#define MIN_FRAMES 5
#define BUF_SIZE_TMP (1024 * 100)

struct AVFrameData
{
AVFrame *frame;
AVPixelFormat srcFormat;
};

static AVPacket flush_pkt;
static pthread_key_t key;
static pthread_once_t once;
static bool seek_ok;
static void initCachedSwsCtx();
static void deinitCachedSwsCtx(void *ctx);
	static bool AVFrameToDst(
void * data, void *dst, int stride, int width, int height);
static void AVFrameDataDestroy(void *data);

////////////////////////////////////////////////////
// packet queue's functions
static void packet_queue_init(PacketQueue *q) {
	memset(q, 0, sizeof(PacketQueue));
	pthread_mutex_init(&q->mutex, NULL);
	pthread_cond_init(&q->cond, NULL);
}

static void packet_queue_flush(PacketQueue *q) {
	AVPacketList *pkt, *pkt1;

	pthread_mutex_lock(&q->mutex);
	for (pkt = q->first_pkt; pkt != NULL; pkt = pkt1) {
		pkt1 = pkt->next;
		av_free_packet(&pkt->pkt);
		av_freep(&pkt);
	}
	q->last_pkt = NULL;
	q->first_pkt = NULL;
	q->nb_packets = 0;
	q->size = 0;
	pthread_mutex_unlock(&q->mutex);
}

static void packet_queue_destroy(PacketQueue *q) {
	packet_queue_flush(q);
	pthread_mutex_destroy(&q->mutex);
	pthread_cond_destroy(&q->cond);
	memset(q, 0, sizeof(*q));
}

static bool packet_queue_put(PacketQueue *q, AVPacket *pkt) {
	AVPacketList *pkt1;
	if (pkt->data != flush_pkt.data) {
		if (!pkt->data)
			return false;
		if (av_dup_packet(pkt) < 0) {
			LOGE("failed dup packet");
			return false;
		}
	} else {
		LOGI("put flush packet to packet queue");
	}

	pkt1 = (AVPacketList *) av_malloc(sizeof(AVPacketList));
	if (!pkt1)
		return false;
	pkt1->pkt = *pkt;
	pkt1->next = NULL;

	pthread_mutex_lock(&q->mutex);

	if (!q->last_pkt)

		q->first_pkt = pkt1;
	else
		q->last_pkt->next = pkt1;
	q->last_pkt = pkt1;
	q->nb_packets++;
	q->size += pkt1->pkt.size;
	pthread_cond_broadcast(&q->cond);

	pthread_mutex_unlock(&q->mutex);
	return true;
}

static bool packet_queue_get(PacketQueue *q, AVPacket *pkt) {
	AVPacketList *pkt1;
	bool ret = false;

	pthread_mutex_lock(&q->mutex);

	for (;;) {
		pkt1 = q->first_pkt;
		if (pkt1) {
			q->first_pkt = pkt1->next;
			if (!q->first_pkt)
				q->last_pkt = NULL;
			q->nb_packets--;
			q->size -= pkt1->pkt.size;
			*pkt = pkt1->pkt;
			av_free(pkt1);
			ret = true;
			break;
		} else if (q->end) {
			ret = false;
			break;
		} else {
			pthread_cond_wait(&q->cond, &q->mutex);
		}
	}
	pthread_cond_broadcast(&q->cond);
	pthread_mutex_unlock(&q->mutex);
	return ret;
}

static bool packet_queue_is_end(PacketQueue *q) {
	bool end;
	pthread_mutex_lock(&q->mutex);
	end = q->end;
	pthread_mutex_unlock(&q->mutex);
	return end;
}

static void packet_queue_set_end(PacketQueue *q, bool end) {
	pthread_mutex_lock(&q->mutex);
	q->end = end;
	pthread_cond_broadcast(&q->cond);
	pthread_mutex_unlock(&q->mutex);
}

static inline void print_error_ex(const char *filename, int err) {
	char errbuf[128];
	const char *errbuf_ptr = errbuf;

	if (av_strerror(err, errbuf, sizeof(errbuf)) < 0)
		errbuf_ptr = strerror(AVUNERROR(err));
	LOGI("%s: %s\n", filename, errbuf_ptr);
}

//copied to stagefright_avdec, at best keep them consistent
static int AudioResampleConvert(struct AVDecoder::ResampleContext *cachedCtx,
		AVFrame *audioFrame, uint8_t **dst, int *dstlen) {
	uint64_t outChannelLayout = audioFrame->channel_layout;
	int outSampleFormat = AV_SAMPLE_FMT_S16;
	int outSampleRate = audioFrame->sample_rate;

	struct AVAudioResampleContext *swrCtx = NULL;
	if (cachedCtx->swrCtx
			&& cachedCtx->channelLayout == audioFrame->channel_layout
			&& cachedCtx->format == audioFrame->format
			&& cachedCtx->sampleRate == audioFrame->sample_rate)
		swrCtx = cachedCtx->swrCtx;
	else {
		if (cachedCtx->swrCtx) {
			swrCtx = cachedCtx->swrCtx;
			avresample_close(swrCtx); //then set options and reopen
		} else {
			// Need format convert, check swr_convert() for details.
			swrCtx = avresample_alloc_context();
			if (!swrCtx) {
				LOGE("error allocating AVAudioResampleContext\n");
				return -1; //TODO
			}
		}

		av_opt_set_int(swrCtx, "in_channel_layout", audioFrame->channel_layout,
				0);
		av_opt_set_int(swrCtx, "in_sample_fmt", audioFrame->format, 0);
		av_opt_set_int(swrCtx, "in_sample_rate", audioFrame->sample_rate, 0);
		av_opt_set_int(swrCtx, "out_channel_layout", outChannelLayout, 0);
		av_opt_set_int(swrCtx, "out_sample_fmt", outSampleFormat, 0);
		av_opt_set_int(swrCtx, "out_sample_rate", outSampleRate, 0);

		int ret = 0;
		if ((ret = avresample_open(swrCtx)) < 0) {
			LOGE("error initializing libavresample:%d\n", ret);
			avresample_free(&swrCtx);
			return -1;
		}

		cachedCtx->swrCtx = swrCtx;
		cachedCtx->channelLayout = audioFrame->channel_layout;
		cachedCtx->format = audioFrame->format;
		cachedCtx->sampleRate = audioFrame->sample_rate;
	}

	int outSamples, outLineSize;
	int osize = av_get_bytes_per_sample((AVSampleFormat) outSampleFormat);
	int channels = av_get_channel_layout_nb_channels(
			audioFrame->channel_layout);

	int outSize = av_samples_get_buffer_size(&outLineSize, channels,
			audioFrame->nb_samples, (AVSampleFormat) outSampleFormat, 0);
	*dst = (uint8_t *) malloc(outSize);

	outSamples = avresample_convert(swrCtx, dst, outLineSize,
			audioFrame->nb_samples, audioFrame->data, audioFrame->linesize[0],
			audioFrame->nb_samples);
	if (outSamples < 0) {
		LOGE("avresample_convert() failed");
		free(*dst);
		*dst = NULL;
		return -1;
	}

	*dstlen = outSamples * osize * channels;
	return outSamples;
}

void initCachedSwsCtx() {
	pthread_key_create(&key, deinitCachedSwsCtx);
}

void deinitCachedSwsCtx(void *ctx) {
	LOGI("close swscale context");
}

void AVFrameDataDestroy(void *pdata) {
	AVFrameData *data = (AVFrameData *) pdata;
	avcodec_free_frame(&data->frame);
	free(pdata);
}

static bool AFrameToAudioFrame(AVFormatContext *fc, AVStream *stream,
		int64_t ptsUs, int64_t sourceTimeOffsetUs, void *data, size_t size,
		int32_t channels, int32_t samplerate, AudioFrame *audioFrame) {
	audioFrame->clear();
	audioFrame->mTimeUs = ptsUs - fc->start_time * 1000000 / AV_TIME_BASE
			+ sourceTimeOffsetUs;
	audioFrame->mData = data;
	audioFrame->mSize = size;
	audioFrame->mAudioChannels = channels;
	audioFrame->mAudioSampleRate = samplerate;
	return true;
}

//////////////////////////////////////////////////////////////////////////////////////////////

AVDecoder::AVDecoder(const char *uri, Var *var) :
		mDecoderVar(var), mOffset(0), mUri(strdup(uri)), mAudioChannels(0), mAudioSampleRate(0), mPixfmt(
				0), mDurationUs(0), mSourceTimeOffsetUs(0), mRefAudioPTS(
				AV_NOPTS_VALUE), mSamples(0), mInitStatus(NOT_INIT), mFormatCtx(
				NULL), mAudioStreamIdx(-1), mFilename(
				NULL), mAudioStream(NULL), mAudioCodecContext(NULL), mAudioFrame(
				NULL), mAudioPacket(
				(AVPacket *) av_malloc(sizeof(*mAudioPacket))), mTmpAudioPacket(
				(AVPacket *) av_malloc(sizeof(*mAudioPacket))), mCustomIOContext(
				NULL), mAbortRequest(false), mSeekReq(false), mSeekRes(false), mSeekPos(
				0), mAudioQInited(false), mReaderThreadStarted(
				false),mDownloadSize(0) ,mCurSize(0),mFilesize(0){
	LOGV("zll AVDecoder::AVDecoder(uri)");
	memset(&mCachedCtx, 0, sizeof(mCachedCtx));
	memset(mAudioPacket, 0, sizeof(*mAudioPacket));
	memset(mTmpAudioPacket, 0, sizeof(*mTmpAudioPacket));
	memset(&mAudioQ, 0, sizeof(mAudioQ));
	pthread_mutex_init(&mLock, NULL);
	pthread_cond_init(&mCond, NULL);
	pthread_mutex_init(&mCommandLock, NULL);

	LibavInit();
}

AVDecoder::~AVDecoder() {
	LOGV("zll AVDecoder::~AVDecoder()");

	// stop reader here if no track!
	stopReaderThread();

	deInitStreams();

	if (mUri) {
		free(mUri);
		mUri = NULL;
	}
	pthread_mutex_destroy(&mCommandLock);
	pthread_mutex_destroy(&mLock);
	pthread_cond_destroy(&mCond);

	av_freep(&mAudioPacket);
	av_freep(&mTmpAudioPacket);

	if (mCachedCtx.swrCtx) {
		avresample_free(&mCachedCtx.swrCtx); //will close and free it
	}
	mFilesize =0;
	mDownloadSize = 0;
	mCurSize = 0;
}

void AVDecoder::stopReaderThread() {
	LOGV("Stopping reader thread");
	pthread_mutex_lock(&mLock);

	if (!mReaderThreadStarted) {
		LOGD("Reader thread have been stopped");
		pthread_mutex_unlock(&mLock);
		return;
	}

	mAbortRequest = true;
	pthread_cond_broadcast(&mCond);
	pthread_mutex_unlock(&mLock);

	void *dummy;
	pthread_join(mReaderThread, &dummy);
	mReaderThreadStarted = false;
	LOGD("Reader thread stopped");
}

bool AVDecoder::Init() {
	if (mInitStatus != NOT_INIT)
		return mInitStatus == INIT_OK;

	int status = initStreams();
	//LOGE("zll Init xxxx 1 status", status);
	if (status != 0) {
		mInitStatus = INIT_FAIL;
		return false;
	}

	//LOGE("zll Init xxxx 2 status");
	startReaderThread();
	mInitStatus = INIT_OK;
	return true;
}

int AVDecoder::startReaderThread() {
	LOGV("chenhl Starting reader thread");
	pthread_mutex_lock(&mLock);
	if (mReaderThreadStarted) {
		pthread_mutex_unlock(&mLock);
		return 0;
	}
	pthread_attr_t attr;
	pthread_attr_init(&attr);
	pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);
	pthread_create(&mReaderThread, &attr, ReaderWrapper, (void *) this);
	pthread_attr_destroy(&attr);
	mReaderThreadStarted = true;
	LOGD("chenhl Reader thread started");

	pthread_mutex_unlock(&mLock);
	return 0;
}

// static
void *AVDecoder::ReaderWrapper(void *me) {
	((AVDecoder *) me)->readerEntry();
	return NULL;
}

void AVDecoder::readerEntry() {
	LOGV("chenhl AVDecoder::readerEntry");
	prctl(PR_SET_NAME, "AVDecoder::readerEntry");

	struct timeval now;
	struct timespec timeout;
	int64_t seekPos = -1;
	bool seek = false;
	bool seekRes = false;
	bool eof = false;
	int err = 0;
	AVPacket pkt1, *pkt = &pkt1;
	av_init_packet(pkt);
	pkt->data = NULL;
	pkt->size = 0;
	
	for (;;) {

//		LOGI("chenhl av_read_frame 1 pkt->size:%d,pkt->pos:%d",pkt->size,pkt->pos);
//		LOGI("chenhl downloadSize:%d,totalsize:%d",mDownloadSize,(pkt->pos+pkt->size));
		
		pthread_mutex_lock(&mLock);		
		while (!mAbortRequest && !exit_interrupt_check() && !mSeekReq
				&& (isPacketQueueFull() || eof )) {

			gettimeofday(&now, NULL);
			timeout.tv_sec = now.tv_sec;
			timeout.tv_nsec = (now.tv_usec + 5000) * 1000; //5ms
			pthread_cond_timedwait(&mCond, &mLock, &timeout); // timedwait as we need check packet queue
		}

		if (mAbortRequest || exit_interrupt_check()) {
			LOGI("chenhl received request to stop read frames");
			av_free_packet(pkt);
			pthread_mutex_unlock(&mLock);
			goto entry_end;
		} else if (mSeekReq) {
			if (seekPos == mSeekPos) {
				LOGI("chenhl seek to %lld finished", (long long)mSeekPos);
				mRefAudioPTS = AV_NOPTS_VALUE;
				mSamples = 0;
				if (pkt->stream_index == mAudioStreamIdx)
					packet_queue_put(&mAudioQ, pkt);
				else
					av_free_packet(pkt);
				mSeekPos = -1;
				mSeekReq = false;
				mSeekRes = seekRes;
				seek = false;
				seekPos = -1;
				seekRes = false;
				pthread_cond_broadcast(&mCond);
			} else {
				LOGI("chenhl met seek request, seek pos:%lld", (long long)mSeekPos);
				seek = true;
				seekPos = mSeekPos;
				seekRes = false;
				eof = false;
				av_free_packet(pkt);

				if (mAudioStreamIdx >= 0) {
					packet_queue_flush(&mAudioQ);
					packet_queue_put(&mAudioQ, &flush_pkt);
				}
			}
		} else {
			if (pkt->stream_index == mAudioStreamIdx)
				packet_queue_put(&mAudioQ, pkt);
			else
				av_free_packet(pkt);
		}

		pthread_mutex_unlock(&mLock);

		if (seek) {
			int64_t pos = seekPos - mSourceTimeOffsetUs;
			if (pos < 0) {
				LOGE("chenhl seek pos mistake, current source timeoffset:%" PRId64 "seek pos:%" PRId64,
						mSourceTimeOffsetUs, seekPos);
				seekRes = false;
			} else {
				int64_t avSeekTime = pos * AV_TIME_BASE / 1000000 + mFormatCtx->start_time;
				int res = avformat_seek_file(mFormatCtx, -1, INT64_MIN, avSeekTime, INT64_MAX, 0);
				if (res < 0) {
					LOGE("chenhl seek to %" PRId64 "failed(av seek value:%" PRId64 " ,result:%d",
							seekPos, avSeekTime, res );
					seekRes = false;
				} else {
					seekRes = true;
				}
			}
		}

		av_init_packet(pkt);
		pkt->data = NULL;
		pkt->size = 0;
		err = av_read_frame(mFormatCtx, pkt);
		mCurSize = pkt->pos;
		//usleep(10000);		
		if (err < 0) {
			LOGI("chenhl no more frames:%d", err);
			if (mAudioStreamIdx >= 0)
				packet_queue_set_end(&mAudioQ, true);
			err = 0;
			eof = true;
			continue;
		}

	}

	entry_end: if (mAudioStreamIdx >= 0)
		packet_queue_set_end(&mAudioQ, true);
	LOGI("chenhl reader thread goto end...");

	pthread_mutex_lock(&mLock);
	mReaderThreadStarted = false;
	pthread_cond_broadcast(&mCond);
	pthread_mutex_unlock(&mLock);
}

int AVDecoder::initStreams() {
	int st_index[AVMEDIA_TYPE_NB] = { 0 };
	st_index[AVMEDIA_TYPE_AUDIO] = -1;

	AVIOContext* pb = NULL;  
	AVInputFormat* piFmt = NULL;
	FILE *file = NULL;

	av_init_packet(&flush_pkt);
	flush_pkt.data = (uint8_t *) "FLUSH";
	flush_pkt.size = 0;

	mFormatCtx = avformat_alloc_context();
	mFormatCtx->interrupt_callback.callback = decode_interrupt_cb;
	mFormatCtx->interrupt_callback.opaque = this;

	mFilename = strdup(mUri);
	LOGV("chenhl initStreams 1 mFilename: %s", mFilename);
	//add by chenhl start

	mFormatCtx->downloadsize_callback.callback_1= download_size_cb;
	mFormatCtx->downloadsize_callback.opaque= this;
	
	file = fopen(mFilename,"rb+");
	if(file!=NULL){
		fseek(file,0,SEEK_END);
		mFilesize=ftell(file);
		LOGV("chenhl initStreams mFilesize:%d",mFilesize); 
		fclose(file);
	}
	
	//add by chenhl end
	 
	int err = avformat_open_input(&mFormatCtx, mFilename, NULL, NULL);
	if (err < 0) {
		LOGV("chenhl initStreams 2 err:%d:", err);
		mFormatCtx = NULL;
		print_error_ex(mFilename, err);
		return -1;
	}

	err = avformat_find_stream_info(mFormatCtx, NULL);
	if (err < 0) {
		LOGE("chenhl %s: could not find codec parameters\n", mFilename);
		return -1;
	}

	for (int i = 0; i < mFormatCtx->nb_streams; i++)
		mFormatCtx->streams[i]->discard = AVDISCARD_ALL;
	st_index[AVMEDIA_TYPE_AUDIO] = av_find_best_stream(mFormatCtx,
			AVMEDIA_TYPE_AUDIO, -1, st_index[AVMEDIA_TYPE_VIDEO], NULL, 0);

	if (mFormatCtx->duration != AV_NOPTS_VALUE
			&& mFormatCtx->start_time != AV_NOPTS_VALUE) {
		int hours, mins, secs, us;
		mDurationUs = mFormatCtx->duration * 1000000 / AV_TIME_BASE;
		LOGV("chenhl file startTime: %lld", mDurationUs);

		secs = mDurationUs / AV_TIME_BASE;
		us = (mDurationUs % AV_TIME_BASE) * 1000000 / AV_TIME_BASE;
		mins = secs / 60;
		secs %= 60;
		hours = mins / 60;
		mins %= 60;
		LOGI("chenhl the duration is %02d:%02d:%02d.%02d", hours, mins, secs, (100 * us) / AV_TIME_BASE);
	}

	int audio_ret = 0;
	if (st_index[AVMEDIA_TYPE_AUDIO] >= 0)
		audio_ret = streamComponentOpen(st_index[AVMEDIA_TYPE_AUDIO]);
	if (audio_ret < 0) {
		LOGE("chenhl %s: could not open codecs\n", mFilename);
		return -1;
	}

	return 0;
}

void AVDecoder::deInitStreams() {
	if (mAudioStreamIdx >= 0)
		streamComponentClose(mAudioStreamIdx);

	if (mFormatCtx) {
		avformat_close_input(&mFormatCtx);
		mFormatCtx = NULL;
	}
	if (mCustomIOContext) {
		LOGV("mIOBuffer free:%p", mCustomIOContext->buffer);
		av_freep(&mCustomIOContext->buffer);
		av_freep(&mCustomIOContext);
	}

	mAudioStream = NULL;
	mAudioStreamIdx = -1;
	if (mFilename) {
		free(mFilename);
		mFilename = NULL;
	}

	/*audio queue, video queue should end and destroyed*/
	assert(!mAudioQInited);
	assert(mAudioFrame == NULL);
}

int AVDecoder::streamComponentOpen(int stream_index) {
	LOGI("chenhl stream_index: %d", stream_index);
	if (stream_index < 0 || stream_index >= mFormatCtx->nb_streams)
		return -1;
	AVCodecContext *avctx = mFormatCtx->streams[stream_index]->codec;
	mFormatCtx->streams[stream_index]->discard = AVDISCARD_DEFAULT;

	char tagbuf[32];
	av_get_codec_tag_string(tagbuf, sizeof(tagbuf), avctx->codec_tag);
	LOGV("zll Tag %s/0x%08x with codec id '%d'\n", tagbuf, avctx->codec_tag, avctx->codec_id);

	AVCodec *codec = avcodec_find_decoder(avctx->codec_id);
	AVDictionary *opts = NULL;
	AVDictionaryEntry *t;
	avctx->flags2 |= CODEC_FLAG2_FAST; //TEST:
	if (!av_dict_get(opts, "threads", NULL, 0)) {
		LOGI("set avcodec threads count: 1");
		av_dict_set(&opts, "threads", "1", 0);
	}
	if (!codec || avcodec_open2(avctx, codec, &opts) < 0) {
		LOGE("avcodec open failed");
		av_dict_free(&opts);
		return -1;
	}
	if ((t = av_dict_get(opts, "", NULL, AV_DICT_IGNORE_SUFFIX))) {
		av_log(NULL, AV_LOG_ERROR, "Option %s not found.ignore\n", t->key);
	}
	av_dict_free(&opts);

	switch (avctx->codec_type) {
	case AVMEDIA_TYPE_VIDEO:
		break;
	case AVMEDIA_TYPE_AUDIO:
		mAudioStreamIdx = stream_index;
		mAudioStream = mFormatCtx->streams[stream_index];
		mAudioCodecContext = mFormatCtx->streams[stream_index]->codec;
		mAudioFrame = avcodec_alloc_frame();
		mAudioChannels = mAudioCodecContext->channels;
		mAudioSampleRate = mAudioCodecContext->sample_rate;
		mAudioChannelLayout = mAudioCodecContext->channel_layout;
		packet_queue_init(&mAudioQ);
		mAudioQInited = true;
		LOGI(
				"audio channels:%d, audio sample rate:%d", mAudioChannels, mAudioSampleRate);
		break;
	}
	return 0;
}

int AVDecoder::streamComponentClose(int stream_index) {
	AVCodecContext *avctx;

	if (stream_index < 0 || stream_index >= mFormatCtx->nb_streams)
		return 0;
	avctx = mFormatCtx->streams[stream_index]->codec;
	mFormatCtx->streams[stream_index]->discard = AVDISCARD_ALL;
	//though not alloc by us,but we open it, we must close
	//to free resource when open
	avcodec_close(avctx);

	switch (avctx->codec_type) {
	case AVMEDIA_TYPE_VIDEO:
		break;
	case AVMEDIA_TYPE_AUDIO:
		LOGV("packet_queue_abort audioq");
		packet_queue_destroy(&mAudioQ);
		mAudioQInited = false;
		if (mAudioFrame)
			avcodec_free_frame(&mAudioFrame);
		mAudioStream = NULL;
		mAudioCodecContext = NULL;
		mAudioStreamIdx = -1;
		mAudioChannels = 0;
		mAudioSampleRate = 0;
		mRefAudioPTS = AV_NOPTS_VALUE;
		if (mAudioPacket->data)
			av_free_packet(mAudioPacket);
		memset(mTmpAudioPacket, 0, sizeof(*mTmpAudioPacket));
		break;
	default:
		break;
	}

	return 0;
}

bool AVDecoder::ReadAudio(AudioFrame *aFrame) {
	//LOGE("chenhl readaudio start");
	AVPacket &pkt = *mAudioPacket;
	AVPacket &tmpPkt = *mTmpAudioPacket;
	bool seeking = false;
	bool waitKeyPkt = false;
	int64_t pktTS = AV_NOPTS_VALUE;
	int64_t seekPos = AV_NOPTS_VALUE;
	int64_t timeUs;
	bool key;
	int gotFrame = 0;

	for (;;) {
		while (tmpPkt.size > 0 && !seek_ok) {
			avcodec_get_frame_defaults(mAudioFrame);
			int len1 = avcodec_decode_audio4(mAudioCodecContext, mAudioFrame,
					&gotFrame, &tmpPkt);
			if (len1 < 0) {
				LOGW("decode audio account error, skip");
				tmpPkt.size = 0;
				break;
			}

			tmpPkt.data += len1;
			tmpPkt.size -= len1;

			if (!gotFrame)
				continue;

			//LOGI("audio frame sample rate:%d, codec:%d", mAudioFrame->sample_rate, mAudioCodecContext->sample_rate);
			int oldSampleRate = mAudioSampleRate;
			uint64_t oldChannelLayout = mAudioChannelLayout;
			int oldChannels = mAudioChannels;
			int newChannels = av_get_channel_layout_nb_channels(
					mAudioFrame->channel_layout);
			if (mAudioFrame->sample_rate != mAudioSampleRate
					|| mAudioFrame->channel_layout != mAudioChannelLayout
					|| newChannels != mAudioChannels) {
				LOGW("audio format changed(sample rate,channel layout,channels):(%d,%llu,%d) -> (%d,%llu,%d)", mAudioSampleRate, (unsigned long long)mAudioChannelLayout, mAudioChannels, mAudioFrame->sample_rate, (unsigned long long)mAudioFrame->channel_layout, newChannels);
				mAudioSampleRate = mAudioFrame->sample_rate;
				mAudioChannelLayout = mAudioFrame->channel_layout;
				mAudioChannels = newChannels;
			}
			int data_size = av_samples_get_buffer_size(NULL,
					mAudioCodecContext->channels, mAudioFrame->nb_samples,
					mAudioCodecContext->sample_fmt, 1);
			if (mRefAudioPTS == AV_NOPTS_VALUE) {
				int64_t pts;
				if (mAudioFrame->pkt_pts != AV_NOPTS_VALUE)
					pts = mAudioFrame->pkt_pts;
				else if (mAudioFrame->pkt_dts != AV_NOPTS_VALUE)
					pts = mAudioFrame->pkt_dts;
				else {
					LOGE("audio frame pts unkonw, drop");
					tmpPkt.size = 0;
					break;
				}
				mRefAudioPTS = pts;
				LOGI("audio pts start with:%lld", (long long)mRefAudioPTS);
			}
			uint8_t *dst = NULL;
			int dstlen = 0;
			int outSamples;
			if ((outSamples = AudioResampleConvert(&mCachedCtx, mAudioFrame,
					&dst, &dstlen)) < 0) {
				LOGE("failed to resample audio frame,drop this frame");
				break;
			}
			int64_t ptsUs = mRefAudioPTS * 1000000ll
					* av_q2d(mAudioStream->time_base)
					+ mSamples * 1000000ll / mAudioFrame->sample_rate;
			AFrameToAudioFrame(mFormatCtx, mAudioStream, ptsUs,
					mSourceTimeOffsetUs, dst, dstlen, mAudioChannels,
					mAudioSampleRate, aFrame);
			dst = NULL;
			if (oldSampleRate == mAudioFrame->sample_rate) {
				mSamples += outSamples;
			} else {
				int64_t ptsOffsetUs = mSamples * 1000000ll / oldSampleRate;
				int64_t ptsOffset = ptsOffsetUs
						/ av_q2d(mAudioStream->time_base) / 1000000ll;
				mRefAudioPTS += ptsOffset;
				mSamples = 0;
				LOGW(
						"audio sample rate changed,will recount samples since ref pts(%lld,%lld)", (long long)mRefAudioPTS, (long long)ptsOffset);
			}
			LOGI("ReadAudio success!");
			return true;
		}

		retry:
		seek_ok = false;
		if (pkt.data)
			av_free_packet(&pkt);
		memset(&tmpPkt, 0, sizeof(tmpPkt));
		bool isOk = packet_queue_get(&mAudioQ, &pkt);
		if (!isOk)
			return false;

		if (pkt.data == flush_pkt.data) {
			LOGV("chenhl read audio flush pkt");
			avcodec_flush_buffers(mAudioCodecContext);
			goto retry;
		}

		key = pkt.flags & AV_PKT_FLAG_KEY ? true : false;
		pktTS = pkt.pts;
		// use dts when AVI
		if (pkt.pts == AV_NOPTS_VALUE)
			pktTS = pkt.dts;

		tmpPkt = pkt;

	} //for

	//SHOULD NOT here
	return false;
}

bool AVDecoder::Seek(int64_t seekTimeUs) {
	pthread_mutex_lock(&mCommandLock);
	pthread_mutex_lock(&mLock);
	mSeekPos = seekTimeUs;
	mSeekRes = false;
	mSeekReq = true;
	pthread_cond_broadcast(&mCond);
	while (mReaderThreadStarted && mSeekReq)
		pthread_cond_wait(&mCond, &mLock);

	bool seekRes = mSeekRes;
	pthread_mutex_unlock(&mLock);
	pthread_mutex_unlock(&mCommandLock);
	return seekRes;
}

bool AVDecoder::isPacketQueueFull() {
	int totalSize = 0;
	bool audioMinReach = true;
	if (mAudioStreamIdx >= 0 && mAudioQ.size <= MIN_AUDIOQ_SIZE)
		audioMinReach = false;

	if (totalSize > MAX_QUEUE_SIZE) {
		return true;
	}

	if (audioMinReach)
		return true;

	return false;
}

int AVDecoder::decode_interrupt_cb(void *ctx) {
	AVDecoder *decoder = (AVDecoder *) ctx;
	return decoder->mAbortRequest || exit_interrupt_check();
}


int AVDecoder::download_size_cb(void *ctx) {
	AVDecoder *decoder = (AVDecoder *) ctx;
	LOGI("chenhl download_size_cb:%d",decoder->mDownloadSize);
	return decoder->mDownloadSize;
}

int AVDecoder::download_size_cb(void *ctx,int cursize) {
	AVDecoder *decoder = (AVDecoder *) ctx;
	LOGI("chenhl download_size_cb 1:%d,cursize:%d",decoder->mDownloadSize,cursize);
//	LOGI("chenhl download_size_cb 2 cursize:%d",decoder->mCurSize);
//	LOGV("chenhl download_size_cb 3 mFilesize:%d",decoder->mFilesize);
	if(decoder->mFilesize ==decoder->mDownloadSize||decoder->mSeekReq){
		return 0;
	}
	if(decoder->mDownloadSize>0 && decoder->mCurSize+cursize>decoder->mDownloadSize){
		return 1;
	}
	return 0;
}


///////////////////////////////////////////////////////////////////////////////////

struct DecoderPriv {
	AVDecoder *decoder; //address is the same as DecoderPriv
	pthread_rwlock_t rwlock;
	AudioFrame *cached_audioframe;
};

static void DestroyDecoder(Decoder *aDecoder) {
	struct DecoderPriv *priv = (struct DecoderPriv *) aDecoder->mPrivate;
	delete priv->decoder;
	pthread_rwlock_destroy(&priv->rwlock);
	free(priv);
	memset(aDecoder, 0, sizeof(*aDecoder));
}

static void GetDuration(Decoder *aDecoder, int64_t *durationUs) {
	struct DecoderPriv *priv = (struct DecoderPriv *) aDecoder->mPrivate;

	pthread_rwlock_rdlock(&priv->rwlock);
	if (priv->decoder)
		priv->decoder->GetDuration(durationUs);
	pthread_rwlock_unlock(&priv->rwlock);
}

static void GetBufferedTime(Decoder *aDecoder, int64_t *timeUs) {
	struct DecoderPriv *priv = (struct DecoderPriv *) aDecoder->mPrivate;

	pthread_rwlock_rdlock(&priv->rwlock);
	if (priv->decoder)
		priv->decoder->GetBufferedTimeUs(timeUs);
	pthread_rwlock_unlock(&priv->rwlock);
}

static void GetAudioParameters(Decoder *aDecoder, int32_t *numChannels,
		int32_t *sampleRate) {
	struct DecoderPriv *priv = (struct DecoderPriv *) aDecoder->mPrivate;

	pthread_rwlock_rdlock(&priv->rwlock);
	if (priv->decoder)
		priv->decoder->GetAudioParameters(numChannels, sampleRate);
	pthread_rwlock_unlock(&priv->rwlock);
}

static bool HasAudio(Decoder *aDecoder) {
	struct DecoderPriv *priv = (struct DecoderPriv *) aDecoder->mPrivate;
	bool ret = false;

	pthread_rwlock_rdlock(&priv->rwlock);
	if (priv->decoder)
		ret = priv->decoder->HasAudio();
	pthread_rwlock_unlock(&priv->rwlock);

	return ret;
}

static bool Seek(Decoder *aDecoder, int64_t timeUs,int64_t *positionUs) {
	struct DecoderPriv *priv = (struct DecoderPriv *)aDecoder->mPrivate;
	pthread_rwlock_wrlock(&priv->rwlock);
	delete priv->cached_audioframe;
	priv->cached_audioframe = NULL;
	seek_ok = false;
	if (!priv->decoder->Seek(timeUs)) {
		LOGE("seek failed");
		pthread_rwlock_unlock(&priv->rwlock);
		return false;
	}
	seek_ok = true;
	AudioFrame *aframe = new AudioFrame();
	if (!priv->decoder->ReadAudio(aframe)) {
		LOGE("pre-read audio frame for seek request failed");
		delete aframe;
		pthread_rwlock_unlock(&priv->rwlock);
		return false;
	}
	*positionUs = aframe->mTimeUs;
	priv->cached_audioframe = aframe;
	pthread_rwlock_unlock(&priv->rwlock);
	return true;
}

static bool ReadAudio(Decoder *aDecoder, AudioFrame *aFrame) {
	struct DecoderPriv *priv = (struct DecoderPriv *) aDecoder->mPrivate;
	bool ret = false;
	pthread_rwlock_rdlock(&priv->rwlock);
	if (priv->cached_audioframe) {
		*aFrame = std::move(*priv->cached_audioframe);
		delete priv->cached_audioframe;
		priv->cached_audioframe = NULL;
		pthread_rwlock_unlock(&priv->rwlock);
		return true;
	}
	if (priv->decoder)
		ret = priv->decoder->ReadAudio(aFrame);
	pthread_rwlock_unlock(&priv->rwlock);
	return ret;
}

static void setDownloadSize(Decoder *aDecoder,int size){
//	LOGI("chenhl decpder setDownloadSize 1:%d",size);
	struct DecoderPriv *priv = (struct DecoderPriv *) aDecoder->mPrivate;
	pthread_rwlock_wrlock(&priv->rwlock);
//	LOGI("chenhl decpder setDownloadSize 2:%d",size);
	if (priv->decoder)
			priv->decoder->setDownloadSize(size);
	
//	LOGI("chenhl decpder setDownloadSize 3:%d",size);
	pthread_rwlock_unlock(&priv->rwlock);
}

extern "C" VSEA_EXPORT
bool CreateAVDecoderImpl(Decoder *aDecoder, const char *psz_uri, int64_t restoreTimeUs)
{
	AVDecoder *decoder = NULL;
	bool needseek = false;
	decoder = new AVDecoder(psz_uri, &aDecoder->mVar);
	if(restoreTimeUs > 0)
	needseek = true;

	if(!decoder->Init())
	{
		LOGE("chenhl Init AVDecoder failed");
		delete decoder;
		return false;
	}

	LOGE("chenhl CreateAVDecoderImpl 1");
	struct DecoderPriv *priv = (struct DecoderPriv *)malloc(sizeof(*priv));
	priv->decoder = decoder;
	priv->cached_audioframe = NULL;
	pthread_rwlock_init(&priv->rwlock, NULL);

	memset(aDecoder, 0, sizeof(*aDecoder));
	aDecoder->mVar.resume_time = -1;
	aDecoder->mPrivate = priv;
	aDecoder->DestroyDecoder = DestroyDecoder;
	aDecoder->GetDuration = GetDuration;
	aDecoder->GetBufferedTimeUs = GetBufferedTime;
	aDecoder->GetAudioParameters = GetAudioParameters;
	aDecoder->HasAudio = HasAudio;
	aDecoder->Seek = Seek;
	aDecoder->ReadAudio = ReadAudio;
	aDecoder->setDownloadSize=setDownloadSize;
	//TODO:get buffer duration
	LOGI("chenhl Create AVDecoder successed");

	if(needseek)
	{
		int64_t positionUs = 0;
		if(aDecoder->Seek(aDecoder, restoreTimeUs,&positionUs))
		{
			LOGI("chenhl resume playback from: %lld(%.2fs) successed",
					(long long)restoreTimeUs, restoreTimeUs / 1E6);
		}
		else
		{
			LOGE("chenhl resume playback from: %lld(%.2fs) failed",
					(long long)restoreTimeUs, restoreTimeUs / 1E6);
		}
	}

	LOGE("chenhl CreateAVDecoderImpl 2");
	return true;
}

