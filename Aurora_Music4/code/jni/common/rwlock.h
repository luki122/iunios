/*
 * an implement of pthread_mutex_rwlock for android(api level <= 8)
 */

#ifndef _VSEA_WRLOCK_H
#define _VSEA_WRLOCK_H

#ifdef __cplusplus
extern "C" {
#endif

typedef struct _rwlock_t
{
    struct RWLockContext *rw_ctx;
} rwlock_t;

typedef struct _rwlockattr_t
{
    void *unsupport_attr;
} rwlockattr_t;

int rwlock_init(rwlock_t *rwlock, const rwlockattr_t *attr);
int rwlock_destroy(rwlock_t *rwlock);
int rwlock_rdlock(rwlock_t *rwlock);
int rwlock_wrlock(rwlock_t *rwlock);
int rwlock_unlock(rwlock_t *rwlock);

#ifdef __cplusplus
};
#endif

#define pthread_rwlock_t rwlock_t
#define pthread_rwlock_init rwlock_init
#define pthread_rwlock_destroy rwlock_destroy
#define pthread_rwlock_rdlock rwlock_rdlock
#define pthread_rwlock_wrlock rwlock_wrlock
#define pthread_rwlock_unlock rwlock_unlock

#define pthread_rwlock_tryrdlock rwlock_tryrdlock_NOT_IMPLEMENT
#define pthread_rwlock_trywrlock rwlock_trywrlock_NOT_IMPLEMENT

#endif
