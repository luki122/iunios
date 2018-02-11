#include "DlAudioTrack.h"
#include "BackportsLog.h"

#include <stdlib.h>
#include <pthread.h>
// for native audio
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

// for native asset manager
#include <sys/types.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#define LOG_TAG "AuroraPlayer-AudioTrack"

typedef void (*myCallback_t)(int event, void* user, void *info);
static myCallback_t mycall;
// engine interfaces
static SLObjectItf engineObject = NULL;
static SLEngineItf engineEngine;

// buffer queue player interfaces
static SLObjectItf bqPlayerObject = NULL;
static SLPlayItf bqPlayerPlay;
static SLAndroidSimpleBufferQueueItf bqPlayerBufferQueue;
static SLEffectSendItf bqPlayerEffectSend;
static SLMuteSoloItf bqPlayerMuteSolo;
static SLVolumeItf bqPlayerVolume;
// pointer and size of the next player buffer to enqueue, and number of remaining buffers
static void *nextBuffer;
static int nextSize;
static int nextCount;
static int i_rate;
static void* myuser;

#define MAX_AUDIOTRACK_SIZE 256;
// aux effect on the output mix, used by the buffer queue player
static const SLEnvironmentalReverbSettings reverbSettings =
		SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;

// output mix interfaces
static SLObjectItf outputMixObject = NULL;
static SLEnvironmentalReverbItf outputMixEnvironmentalReverb = NULL;
void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
	mycall(1,myuser,context);
}
void createEngine() {
	SLresult result;

	// create engine
	result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
	assert(SL_RESULT_SUCCESS == result);
	(void) result;

	// realize the engine
	result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
	assert(SL_RESULT_SUCCESS == result);
	(void) result;

	// get the engine interface, which is needed in order to create other objects
	result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE,
			&engineEngine);
	assert(SL_RESULT_SUCCESS == result);
	(void) result;

	// create output mix, with environmental reverb specified as a non-required interface
	const SLInterfaceID ids[1] = { SL_IID_ENVIRONMENTALREVERB };
	const SLboolean req[1] = { SL_BOOLEAN_FALSE };
	result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1,
			ids, req);
	assert(SL_RESULT_SUCCESS == result);
	(void) result;

	// realize the output mix
	result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
	assert(SL_RESULT_SUCCESS == result);
	(void) result;

	// get the environmental reverb interface
	// this could fail if the environmental reverb effect is not available,
	// either because the feature is not present, excessive CPU load, or
	// the required MODIFY_AUDIO_SETTINGS permission was not requested and granted
	result = (*outputMixObject)->GetInterface(outputMixObject,
			SL_IID_ENVIRONMENTALREVERB, &outputMixEnvironmentalReverb);
	if (SL_RESULT_SUCCESS == result) {
		result =
				(*outputMixEnvironmentalReverb)->SetEnvironmentalReverbProperties(
						outputMixEnvironmentalReverb, &reverbSettings);
		(void) result;
	}
}
void createBufferQueueAudioPlayer() {
	SLresult result;

	// configure audio source
	SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {
			SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2 };
//	SLDataFormat_PCM format_pcm = { SL_DATAFORMAT_PCM, 1, SL_SAMPLINGRATE_8,
//			SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
//			SL_SPEAKER_FRONT_CENTER, SL_BYTEORDER_LITTLEENDIAN };
	SLDataFormat_PCM format_pcm;
	format_pcm.formatType = SL_DATAFORMAT_PCM;
	format_pcm.numChannels = 2;
	format_pcm.samplesPerSec = ((SLuint32) i_rate * 1000);
	format_pcm.bitsPerSample = SL_PCMSAMPLEFORMAT_FIXED_16;
	format_pcm.containerSize = SL_PCMSAMPLEFORMAT_FIXED_16;
	format_pcm.channelMask = SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT;
	format_pcm.endianness = SL_BYTEORDER_LITTLEENDIAN;

	SLDataSource audioSrc = { &loc_bufq, &format_pcm };

	// configure audio sink
	SLDataLocator_OutputMix loc_outmix = { SL_DATALOCATOR_OUTPUTMIX,
			outputMixObject };
	SLDataSink audioSnk = { &loc_outmix, NULL };

	// create audio player
	const SLInterfaceID ids[3] = { SL_IID_BUFFERQUEUE, SL_IID_EFFECTSEND,
	/*SL_IID_MUTESOLO,*/SL_IID_VOLUME };
	const SLboolean req[3] = { SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE,
	/*SL_BOOLEAN_TRUE,*/SL_BOOLEAN_TRUE };
	result = (*engineEngine)->CreateAudioPlayer(engineEngine, &bqPlayerObject,
			&audioSrc, &audioSnk, 3, ids, req);
	assert(SL_RESULT_SUCCESS == result);
	(void) result;

	// realize the player
	result = (*bqPlayerObject)->Realize(bqPlayerObject, SL_BOOLEAN_FALSE);
	assert(SL_RESULT_SUCCESS == result);
	(void) result;

	// get the play interface
	result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_PLAY,
			&bqPlayerPlay);
	assert(SL_RESULT_SUCCESS == result);
	(void) result;

	// get the buffer queue interface
	result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_BUFFERQUEUE,
			&bqPlayerBufferQueue);
	assert(SL_RESULT_SUCCESS == result);
	(void) result;

	// register callback on the buffer queue
	result = (*bqPlayerBufferQueue)->RegisterCallback(bqPlayerBufferQueue,
			bqPlayerCallback, NULL);
	assert(SL_RESULT_SUCCESS == result);
	(void) result;

	// get the effect send interface
	result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_EFFECTSEND,
			&bqPlayerEffectSend);
	assert(SL_RESULT_SUCCESS == result);
	(void) result;

	 result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PLAYING);
	 assert(SL_RESULT_SUCCESS == result);
	 (void) result;
