#ifndef _MDATE_H_
#define _MDATE_H_
#include <sys/time.h>
#include <dlfcn.h>
#include <unistd.h>


static inline int64_t mdate()
{
    struct timeval time = { 0 };
    gettimeofday(&time, NULL);
    return (int64_t)time.tv_sec * 1000000 + time.tv_usec;
}

static inline void mwait(int64_t time)
{
    int64_t dur = time - mdate();
    if(dur <= 0)
        return;
    usleep(dur);
}

#endif 
