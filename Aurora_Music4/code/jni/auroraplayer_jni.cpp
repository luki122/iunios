/*
 **
 ** Copyright 2007, The Android Open Source Project
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */

#include "bin/com_android_auroramusic_local_AuroraPlayer.h"
#include "BackportsLog.h"
#include "AuroraListener.h"
#include "auroraplayer.h"
#include "common/jnihelp.h"
#include "common/wrap.h"

#define LOG_TAG "AuroraPlayer-jni"

#include <stdio.h>
#include <assert.h>
#include <limits.h>
#include <unistd.h>
#include <fcntl.h>

struct MetaFlags {
	static const int CAN_PAUSE = 1;
	static const int CAN_SEEK = 1 << 1;
	static const int CAN_SEEK_FORWARD = 1 << 2;
	static const int CAN_SEEK_BACKWARD = 1 << 3;
};

// ----------------------------------------------------------------------------
extern int OffsetTypeSize();
int __android_version;

struct fields_t {
	jfieldID context;
	jfieldID java_surface_ref;

	jmethodID post_event;
};
static fields_t fields;

// global java vm, also used in other module
extern JavaVM *g_vm;

class JNIMediaPlayerListener: public ListenerInterface {
public:
	JNIMediaPlayerListener(JNIEnv *env, jobject thiz, jobject weak_thiz);
	virtual ~JNIMediaPlayerListener();
	virtual void sendEvent(int msg, int ext1, int ext2);
private:
	JNIMediaPlayerListener();
	jclass mClass;     // Reference to AwesomePlayer class
	jobject mObject;    // Weak ref to AwesomePlayer Java object to call on
};

JNIMediaPlayerListener::JNIMediaPlayerListener(JNIEnv *env, jobject thiz,
		jobject weak_thiz) {

	// Hold onto the MediaPlayer class for use in calling the static method
	// that posts events to the application thread.
	jclass clazz = env->GetObjectClass(thiz);
	if (clazz == NULL) {
		LOGE("Can't find com/aurora/MediaPlayer");
		jclass exc = env->FindClass("java/lang/Exception");
		env->ThrowNew(exc, "Unable to create JNIMediaPlayerListener");
		env->DeleteLocalRef(exc);
		return;
	}
	mClass = (jclass) env->NewGlobalRef(clazz);

	// We use a weak reference so the AwesomePlayer object can be garbage collected.
	// The reference is only used as a proxy for callbacks.
	mObject = env->NewGlobalRef(weak_thiz);
}

JNIMediaPlayerListener::~JNIMediaPlayerListener() //BUG:here?
{
	JNIEnv *env = getEnv();
	// remove global references
	env->DeleteGlobalRef(mObject);
	env->DeleteGlobalRef(mClass);

}

/* this function attached jvm in a non-attach-jvm thread,
 * it needs detach jvm before thread exit.
 * we use a pthread_key_t to make sure it do this*/
void JNIMediaPlayerListener::sendEvent(int msg, int ext1, int ext2) {
	JNIEnv *env = getEnv();

	env->CallStaticVoidMethod(mClass, fields.post_event, mObject, msg, ext1,
			ext2, NULL);
}

// ----------------------------------------------------------------------------

// ----------------------------------------------------------------------------

static AuroraPlayer *getMediaPlayer(JNIEnv *env, jobject thiz) {
	AuroraPlayer * const p = (AuroraPlayer *) env->GetLongField(thiz,
			fields.context);
	return p;
}

void setMediaPlayer(JNIEnv *env, jobject thiz, AuroraPlayer *player) {
	AuroraPlayer *old = (AuroraPlayer *) env->GetLongField(thiz,
			fields.context);
	if (old != NULL) {
		LOGW("delete old player instance not destroyed.");
		delete old;
	}
	env->SetLongField(thiz, fields.context, (long) player);
}