#if 0   // mute/solo is not supported for sources that are known to be mono, as this is
	// get the mute/solo interface
	result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_MUTESOLO, &bqPlayerMuteSolo);
	assert(SL_RESULT_SUCCESS == result);
	(void)result;
#endif

	// get the volume interface
	result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_VOLUME,
			&bqPlayerVolume);
	assert(SL_RESULT_SUCCESS == result);
	(void) result;

	// set the player's state to playing
	result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PLAYING);
	assert(SL_RESULT_SUCCESS == result);
	(void) result;
}
bool selectClip(void * data, int size) {
	nextBuffer = data;
	nextSize = size;
	if (size >= 0) {
		// here we only enqueue one buffer because it is a long clip,
		// but for streaming playback we would typically enqueue at least 2 buffers to start
		SLresult result;
		result = (*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue, data,
				size);
		if (SL_RESULT_SUCCESS != result) {
			return JNI_FALSE;
		}
	}
	return 0;

}
struct AudioTrackDlInfo {
	enum status {
		OK = 0, NO_INIT, DLOPEN_ERR, DLSYM_ERR
	};
	status dl_status;
	pthread_mutex_t lock;

	AudioTrackDlInfo() :
			dl_status(NO_INIT) {
		pthread_mutex_init(&lock, NULL);
	}

	~AudioTrackDlInfo() {
		if (dl_status == OK) {
			LOGV("zll ~AudioTrackDlInfo()");
			dl_status = NO_INIT;
		}
		pthread_mutex_unlock(&lock);
	}

	void open() {
		if (dl_status == NO_INIT) {
			pthread_mutex_lock(&lock);
			if (dl_status == NO_INIT)
				open_l();
			pthread_mutex_unlock(&lock);
		}
	}

private:
	void open_l() {
		LOGV("zll AudioTrackDlInfo()");
		createEngine();
		createBufferQueueAudioPlayer();
	}

};
static AudioTrackDlInfo g_AudioTrackDlInfo;

DlAudioTrack::DlAudioTrack(int streamType, uint32_t sampleRate, int format,
		int channelMask, int frameCount, uint32_t flags,
		DlAudioTrack::callback_t cbf, void* user, int notificationFrames,
		int sessionId) :
		m_audioTrack(NULL) {
	LOGV("zll DlAudioTrack::DlAudioTrack");
	mycall = cbf;
	myuser = user;
	i_rate = sampleRate;
	g_AudioTrackDlInfo.open();
}

DlAudioTrack::~DlAudioTrack() {
	LOGV("zll DlAudioTrack::~DlAudioTrack");
}

void DlAudioTrack::start(void *data, int size) {
	selectClip(data, size);
}

void DlAudioTrack::stop() {
	LOGV("zll DlAudioTrack::stop");
	(*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PAUSED);

	if (bqPlayerObject != NULL) {
		(*bqPlayerObject)->Destroy(bqPlayerObject);
		bqPlayerObject = NULL;
		bqPlayerPlay = NULL;
		bqPlayerBufferQueue = NULL;
		bqPlayerEffectSend = NULL;
		bqPlayerMuteSolo = NULL;
		bqPlayerVolume = NULL;
	}

	// destroy output mix object, and invalidate all associated interfaces
	if (outputMixObject != NULL) {
		(*outputMixObject)->Destroy(outputMixObject);
		outputMixObject = NULL;
		outputMixEnvironmentalReverb = NULL;
	}

	// destroy engine object, and invalidate all associated interfaces
	if (engineObject != NULL) {
		(*engineObject)->Destroy(engineObject);
		engineObject = NULL;
		engineEngine = NULL;
	}
}

void DlAudioTrack::flush() {
}

void DlAudioTrack::pause() {
	SLresult result;

	LOGV("zll DlAudioTrack::pause");
	if (NULL != bqPlayerPlay) {
		LOGV("zll DlAudioTrack::pause 1");
		// set the player's state
		result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PAUSED);
		assert(SL_RESULT_SUCCESS == result);
		(void)result;
	}
}
void DlAudioTrack::resume() {
	SLresult result;
	LOGV("zll DlAudioTrack::resume");
	if (NULL != bqPlayerPlay) {
		LOGV("zll DlAudioTrack::resume 1");
		// set the player's state
		result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PLAYING);
		assert(SL_RESULT_SUCCESS == result);
		(void)result;
	}
}

void DlAudioTrack::setVolume(float vol){
	SLresult result;
	if(NULL!=bqPlayerVolume){
		result =(*bqPlayerVolume)->SetVolumeLevel(bqPlayerVolume,vol);
		assert(SL_RESULT_SUCCESS == result);
		(void)result;
	}
}

int DlAudioTrack::initCheck() const {
	return 0;
}

uint32_t DlAudioTrack::latency() const {
	return 0;
}

int DlAudioTrack::frameSize() const {
	return 0;
}
