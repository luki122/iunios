#ifndef _AV_DECODER_H_
#define _AV_DECODER_H_
#include <pthread.h>
#include <stdlib.h>
#include "mediadef.h"
#include <stdio.h>

struct AVPacket ;
struct AVPacketList;
struct AVFormatContext;
struct AVStream;
struct AVIOContext;
struct AVCodecContext;
struct AVFrame;

typedef struct PacketQueue {
    AVPacketList *first_pkt, *last_pkt;
    int nb_packets;
    int size;
    bool end;
    pthread_mutex_t mutex;
    pthread_cond_t cond;
} PacketQueue;

class AVDecoder
{
public:
    bool ReadAudio(AudioFrame *aFrame);
    bool Seek(int64_t seekTimeUs);

    AVDecoder(const char *psz_uri, Var *var);
    ~AVDecoder();

    bool Init();

    void GetDuration(int64_t *durationUs)
    {
            *durationUs = mDurationUs;
    }

    void GetBufferedTimeUs(int64_t *timeUs)
    {
            *timeUs = -1;
    }

    void GetAudioParameters(int32_t *numChannels, int32_t *sampleRate)
    {
        *numChannels = mAudioChannels;
        *sampleRate = mAudioSampleRate;
    }

    bool HasAudio()
    {
        return mAudioStreamIdx != -1;
    }
	
	void setDownloadSize(int size){
		mDownloadSize = size;
	}

	void getDownloadSize(int *size){
		*size = mDownloadSize;
	}
	
    struct ResampleContext
    {
        struct AVAudioResampleContext *swrCtx;
        uint64_t channelLayout;
        int format;
        int sampleRate;
    };

private:
    Var *mDecoderVar; /*weak ref*/
    int64_t mOffset;
    char *mUri;
    int32_t mAudioChannels;
    int32_t mAudioSampleRate;
    uint64_t mAudioChannelLayout;
    int mPixfmt; /* AVPixelFormat */
    int64_t mDurationUs;
    int64_t mSourceTimeOffsetUs;
    int64_t mRefAudioPTS;
    int64_t mSamples; //sample count since got mRefAudioPTS
    enum { INIT_FAIL = -1, NOT_INIT = 0, INIT_OK = 1} mInitStatus;

    AVFormatContext *mFormatCtx;
    int mAudioStreamIdx;
    AVStream *mAudioStream;
    AVCodecContext *mAudioCodecContext;
    AVFrame *mAudioFrame;
    AVPacket *mAudioPacket;
    AVPacket *mTmpAudioPacket;
    AVIOContext *mCustomIOContext; /*if we use custom io,we need free(NOT close) it manually*/
    static const int smIOBufferSize = 32768;
    char *mFilename;

    // if we may send some command to ReaderThread in more than one thread at the sametime,
    // such as seek,
    // need hold mCommandLock to make sure thread-safe(as when condition wait with mCond, it will
    // unlock mLock automaticly)
    pthread_mutex_t mCommandLock;
    pthread_mutex_t mLock;
    pthread_cond_t mCond;
    bool mAbortRequest;
    bool mSeekReq;
    bool mSeekRes;
    int64_t mSeekPos;

    PacketQueue mAudioQ;
    bool mAudioQInited;

    bool mReaderThreadStarted;
    pthread_t mReaderThread;

	int mDownloadSize;
	int mCurSize;
	int mFilesize; 
	
    struct ResampleContext mCachedCtx;

private:
    int startReaderThread();
    void stopReaderThread();
    static void *ReaderWrapper(void *me);
    void readerEntry();

    static int decode_interrupt_cb(void *ctx);
	static int download_size_cb(void *ctx,int cursize);//add by chenhl
	static int download_size_cb(void *ctx);//add by chenhl
	
    int initStreams();
    void deInitStreams();
    int streamComponentOpen(int streamIndex);
    int streamComponentClose(int streamIndex);
    bool isPacketQueueFull();
private:
    AVDecoder(const AVDecoder &);
    AVDecoder &operator=(const AVDecoder &);
};

#endif
