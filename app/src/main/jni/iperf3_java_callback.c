//
// Created by 申勇 on 2020/11/20.
//

#include <string.h>
#include <malloc.h>
#include "iperf3_java_callback.h"
#include "common_jni_util.h"
#include "iperf-3.1.3/src/iperf_api.h"

/******************************** Java 方法回调 start ********************************/
void call_java_method(struct iperf_test *test, jmethodID  method_id, int argc, ...) {
    JNIEnv *env = test->jniCallback->env;
    struct jni_callback *jniCallback = test->jniCallback;
    jobject callbackObj = jniCallback->callbackObj;

    va_list arg_list;
    va_start(arg_list, argc);
    (* env)->CallVoidMethodV(env, callbackObj, method_id, arg_list);
    va_end(arg_list);
}

void on_connecting_java(struct iperf_test *test, char * ipr, int port) {
    JNIEnv *env = test->jniCallback->env;
    call_java_method(test, test->jniCallback->connectingMethod, 2, charToJstring(env, ipr), port);
}

void on_connected_java(struct iperf_test *test, char ipl[], int portl, char ipr[], int portr) {
    JNIEnv *env = test->jniCallback->env;
    call_java_method(test, test->jniCallback->connectedMethod, 4, charToJstring(env, ipl), portl,
                     charToJstring(env, ipr), portr);
}

/* 共用报告带宽方法 */
void on_report_bandwidth(struct iperf_test *test, jmethodID  method_id,
                    float start, float end, char send_bytes[], char band_width[]) {
    JNIEnv *env = test->jniCallback->env;
    call_java_method(test, method_id, 4, start, end,
                     charToJstring(env, send_bytes), charToJstring(env, band_width), test->reverse);
}

void on_interval_java(struct iperf_test *test, float start, float end, char send_bytes[], char band_width[]) {
    on_report_bandwidth(test, test->jniCallback->intervalMethod, start, end, send_bytes, band_width);
}

void on_result_java(struct iperf_test *test, float start, float end, char send_bytes[], char band_width[]) {
    on_report_bandwidth(test, test->jniCallback->resultMethod, start, end, send_bytes, band_width);
}

void on_error_java(struct iperf_test *test, char *err_msg) {
    JNIEnv *env = test->jniCallback->env;
    call_java_method(test, test->jniCallback->errorMethod, 1, charToJstring(env, err_msg));
}
/******************************** Java 方法回调 end ********************************/

/* 构造回调Java方法需要的参数 */
int construct_java_callback(JNIEnv *env, struct iperf_test *test, jobject callback) {
    test->jniCallback = (struct jni_callback *) malloc(sizeof(struct jni_callback));
    if (!test->jniCallback) {
        free(test);
        i_errno = IENEWTEST;
        return NULL;
    }
    memset(test->jniCallback, 0, sizeof(struct jni_callback));

    /*
    public interface com.cmii.iperf3.Iperf3Callback {
        public abstract void onConnecting(java.lang.String, int);
        descriptor: (Ljava/lang/String;I)V

        public abstract void onConnected(java.lang.String, int, java.lang.String, int);
        descriptor: (Ljava/lang/String;ILjava/lang/String;I)V

        public abstract void onInterval(float, float, java.lang.String, java.lang.String, boolean);
        descriptor: (FFLjava/lang/String;Ljava/lang/String;Z)V

        public abstract void onResult(float, float, java.lang.String, java.lang.String, boolean);
        descriptor: (FFLjava/lang/String;Ljava/lang/String;Z)V

        public abstract void onError(java.lang.String);
        descriptor: (Ljava/lang/String;)V
    }
     */
    jclass class = (*env)->GetObjectClass(env, callback);
    jmethodID connectingMethod  = (*env)->GetMethodID(env, class, "onConnecting",
                                                      "(Ljava/lang/String;I)V");
    jmethodID connectedMethod   = (*env)->GetMethodID(env, class, "onConnected",
                                                      "(Ljava/lang/String;ILjava/lang/String;I)V");
    jmethodID intervalMethod    = (*env)->GetMethodID(env, class, "onInterval",
                                                      "(FFLjava/lang/String;Ljava/lang/String;Z)V");
    jmethodID resultMethod      = (*env)->GetMethodID(env, class, "onResult",
                                                      "(FFLjava/lang/String;Ljava/lang/String;Z)V");
    jmethodID errorMethod       = (*env)->GetMethodID(env, class, "onError", "(Ljava/lang/String;)V");
    test->jniCallback->env = env;
    test->jniCallback->callbackObj = callback;
    test->jniCallback->connectingMethod = connectingMethod;
    test->jniCallback->connectedMethod = connectedMethod;
    test->jniCallback->intervalMethod = intervalMethod;
    test->jniCallback->resultMethod = resultMethod;
    test->jniCallback->errorMethod = errorMethod;
    test->jniCallback->on_connecting = on_connecting_java;
    test->jniCallback->on_connected = on_connected_java;
    test->jniCallback->on_interval = on_interval_java;
    test->jniCallback->on_result = on_result_java;
    test->jniCallback->on_error = on_error_java;

    return 0;
}

