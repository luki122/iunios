#ifndef _VSEA_WRAP
#define _VSEA_WRAP

// we use those native window api wrap as android 2.2.x not have native api
// we shouldn't use native window api directly as it will cause undfined
// reference error when start player

#include <jni.h>
struct ANativeWindow;

ANativeWindow* ANativeWindow_fromSurfaceWrap(JNIEnv* env, jobject surface);
void ANativeWindow_releaseWrap(ANativeWindow* window);
void ANativeWindow_acquireWrap(ANativeWindow* window);
int32_t ANativeWindow_getWidthWrap(ANativeWindow *window);
int32_t ANativeWindow_getHeightWrap(ANativeWindow *window);
int32_t ANativeWindow_getFormatWrap(ANativeWindow *window);
int32_t ANativeWindow_setBuffersGeometryWrap(ANativeWindow* window, int32_t width, int32_t height, int32_t format);
#endif
