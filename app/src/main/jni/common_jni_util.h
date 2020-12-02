//
// Created by 申勇 on 2020/11/20.
//

#include <jni.h>

#ifndef INC_5GMPORTAL_CMII_JNI_UTIL_H
#define INC_5GMPORTAL_CMII_JNI_UTIL_H

extern jstring charToJstring(JNIEnv* env, const char* str);

extern const char* getPackageName(JNIEnv *env);

extern char* getSafeTmpPath(JNIEnv *env);

extern const char* get_java_string_field(JNIEnv* env, jclass class, jobject object,
                                         const char *field, const char *signature);

#endif //INC_5GMPORTAL_CMII_JNI_UTIL_H
