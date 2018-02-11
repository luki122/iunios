#include "rwlock.h"

#undef pthread_rwlock_t
#undef pthread_rwlock_init
#undef pthread_rwlock_destroy
#undef pthread_rwlock_rdlock
#undef pthread_rwlock_wrlock
#undef pthread_rwlock_unlock

#include <pthread.h>
#include <assert.h>

static pthread_key_t key;
static pthread_once_t key_once;

#define RWLOCK_NOT_LOCK ((void*)0)
#define RWLOCK_RD_LOCK  ((void*)1)
#define RWLOCK_WR_LOCK  ((void*)2)

static void key_init()
{
    pthread_key_create(&key, NULL);
}

struct RWLockContext
{
    pthread_mutex_t rd_mutex;
    pthread_mutex_t wr_mutex;
    int read_cnt;
};

int rwlock_init(rwlock_t *rwlock, const rwlockattr_t *attr)
{
    assert(attr == NULL);
    pthread_once(&key_once, key_init);

    struct RWLockContext *ctx = (struct RWLockContext *)malloc(sizeof(*ctx));
    pthread_mutex_init(&ctx->rd_mutex, NULL);
    pthread_mutex_init(&ctx->wr_mutex, NULL);
    ctx->read_cnt = 0;
    rwlock->rw_ctx = ctx;
    return 0;
}

int rwlock_destroy(rwlock_t *rwlock)
{
    assert(rwlock->rw_ctx->read_cnt == 0);
    pthread_mutex_destroy(&rwlock->rw_ctx->wr_mutex);
    pthread_mutex_destroy(&rwlock->rw_ctx->rd_mutex);
    free(rwlock->rw_ctx);
    rwlock->rw_ctx = NULL;
    return 0;
}

int rwlock_rdlock(rwlock_t *rwlock)
{
    pthread_mutex_lock(&rwlock->rw_ctx->rd_mutex);
    pthread_setspecific(key, RWLOCK_RD_LOCK);
    if(++rwlock->rw_ctx->read_cnt == 1)
        pthread_mutex_lock(&rwlock->rw_ctx->wr_mutex);
    pthread_mutex_unlock(&rwlock->rw_ctx->rd_mutex);
    return 0;
}

int rwlock_wrlock(rwlock_t *rwlock)
{
    pthread_mutex_lock(&rwlock->rw_ctx->wr_mutex);
    pthread_setspecific(key, RWLOCK_WR_LOCK);
    return 0;
}

int rwlock_unlock(rwlock_t *rwlock)
{
    void *lock_type = pthread_getspecific(key);
    assert(lock_type == RWLOCK_RD_LOCK || lock_type == RWLOCK_WR_LOCK);
    if(lock_type == RWLOCK_RD_LOCK)
    {
        pthread_mutex_lock(&rwlock->rw_ctx->rd_mutex);
        if(--rwlock->rw_ctx->read_cnt == 0)
            pthread_mutex_unlock(&rwlock->rw_ctx->wr_mutex);
        pthread_mutex_unlock(&rwlock->rw_ctx->rd_mutex);
    }
    else
    {
        pthread_mutex_unlock(&rwlock->rw_ctx->wr_mutex);
    }

    pthread_setspecific(key, RWLOCK_NOT_LOCK);
    return 0;
}
