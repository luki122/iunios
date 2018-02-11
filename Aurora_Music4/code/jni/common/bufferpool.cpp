#include "mediadef.h"
#include "BackportsLog.h"
#define LOG_TAG "AuroraPlayer"

struct BufferItem
{
    struct list_head list;
    long buffer_id;
    void *buf_ptr;
};

struct BufferPool *BufferPoolNew()
{
    BufferPool *pool = (BufferPool*)malloc(sizeof(*pool));
    INIT_LIST_HEAD(&pool->buffer_list);
    pthread_mutex_init(&pool->lock, NULL);
    pool->next_uid = 0;
    return pool;
}

void BufferPoolFreep(struct BufferPool **pool)
{
    BufferPool *p = *pool;
    if(!list_empty(&p->buffer_list))
    {
        LOGE("FALTAL ERROR. Bufffer pool not empty");
        abort();
    }
    INIT_LIST_HEAD(&p->buffer_list);
    pthread_mutex_destroy(&p->lock);
    free(p);
    *pool = NULL;
}

void BufferPoolLock(struct BufferPool *pool)
{
    pthread_mutex_lock(&pool->lock);
}

void BufferPoolUnlock(struct BufferPool *pool)
{
    pthread_mutex_unlock(&pool->lock);
}

long BufferPoolAdd(struct BufferPool *pool, void *buf_ptr)
{
    struct BufferItem *item = (struct BufferItem *)malloc(sizeof(*item));
    item->buf_ptr = buf_ptr;
    item->buffer_id = pool->next_uid++;
    list_add_tail(&item->list, &pool->buffer_list);
    return item->buffer_id;
}

void *BufferPoolRemove(struct BufferPool *pool, long buffer_id)
{
    struct list_head *i, *next;
    struct BufferItem *item = NULL;
    list_for_each_safe(i, next, &pool->buffer_list)
    {
        item = list_entry(i, BufferItem, list);
        if(item->buffer_id == buffer_id)
        {
            list_del(i);
            void *buffer = item->buf_ptr;
            free(item);
            return buffer;
        }
    }
    return NULL;
}

void BufferPoolRemoveAll(struct BufferPool *pool, void ***buf_ptrs, int *size)
{
    struct list_head *i, *next;
    *buf_ptrs = NULL;
    *size = 0;
    list_for_each_safe(i, next, &pool->buffer_list)
        ++*size;
    *buf_ptrs = (void **)malloc(sizeof(void *) * (*size));
    void **ptr = *buf_ptrs;
    list_for_each_safe(i, next, &pool->buffer_list)
    {
        struct BufferItem *item = list_entry(i, BufferItem, list);
        *ptr++ = item->buf_ptr;
        list_del(i);
        free(item);
    }
}

void *BufferPoolGet(struct BufferPool *pool, long buffer_id)
{
    struct list_head *i, *next;
    struct BufferItem *item = NULL;
    list_for_each_safe(i, next, &pool->buffer_list)
    {
        item = list_entry(i, BufferItem, list);
        if(item->buffer_id == buffer_id)
            return item->buf_ptr;
    }
    return NULL;
}

bool BufferPoolEmpty(struct BufferPool *pool)
{
    return list_empty(&pool->buffer_list);
}

