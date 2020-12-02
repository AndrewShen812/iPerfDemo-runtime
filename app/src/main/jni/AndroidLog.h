#ifndef __CUSTOM_ANDROID_LOG_H
#define __CUSTOM_ANDROID_LOG_H

#include <android/log.h>

#define TAG "iperf3" // 这个是自定义的LOG的标识
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__) // 定义LOGD类型
#define LOGDV(...) __android_log_vprint(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__) // 定义LOGD类型，应用于可变参数情况
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__) // 定义LOGI类型
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__) // 定义LOGW类型
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__) // 定义LOGE类型
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL, TAG, __VA_ARGS__) // 定义LOGF类型


#ifdef __ANDROID__ //android的编译器会自动识别到这个为真。
#include <android/log.h>
#include <stdio.h>

static int my_fprintf(FILE *stream, const char *format, ...){
    va_list ap;
    va_start(ap, format);
    __android_log_vprint(ANDROID_LOG_DEBUG, TAG, format, ap);
    va_end(ap);
    return 0;
}

#ifdef fprintf
#undef fprintf
#endif
#define fprintf(fp,...) my_fprintf(fp, __VA_ARGS__)

#endif /*__ANDROID__*/

#endif //__CUSTOM_ANDROID_LOG_H