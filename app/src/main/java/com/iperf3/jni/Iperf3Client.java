package com.iperf3.jni;

/**
 * @author shenyong
 * @date 2020-11-10
 */
public class Iperf3Client {

    static {
        System.loadLibrary("iperf3");
    }

    private Iperf3Callback mCallback;

    public Iperf3Client(Iperf3Callback callback) {
        mCallback = callback;
    }

    private native void simpleTest(String serverIp, String serverPort, boolean isDownMode, Iperf3Callback callback);

    public native void exec(Iperf3Config testConfig, Iperf3Callback callback);

    public void exec(Iperf3Config testConfig) {
        exec(testConfig, mCallback);
    }

    public void exec(String serverIp, String serverPort, boolean isDownMode) {
        simpleTest(serverIp, serverPort, isDownMode, mCallback);
    }
}
