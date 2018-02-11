#include <dlfcn.h>
#include <pthread.h>
#include <jni.h>

struct ANativeWindow;
typedef ANativeWindow* (*ANativeWindow_fromSurfaceFuncType)(JNIEnv* env, jobject surface);
typedef void (*ANativeWindow_releaseFuncType)(ANativeWindow* window);
typedef void (*ANativeWindow_acquireFuncType)(ANativeWindow* window);
typedef int32_t (*ANativeWindow_getWidthFuncType)(ANativeWindow* window);
typedef int32_t (*ANativeWindow_getHeightFuncType)(ANativeWindow* window);
typedef int32_t (*ANativeWindow_getFormatFuncType)(ANativeWindow* window);
typedef int32_t (*ANativeWindow_setBuffersGeometryFuncType)(ANativeWindow* window, int32_t width, int32_t height, int32_t format);

static pthread_once_t once;
static void *android_lib = NULL;
static ANativeWindow_fromSurfaceFuncType fromSurface = NULL;
static ANativeWindow_releaseFuncType release = NULL;
static ANativeWindow_acquireFuncType acquire = NULL;
static ANativeWindow_getWidthFuncType getWidth = NULL;
static ANativeWindow_getHeightFuncType getHeight = NULL;
static ANativeWindow_getFormatFuncType getFormat = NULL;
static ANativeWindow_setBuffersGeometryFuncType setBuffersGeometry = NULL;

static void initLib()
{
    android_lib = dlopen("libandroid.so", RTLD_NOW);
    if(android_lib)
    {
        fromSurface = (ANativeWindow_fromSurfaceFuncType)dlsym(android_lib, "ANativeWindow_fromSurface");
        release = (ANativeWindow_releaseFuncType)dlsym(android_lib, "ANativeWindow_release");
        acquire = (ANativeWindow_acquireFuncType)dlsym(android_lib, "ANativeWindow_acquire");
        getWidth = (ANativeWindow_getWidthFuncType)dlsym(android_lib, "ANativeWindow_getWidth");
        getHeight = (ANativeWindow_getHeightFuncType)dlsym(android_lib, "ANativeWindow_getHeight");
        getFormat = (ANativeWindow_getFormatFuncType)dlsym(android_lib, "ANativeWindow_getFormat");
        setBuffersGeometry = (ANativeWindow_setBuffersGeometryFuncType)dlsym(android_lib, "ANativeWindow_setBuffersGeometry");
    }
}

void *ANativeWindow_fromSurfaceWrap(JNIEnv* env, jobject surface)
{
    pthread_once(&once, initLib);
    if(!fromSurface || !release || !acquire || !getWidth || !getHeight || !getFormat || !setBuffersGeometry)
        return NULL;
    return fromSurface(env, surface);
}

void ANativeWindow_releaseWrap(ANativeWindow* window)
{
    pthread_once(&once, initLib);
    if(!fromSurface || !release || !acquire || !getWidth || !getHeight || !getFormat || !setBuffersGeometry)
        return;
    return release(window);
}

void ANativeWindow_acquireWrap(ANativeWindow* window)
{
    pthread_once(&once, initLib);
    if(!fromSurface || !release || !acquire || !getWidth || !getHeight || !getFormat || !setBuffersGeometry)
        return ;
    return acquire(window);
}

int32_t ANativeWindow_getWidthWrap(ANativeWindow *window)
{
    pthread_once(&once, initLib);
    if(!fromSurface || !release || !acquire || !getWidth || !getHeight || !getFormat || !setBuffersGeometry)
        return NULL;
    return getWidth(window);
}

int32_t ANativeWindow_getHeightWrap(ANativeWindow *window)
{
    pthread_once(&once, initLib);
    if(!fromSurface || !release || !acquire || !getWidth || !getHeight || !getFormat || !setBuffersGeometry)
        return 0;
    return getHeight(window);
}

int32_t ANativeWindow_getFormatWrap(ANativeWindow *window)
{
    pthread_once(&once, initLib);
    if(!fromSurface || !release || !acquire || !getWidth || !getHeight || !getFormat || !setBuffersGeometry)
        return NULL;
    return getFormat(window);
}

int32_t ANativeWindow_setBuffersGeometryWrap(ANativeWindow* window, int32_t width, int32_t height, int32_t format)
{
    pthread_once(&once, initLib);
    if(!fromSurface || !release || !acquire || !getWidth || !getHeight || !getFormat || !setBuffersGeometry)
        return NULL;
    return setBuffersGeometry(window, width, height, format);
}

