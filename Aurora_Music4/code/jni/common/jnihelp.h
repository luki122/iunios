#ifndef _CNS_JNIHELP_H_
#define _CNS_JNIHELP_H_
#include <jni.h>

int jniThrowException(JNIEnv* env, const char* className, const char* msg);

// get JNIEnv * for caller's thread
// this function will attach caller's thread to jvm, and
// detach when thread exit(if thread is attached by
// this function)
// NOTE: this function ref an initillized global value g_vm;
JNIEnv *getEnv();

#endif