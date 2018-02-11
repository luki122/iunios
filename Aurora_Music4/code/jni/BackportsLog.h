#ifndef _BACKPORTS_LOG_H
#define _BACKPORTS_LOG_H

#include "android/log.h"

#undef LOG_TAG
#undef LOG
#undef LOGV
#undef LOGD
#undef LOGI
#undef LOGW
#undef LOGE

#define LOG_DISABLE

#ifdef LOG_DISABLE
#define LOGV(...) ((void)0)
#define LOGD(...) ((void)0)
#define LOGI(...) ((void)0)
#define LOGW(...) ((void)0)
#define LOGE(...) ((void)0)

#else

#ifndef LOG
#define LOG(priority, tag, ...) \
    __android_log_print(ANDROID_##priority, tag, __VA_ARGS__)
#endif
#define LOGV(...) ((void)LOG(LOG_VERBOSE, LOG_TAG, __VA_ARGS__))
#define LOGD(...) ((void)LOG(LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define LOGI(...) ((void)LOG(LOG_INFO, LOG_TAG, __VA_ARGS__))
#define LOGW(...) ((void)LOG(LOG_WARN, LOG_TAG, __VA_ARGS__))
#define LOGE(...) ((void)LOG(LOG_ERROR, LOG_TAG, __VA_ARGS__))

#endif

#define VSEA_CHECK(exp) \
    do { \
        if(!(exp)) { \
            (void)__android_log_assert(NULL , \
                "AuroraPlayer" , \
                "%s:%d : check %s failed" , \
                __FILE__, __LINE__, #exp ); \
        } \
    } while(0) \


#endif
