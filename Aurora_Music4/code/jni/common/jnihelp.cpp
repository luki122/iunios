#include <pthread.h>
#include <assert.h>
#include "jnihelp.h"
#include "BackportsLog.h"

#define LOG_TAG "AuroraPlayer-jnihelp"

static struct CachedFields
{
    jclass fileDescriptorClass;
    jmethodID fileDescriptorCtor;
    jfieldID descriptorField;
} gCachedFields;

int jniThrowException(JNIEnv* env, const char* className, const char* msg)
{
    if(env->ExceptionCheck())
    {
        LOGW("ExceptionOccurred,will clear");
        env->ExceptionClear();
    }
    
    jclass ec = env->FindClass(className);
    if(!ec)
    {
        LOGE("Unable to find exception class %s", className);
        env->DeleteLocalRef(ec);
        return -1;
    }
    
    LOGE("Throw exception %s (msg:%s)", className, msg);
    if(env->ThrowNew(ec, msg) != JNI_OK)
    {
        LOGE("Failed throwing '%s' '%s'", className, msg);
        env->DeleteLocalRef(ec);
        return -1;
    }
    
    return 0;
}

// ----------------------------------------------------------------------------

JavaVM *g_vm = NULL;
static pthread_key_t key;
static pthread_once_t key_once = PTHREAD_ONCE_INIT;

static void destructor(void *data)
{
    JNIEnv *env = NULL;
    if(g_vm->GetEnv((void**)&env, JNI_VERSION_1_4) == JNI_OK &&
       env != NULL &&
       env == (JNIEnv*)data)
        g_vm->DetachCurrentThread();
    else
        LOGE("Detach jvm in an unespected thread"); /* SHOULD NOT occure*/
}

static void make_key()
{
    (void) pthread_key_create(&key, destructor);
}

JNIEnv *getEnv()
{
    bool isAttachByMe = false;
    JNIEnv *env = NULL;
    pthread_once(&key_once, make_key);

    if(g_vm->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK ||
       env == NULL)
    {
        g_vm->AttachCurrentThread(&env, NULL);
        isAttachByMe = true;
        assert(env);
    }
    if(isAttachByMe && (void*)env != pthread_getspecific(key))
        pthread_setspecific(key, (void*)env);
    return env;
}

