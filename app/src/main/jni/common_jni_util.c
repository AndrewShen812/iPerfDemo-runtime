//
// Created by 申勇 on 2020/11/20.
//

#include <string.h>
#include <malloc.h>
#include "common_jni_util.h"
#include "AndroidLog.h"

#define TMP_PATH_PREFIX "/data/data/"
#define TMP_PATH_SUFFIX "/files/iperf3.XXXXXX"

jstring charToJstring(JNIEnv* env, const char* str) {

    jclass clstring = (*env)->FindClass(env, "java/lang/String");
    //获取对象
    jmethodID mid = (*env)->GetMethodID(env, clstring, "<init>", "([BLjava/lang/String;)V");//获取String(byte[],String)的构造器,用于将本地byte[]数组转换为一个新String
    jbyteArray bytes = (*env)->NewByteArray(env, strlen(str));//建立jbyte数组
    (*env)->SetByteArrayRegion(env, bytes, 0, strlen(str), (jbyte*) str);//将char* 转换为byte数组
    jstring encoding = (*env)->NewStringUTF(env, "utf-8");// 设置String, 保存语言类型,用于byte数组转换至String时的参数
    return (jstring) (*env)->NewObject(env, clstring, mid, bytes, encoding);//将byte数组转换为java String,并输出
}

/**
 * 获取上下文
 * @param env
 * @return
 */
jobject getApplication(JNIEnv *env) {
    jobject application = NULL;
    jclass activity_thread_clz = (*env)->FindClass(env, "android/app/ActivityThread");
    if (activity_thread_clz != NULL) {
        jmethodID get_Application = (*env)->GetStaticMethodID(env, activity_thread_clz,
                "currentActivityThread", "()Landroid/app/ActivityThread;");
        if (get_Application != NULL) {
            jobject currentActivityThread = (*env)->
                    CallStaticObjectMethod(env, activity_thread_clz,get_Application);
            jmethodID getal = (*env)->GetMethodID(env, activity_thread_clz, "getApplication",
                    "()Landroid/app/Application;");
            application = (*env)->CallObjectMethod(env, currentActivityThread, getal);
        }
        return application;
    }
    return application;
}

/**
 * 获取包名
 * @param env
 * @return
 */
const char* getPackageName(JNIEnv *env) {
    jobject context = getApplication(env);
    if (context == NULL) {
        LOGE("context is null!");
        return NULL;
    }
    jclass activity = (*env)->GetObjectClass(env, context);
    jmethodID methodId_pack = (*env)->GetMethodID(env, activity, "getPackageName", "()Ljava/lang/String;");
    jstring package_name = (jstring) ((*env)->CallObjectMethod(env, context, methodId_pack));
    jboolean isCopy;
    return (*env)->GetStringUTFChars(env, package_name, &isCopy);
}

char* getSafeTmpPath(JNIEnv *env) {
    int prefix_len = strlen(TMP_PATH_PREFIX);
    int suffix_len = strlen(TMP_PATH_SUFFIX);
    const char *package_name = getPackageName(env);
    int package_len = strlen(package_name);
    char *tmp_path = (char *) malloc((size_t) (prefix_len + package_len + suffix_len));
    sprintf(tmp_path, "%s%s%s", TMP_PATH_PREFIX, package_name, TMP_PATH_SUFFIX);

    return tmp_path;
}

const char* get_java_string_field(JNIEnv* env, jclass class, jobject object,
        const char *field, const char *signature) {
    jfieldID server_field = (*env)->GetFieldID(env, class, field, signature);
    jstring server = (*env)->GetObjectField(env, object, server_field);
    return (*env)->GetStringUTFChars(env, server, NULL);
}