cmake_minimum_required(VERSION 3.4.1)

#file(GLOB variable [RELATIVE path] [globbing expressions]...)
#GLOB选项将会为所有匹配查询表达式的文件生成一个文件list，并将该list存储进变量variable里
file(GLOB IPERF3_C_SOURCES
        iperf-3.1.3/src/cjson.c
        iperf-3.1.3/src/iperf_api.c
        iperf-3.1.3/src/iperf_client_api.c
        iperf-3.1.3/src/iperf_error.c
        iperf-3.1.3/src/iperf_locale.c
        iperf-3.1.3/src/iperf_sctp.c
        iperf-3.1.3/src/iperf_server_api.c
        iperf-3.1.3/src/iperf_tcp.c
        iperf-3.1.3/src/iperf_udp.c
        iperf-3.1.3/src/iperf_util.c
        iperf-3.1.3/src/main.c
        iperf-3.1.3/src/net.c
        iperf-3.1.3/src/tcp_info.c
        iperf-3.1.3/src/tcp_window_size.c
        iperf-3.1.3/src/timer.c
        iperf-3.1.3/src/units.c)

include_directories(iperf-3.1.3/src)

SET(CMAKE_SYSTEM_NAME Android)
# API level
set(CMAKE_SYSTEM_VERSION 21)

set(CMAKE_C_FLAGS_RELEASE "-pie -fPIE -fPIC -s")
SET(CMAKE_BUILD_TYPE "Release")

# STATIC | SHARED | MODULE
#add_library(iperf3 SHARED ${IPERF3_C_SOURCES})
add_executable(iperf3 ${IPERF3_C_SOURCES})