int parse_java_config(JNIEnv *env, struct iperf_test *test, jobject config) {
    jclass class = (*env)->GetObjectClass(env, config);
    /**
     * public class com.cmii.iperf3.Iperf3Config {
     *  public java.lang.String mServerAddr;
     *    descriptor: Ljava/lang/String;
     *  public java.lang.String mServerPort;
     *    descriptor: I
     *  public boolean isDownMode;
     *    descriptor: Z
     *  public boolean interval;
     *    descriptor: D
     *  public long bandwidth;
     *    descriptor: J
     *  public char formatUnit;
     *    descriptor: C
     *  public int parallels;
     *    descriptor: I
     *  public com.cmii.iperf3.Iperf3Config();
     *    descriptor: ()V
     * }
     */
    // -c
    iperf_set_test_role(test, 'c');
    const char *server_str = get_java_string_field(env, class, config,
            "mServerAddr", "Ljava/lang/String;");
    iperf_set_test_server_hostname(test, server_str);
    //-p
    jfieldID port_field = (*env)->GetFieldID(env, class, "mServerPort", "I");
    jint port = (*env)->GetIntField(env, config, port_field);
    iperf_set_test_server_port(test, port);
    // -R
    jfieldID is_down_field = (*env)->GetFieldID(env, class, "isDownMode", "Z");
    jboolean is_down = (*env)->GetBooleanField(env, config, is_down_field);
    if (is_down) {
        iperf_set_test_reverse(test, 1);
    }
    // -b
    jfieldID bw_field = (*env)->GetFieldID(env, class, "bandwidth", "J");
    jlong bandwidth = (*env)->GetLongField(env, config, bw_field);
    iperf_set_test_rate(test, bandwidth);
    // -f
    jfieldID unit_field = (*env)->GetFieldID(env, class, "formatUnit", "C");
    jchar unit = (*env)->GetCharField(env, config, unit_field);
    iperf_set_test_unit_format(test, (char) unit);
    // -P
    jfieldID parallels_field = (*env)->GetFieldID(env, class, "parallels", "I");
    jint parallels = (*env)->GetIntField(env, config, parallels_field);
    iperf_set_test_num_streams(test, parallels);
    // --tmp-path
    char *tmp_path = getSafeTmpPath(env);
    iperf_set_test_template(test, tmp_path);
    // -i
    jfieldID interval_field = (*env)->GetFieldID(env, class, "interval", "D");
    double interval = (*env)->GetDoubleField(env, config, interval_field);
    iperf_set_test_stats_interval(test, interval);
    iperf_set_test_reporter_interval(test, interval);
    // -u，没有使用需要，先不支持udp
//    jfieldID is_udp_field = (*env)->GetFieldID(env, class, "isUdp", "Z");
//    jboolean is_udp = (*env)->GetBooleanField(env, config, is_udp_field);
//    if (is_udp) {
//        set_protocol(test, Pudp);
//        iperf_set_test_blksize(test, DEFAULT_UDP_BLKSIZE);
//    }

    return 0;
}