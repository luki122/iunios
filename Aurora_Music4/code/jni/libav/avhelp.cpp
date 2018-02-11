#include <pthread.h>
#include "BackportsLog.h"
#define LOG_TAG "AuroraPlayer-libav"

extern "C"
{
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavutil/avutil.h"
#include "libavutil/avstring.h"
#include "libavutil/mem.h"
}

static pthread_once_t libav_init_once;
static pthread_once_t key_init_once;
static pthread_key_t log_ctx_key;
static bool debug_enabled = false;
static int flags = AV_LOG_SKIP_REPEATED;

struct LogContex
{
    int print_prefix;
    int count;
    char prev[1024];
};

static void log_ctx_clean(void *ctx)
{
    LOGI("log context clean");
    free(ctx);
}

static void key_init()
{
    pthread_key_create(&log_ctx_key, log_ctx_clean);
}

static void logcat_av_log_callback(void *ptr, int level, const char *fmt, va_list vl)
{
    pthread_once(&key_init_once, key_init);
    struct LogContex *log_ctx = (struct LogContex *)pthread_getspecific(log_ctx_key);
    if(!log_ctx)
    {
        log_ctx = (struct LogContex *)malloc(sizeof(*log_ctx));
        memset(log_ctx, 0, sizeof(*log_ctx));
        log_ctx->print_prefix = 1;
        pthread_setspecific(log_ctx_key, log_ctx);
    }

    char line[1024];
    AVClass *avc = ptr ? *(AVClass **) ptr : NULL;
    if(level > AV_LOG_INFO)
        return;
    line[0] = 0;
    if(log_ctx->print_prefix && avc)
    {
        if(avc->parent_log_context_offset)
        {
            AVClass **parent = *(AVClass ** *)(((uint8_t *) ptr) + avc->parent_log_context_offset);
            if(parent && *parent)
            {
                snprintf(line, sizeof(line), "[%s @ %p] ", (*parent)->item_name(parent), parent);
            }
        }
        snprintf(line + strlen(line), sizeof(line) - strlen(line), "[%s @ %p] ",
                 avc->item_name(ptr), ptr);
    }

    vsnprintf(line + strlen(line), sizeof(line) - strlen(line), fmt, vl);
    log_ctx->print_prefix = strlen(line) && line[strlen(line) - 1] == '\n';
    if(log_ctx->print_prefix &&
        (flags & AV_LOG_SKIP_REPEATED) &&
        !strncmp(line, log_ctx->prev, sizeof line))
    {
        log_ctx->count++;
        return;
    }
    if(log_ctx->count > 0)
    {
        LOGI("    Last message repeated %d times\n", log_ctx->count);
        log_ctx->count = 0;
    }
    LOGI("%s", line);
    av_strlcpy(log_ctx->prev, line, sizeof line);
}

/* Mutex manager callback. */
static int lockmgr(void **mtx, enum AVLockOp op)
{
    switch (op) {
    case AV_LOCK_CREATE:
        *mtx = (void *)av_malloc(sizeof(pthread_mutex_t));
        if (!*mtx)
            return 1;
        return !!pthread_mutex_init((pthread_mutex_t *)(*mtx), NULL);
    case AV_LOCK_OBTAIN:
        return !!pthread_mutex_lock((pthread_mutex_t *)(*mtx));
    case AV_LOCK_RELEASE:
        return !!pthread_mutex_unlock((pthread_mutex_t *)(*mtx));
    case AV_LOCK_DESTROY:
        pthread_mutex_destroy((pthread_mutex_t *)(*mtx));
        av_freep(mtx);
        return 0;
    }
    return 1;
}

static void init()
{
    if(debug_enabled)
        av_log_set_level(AV_LOG_DEBUG);
    else
        av_log_set_level(AV_LOG_INFO);

    av_log_set_callback(logcat_av_log_callback);

    /* register all codecs, demux and protocols */
    avcodec_register_all();
    av_register_all();
    avformat_network_init();

    if(av_lockmgr_register(lockmgr))
    {
        LOGE("could not initialize lock manager!");
    }
}

void LibavInit()
{
    pthread_once(&libav_init_once, init);
}

