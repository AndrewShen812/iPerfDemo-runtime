#include "iperf-3.1.3/src/iperf_config.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <getopt.h>
#include <errno.h>
#include <signal.h>
#include <unistd.h>
#ifdef HAVE_STDINT_H
#include <stdint.h>
#endif
#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#ifdef HAVE_STDINT_H
#include <stdint.h>
#endif
#include <netinet/tcp.h>

#include "iperf-3.1.3/src/iperf.h"
#include "iperf-3.1.3/src/iperf_api.h"
#include "iperf-3.1.3/src/units.h"
#include "iperf-3.1.3/src/iperf_locale.h"
#include "iperf-3.1.3/src/net.h"

#include "AndroidLog.h"
#include "iperf3_jni_api.h"
#include "iperf3_java_callback.h"
#include "common_jni_util.h"

static int run(struct iperf_test *test);

JNIEXPORT void JNICALL Java_com_iperf3_jni_Iperf3Client_exec(
        JNIEnv *env, jobject iperfClient, jobject iperfConfig, jobject callback) {
    invoke_flag = INVOKE_TYPE_JNI;

    struct iperf_test *test;

    test = iperf_new_test();
    if (!test)
        iperf_errexit(NULL, "create new test error - %s", iperf_strerror(i_errno));

    iperf_defaults(test);    /* sets defaults */
    // 解析Java层的测试参数
    parse_java_config(env, test, iperfConfig);
    // 准备Java层的回调函数
    construct_java_callback(env, test, callback);

    if (run(test) < 0) {
        char *err_str = iperf_strerror(i_errno);
        // jni callback
        test->jniCallback->on_error(test, err_str);
        iperf_errexit(test, "error - %s", err_str);
    }

    iperf_free_test(test);
}

JNIEXPORT void JNICALL Java_com_iperf3_jni_Iperf3Client_simpleTest
  (JNIEnv * env, jobject iperfClient, jstring ip, jstring port, jboolean isDownMode, jobject callback) {
    invoke_flag = INVOKE_TYPE_JNI;

    const char *hostname = (*env)->GetStringUTFChars(env, ip, NULL);
    const char *port_str = (*env)->GetStringUTFChars(env, port, NULL);
    char *tmp_path = getSafeTmpPath(env);

    if (isDownMode) {
        LOGD("iperf3 -c %s -p %s -R --tmp-path %s", hostname, port_str, tmp_path);
    } else {
        LOGD("iperf3 -c %s -p %s --tmp-path %s", hostname, port_str, tmp_path);
    }
      
    struct iperf_test *test;

    test = iperf_new_test();
    if (!test)
        iperf_errexit(NULL, "create new test error - %s", iperf_strerror(i_errno));

    iperf_defaults(test);    /* sets defaults */
    // 准备Java层的回调函数
    construct_java_callback(env, test, callback);
    // 设置iperf参数
    iperf_set_test_role(test, 'c');
    iperf_set_test_server_hostname(test, hostname);
    // -p
    test->server_port = atoi(port_str);
    // --tmp-path
    iperf_set_test_template(test, tmp_path);
    // -R
    if (isDownMode) {
        iperf_set_test_reverse(test, 1);
    }

    /*if (iperf_parse_arguments(test, argc, argv) < 0) {
        iperf_err(test, "parameter error - %s", iperf_strerror(i_errno));
        fprintf(stderr, "\n");
        usage_long();
        // FIXME 2020-11-17: do not exit on Android
        //SAFE_EXIT(1);
    }*/

    if (run(test) < 0) {
        char *err_str = iperf_strerror(i_errno);
        // jni callback
        test->jniCallback->on_error(test, err_str);
        iperf_errexit(test, "error - %s", err_str);
    }

    iperf_free_test(test);
}

/**************************************************************************/
static jmp_buf sigend_jmp_buf;

static void
sigend_handler(int sig)
{
    longjmp(sigend_jmp_buf, 1);
}

static int
run(struct iperf_test *test)
{
    /* Termination signals. */
    iperf_catch_sigend(sigend_handler);
    if (setjmp(sigend_jmp_buf)) {
        iperf_got_sigend(test);
    }

    switch (test->role) {
        case 'c':
            if (iperf_run_client(test) < 0) {
                char *err_str = iperf_strerror(i_errno);
                // jni callback
                test->jniCallback->on_error(test, err_str);
                iperf_errexit(test, "error - %s", err_str);
            }
            break;
        default:
            usage();
            break;
    }

    iperf_catch_sigend(SIG_DFL);

    return 0;
}