#include <jni.h>
/* Header for class com_cmii_iperf3_Iperf3Client */

#ifndef _INCLUDED_CMII_IPERF3
#define _INCLUDED_CMII_IPERF3
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_iperf3_jni_Iperf3Client_simpleTest
  (JNIEnv *, jobject, jstring, jstring, jboolean, jobject);

JNIEXPORT void JNICALL Java_com_iperf3_jni_Iperf3Client_exec
        (JNIEnv *, jobject, jobject , jobject);

#ifdef __cplusplus
}
#endif
#endif // _INCLUDED_CMII_IPERF3