// If exception is NULL and opStatus is not OK, this method sends an error
// event to the client application; otherwise, if exception is not NULL and
// opStatus is not OK, this method throws the given exception to the client
// application.
static void process_media_player_call(JNIEnv *env, jobject thiz, int opStatus,
		const char *exception, const char *message) {
	if (exception == NULL)    // Don't throw exception. Instead, send an event.
			{
		if (opStatus != 0) {
			AuroraPlayer *mp = getMediaPlayer(env, thiz);
			if (mp != 0)
				mp->notify(MEDIA_ERROR, opStatus, 0);
		}
	} else      // Throw exception!
	{
		if (opStatus == INVALID_OPERATION) {
			jniThrowException(env, "java/lang/IllegalStateException", NULL);
		} else if (opStatus == PERMISSION_DENIED) {
			jniThrowException(env, "java/lang/SecurityException", NULL);
		} else if (opStatus != 0) {
			if (strlen(message) > 230) {
				// if the message is too long, don't bother displaying the status code
				jniThrowException(env, exception, message);
			} else {
				char msg[256];
				// append the status code to the message
				sprintf(msg, "%s: status=0x%X", message, opStatus);
				jniThrowException(env, exception, msg);
			}
		}
	}
}

static void *getJavaSurfaceRef(JNIEnv *env, jobject thiz) {
	void * const p = (void *) env->GetLongField(thiz, fields.java_surface_ref);
	return p;
}

static void decJavaSurfaceRef(JNIEnv *env, jobject thiz) {
	AuroraPlayer *mp = getMediaPlayer(env, thiz);
	if (mp == NULL) {
		return;
	}

	//TODO:FIXME: could a global ref of java surface object make mNativeSurface not destroyed ?
	jobject ref = (jobject) getJavaSurfaceRef(env, thiz);
	if (ref != NULL) {
		env->DeleteGlobalRef(ref);
		env->SetLongField(thiz, fields.java_surface_ref, 0);
	}
}

void Java_com_android_auroramusic_local_AuroraPlayer__1setVideoSurface(
		JNIEnv *env, jobject thiz, jobject jsurface) {
}

static bool debugWait = false;

#define DEBUG_STOP \
do{ \
    FILE *fp = fopen("/sdcard/debug", "rb"); \
    if(fp) \
    { \
        debugWait = true; \
        fclose(fp); \
    } \
    else \
        debugWait = false; \
    while(debugWait) \
        sleep(1); \
}while(0);

void Java_com_android_auroramusic_local_AuroraPlayer__1setDataSource(
		JNIEnv *env, jobject thiz, jstring path, jobjectArray keys,
		jobjectArray values) {
	DEBUG_STOP
	;

	// we use __android_log_print instead LOGI to avoid
	// log disable by macro in BackportsLog.h
	__android_log_print(ANDROID_LOG_INFO, "AuroraPlayer",
			"auroraplayer build date:%s", __BUILD_DATE);
	AuroraPlayer *mp = getMediaPlayer(env, thiz);
	if (mp == NULL) {
		jniThrowException(env, "java/lang/IllegalStateException", NULL);
		return;
	}

	if (path == NULL) {
		jniThrowException(env, "java/lang/IllegalArgumentException", NULL);
		return;
	}

	const char *tmp = env->GetStringUTFChars(path, NULL);
	if (tmp == NULL)    // Out of memory
			{
		return;
	}

	LOGV("setDataSource: path %s", tmp);

	int opStatus = mp->setDataSource(tmp);

	process_media_player_call(env, thiz, opStatus, "java/io/IOException",
			"setDataSource failed.");

	env->ReleaseStringUTFChars(path, tmp);
	tmp = NULL;
}

void Java_com_android_auroramusic_local_AuroraPlayer_setDataSource(JNIEnv *env,
		jobject thiz, jstring path) {
	Java_com_android_auroramusic_local_AuroraPlayer__1setDataSource(env, thiz,
			path, NULL, NULL);
}

void Java_com_android_auroramusic_local_AuroraPlayer_prepare(JNIEnv *env,
		jobject thiz, jint startTimeMs) {
	LOGV("prepare");
	AuroraPlayer *mp = getMediaPlayer(env, thiz);
	if (mp == NULL) {
		jniThrowException(env, "java/lang/IllegalStateException", NULL);
		return;
	}

	process_media_player_call(env, thiz, mp->prepare(startTimeMs * 1000ll),
			"java/io/IOException", "Prepare failed.");
}

void Java_com_android_auroramusic_local_AuroraPlayer_prepareAsync(JNIEnv *env,
		jobject thiz, jint startTimeMs) {
	LOGV("prepareAsync");
	AuroraPlayer *mp = getMediaPlayer(env, thiz);
	if (mp == NULL) {
		jniThrowException(env, "java/lang/IllegalStateException", NULL);
		return;
	}

	process_media_player_call(env, thiz, mp->prepareAsync(startTimeMs * 1000ll),
			"java/io/IOException", "Prepare Async failed.");
}

