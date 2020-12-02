package com.iperf3.jni;

/**
 * @author shenyong
 * @date 2020-11-17
 */
public interface Iperf3Callback {

    void onConnecting(String destHost, int destPort);

    void onConnected(String localAddr, int localPort, String destAddr, int destPort);

    void onInterval(float timeStart, float timeEnd, String sendBytes, String bandWidth, boolean isDown);

    void onResult(float timeStart, float timeEnd, String sendBytes, String bandWidth, boolean isDown);

    void onError(String errMsg);
}