void Java_com_android_auroramusic_local_AuroraPlayer__1start(JNIEnv *env,
		jobject thiz) {
	LOGV("start");
	AuroraPlayer *mp = getMediaPlayer(env, thiz);
	if (mp == NULL) {
		jniThrowException(env, "java/lang/IllegalStateException", NULL);
		return;
	}
	process_media_player_call(env, thiz, mp->play(), NULL, NULL);
}

void Java_com_android_auroramusic_local_AuroraPlayer__1stop(JNIEnv *env,
		jobject thiz) {
	LOGV("stop");
	AuroraPlayer *mp = getMediaPlayer(env, thiz);
	if (mp == NULL) {
		jniThrowException(env, "java/lang/IllegalStateException", NULL);
		return;
	}
	process_media_player_call(env, thiz, mp->pause(), NULL, NULL);
}

void Java_com_android_auroramusic_local_AuroraPlayer__1pause(JNIEnv *env,
		jobject thiz) {
	LOGV("pause");
	AuroraPlayer *mp = getMediaPlayer(env, thiz);
	if (mp == NULL) {
		jniThrowException(env, "java/lang/IllegalStateException", NULL);
		return;
	}
	process_media_player_call(env, thiz, mp->pause(), NULL, NULL);
}

jint Java_com_android_auroramusic_local_AuroraPlayer_getVideoWidth(JNIEnv *env,
		jobject thiz) {
	return 0;
}

jint Java_com_android_auroramusic_local_AuroraPlayer_getVideoHeight(JNIEnv *env,
		jobject thiz) {
	return 0;
}

jboolean Java_com_android_auroramusic_local_AuroraPlayer_isPlaying(JNIEnv *env,
		jobject thiz) {
	AuroraPlayer *mp = getMediaPlayer(env, thiz);
	if (mp == NULL) {
		jniThrowException(env, "java/lang/IllegalStateException", NULL);
		return false;
	}
	const jboolean is_playing = mp->isPlaying();

	return is_playing;
}

void Java_com_android_auroramusic_local_AuroraPlayer__1seekTo(JNIEnv *env,
		jobject thiz, int msec) {
	AuroraPlayer *mp = getMediaPlayer(env, thiz);
	if (mp == NULL) {
		jniThrowException(env, "java/lang/IllegalStateException", NULL);
		return;
	}
	LOGV("seekTo: %d(msec)", msec);
	process_media_player_call(env, thiz, mp->seekTo((int64_t) msec * 1000),
			NULL, NULL);
}

jint Java_com_android_auroramusic_local_AuroraPlayer__1getCurrentPosition(
		JNIEnv *env, jobject thiz) {
	AuroraPlayer *mp = getMediaPlayer(env, thiz);
	if (mp == NULL) {
		jniThrowException(env, "java/lang/IllegalStateException", NULL);
		return 0;
	}

	int msec;
	int64_t positionUs = 0;
	process_media_player_call(env, thiz, mp->getPosition(&positionUs), NULL,
			NULL);
	msec = positionUs > 0 ? (positionUs + 500) / 1000 : 0;
	return msec;
}

jint Java_com_android_auroramusic_local_AuroraPlayer_getDuration(JNIEnv *env,
		jobject thiz) {
	// LOGV("getDuration()");
	AuroraPlayer *mp = getMediaPlayer(env, thiz);
	if (mp == NULL) {
		jniThrowException(env, "java/lang/IllegalStateException", NULL);
		return 0;
	}
	int msec;
	int64_t usec;
	process_media_player_call(env, thiz, mp->getDuration(&usec), NULL, NULL);
	msec = usec > 0 ? (usec + 500) / 1000 : 0;
	// LOGV("getDuration: %d (msec)", msec);
	return msec;
}

/*
 * Class:     AuroraPlayer_MediaPlayer
 * Method:    getMetaFlags
 * Signature: ()I
 */
JNIEXPORT jint JNICALL JNICALL Java_com_android_auroramusic_local_AuroraPlayer_getMetaFlags(
		JNIEnv *env, jobject thiz) {
	LOGV("getMetaFlags()");
	AuroraPlayer *media_player = getMediaPlayer(env, thiz);
	if (media_player == NULL) {
		jniThrowException(env, "java/lang/IllegalStateException", NULL);
		return 0;
	}

	//TODO:
	return MetaFlags::CAN_PAUSE | MetaFlags::CAN_SEEK
			| MetaFlags::CAN_SEEK_FORWARD | MetaFlags::CAN_SEEK_BACKWARD;
}

void Java_com_android_auroramusic_local_AuroraPlayer__1release(JNIEnv *env,
		jobject thiz) {
	LOGV("release");
	setMediaPlayer(env, thiz, 0);
}

void Java_com_android_auroramusic_local_AuroraPlayer__1reset(JNIEnv *env,
		jobject thiz) {
	LOGV("reset");
	AuroraPlayer *mp = getMediaPlayer(env, thiz);
	if (mp == NULL) {
		jniThrowException(env, "java/lang/IllegalStateException", NULL);
		return;
	}

	mp->reset();
}

// This function gets some field IDs, which in turn causes class initialization.
// It is called from a static block in AwesomePlayer, which won't run until the
// first time an instance of this class is used.
void Java_com_android_auroramusic_local_AuroraPlayer_native_1init(JNIEnv *env,
		jclass jclz, jint version) {
	LOGV("native init(version:%d)", (int)version);
	__android_version = version;
	jclass clazz;

	clazz = env->FindClass("com/android/auroramusic/local/AuroraPlayer");
	if (clazz == NULL) {
		LOGE("cannot find com/android/auroramusic/local/AuroraPlayer");
		return;
	}

	fields.context = env->GetFieldID(clazz, "mNativeContext", "J");
	if (fields.context == NULL) {
		LOGE("cannot find mNativeContext");
		return;
	}

	fields.post_event = env->GetStaticMethodID(clazz, "postEventFromNative",
			"(Ljava/lang/Object;IIILjava/lang/Object;)V");
	if (fields.post_event == NULL) {
		LOGE("cannot find postEventFromNative");
		return;
	}

	fields.java_surface_ref = env->GetFieldID(clazz, "mJavaSurfaceGlobalRef",
			"J");
	if (fields.java_surface_ref == NULL) {
		LOGE("cannot find mJavaSurfaceGlobalRef");
		return;
	}
}

/*call in MediaPlayer constructor */
void Java_com_android_auroramusic_local_AuroraPlayer_native_1setup(JNIEnv *env,
		jobject thiz, jobject weak_this) {
	LOGV("native_setup");
	AuroraPlayer *mp = new AuroraPlayer();
	if (mp == NULL) {
		jniThrowException(env, "java/lang/RuntimeException", "Out of memory");
		return;
	}

	// create new listener and give it to AwesomePlayer
	JNIMediaPlayerListener *listener = new JNIMediaPlayerListener(env, thiz,
			weak_this);
	mp->setListener(listener);

	// Stow our new C++ AwesomePlayer in an opaque field in the Java object.
	setMediaPlayer(env, thiz, mp);
}

void Java_com_android_auroramusic_local_AuroraPlayer_native_1finalize(
		JNIEnv *env, jobject thiz) {
	LOGV("native_finalize");
	AuroraPlayer *mp = getMediaPlayer(env, thiz);
	if (mp != NULL) {
		LOGW("AwesomePlayer finalized without being released");
	}
	Java_com_android_auroramusic_local_AuroraPlayer__1release(env, thiz);
}

// ----------------------------------------------------------------------------

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
	JNIEnv *env = NULL;
	jint result = -1;

	LOGD("Player jni loading...");
	if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
		LOGE("ERROR: GetEnv failed\n");
		goto bail;
	}
	assert(env != NULL);
	g_vm = vm;

	/* success -- return valid version number */
	result = JNI_VERSION_1_4;
	LOGD("Player jni loaded");
	bail: return result;
}

void JNI_OnUnLoad(JavaVM *vm, void *reserved) {
	LOGD("Player jni unloaded");
}

void Java_com_android_auroramusic_local_AuroraPlayer_setVolume(JNIEnv * env,
		jobject thiz, jfloat vol) {
	AuroraPlayer *mp = getMediaPlayer(env, thiz);
	if (mp == NULL) {
		jniThrowException(env, "java/lang/IllegalStateException", NULL);
		return;
	}

	//process_media_player_call( env, thiz, mp->pause(), NULL, NULL );
}

void Java_com_android_auroramusic_local_AuroraPlayer_setDownloadSize(
		JNIEnv *env, jobject thiz, jint size) {
	AuroraPlayer *mp = getMediaPlayer(env, thiz);

	if (mp == NULL) {
		jniThrowException(env, "java/lang/IllegalStateException", NULL);
		return;
	}
	mp->setDownloadSize(size);
}

