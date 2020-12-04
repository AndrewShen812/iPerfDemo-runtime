package com.iperf3.jni;

/**
 * iPerf测试参数配置
 * @author shenyong
 * @date 2020/11/26
 */
public class Iperf3Config {
    public static final long BANDWIDTH_1M = 1000 * 1000;
    public static final long BANDWIDTH_1000M = 1000 * BANDWIDTH_1M;

    /** */
    /** 服务器地址 */
    public String mServerAddr;
    /** 服务器端口 */
    public int mServerPort;
    /** 是否为下行速率测试 */
    public boolean isDownMode;
    /** 中间测试结果的报告间隔，取值范围：[0.1, 60.0]，单位：秒，默认间隔1秒 */
    public double interval = 1.0;
    /** 带宽限制，单位：bps，默认1000Mbps */
    public long bandwidth = BANDWIDTH_1000M;
    /**
     * 回调接口中数据的单位，默认为：Mbit。设置规则：</br>
     * B, K, M, G, A for Byte, Kbyte, Mbyte, Gbyte, adaptive byte</br>
     * b, k, m, g, a for bit,  Kbit,  Mbit,  Gbit,  adaptive bit</br>
     * */
    public char formatUnit = 'm';
    /** iperf执行时的并发连接数量，默认为1*/
    public int parallels = 1;

    public Iperf3Config() {
    }

    public Iperf3Config(String mServerAddr, int mServerPort) {
        this.mServerAddr = mServerAddr;
        this.mServerPort = mServerPort;
    }

    public Iperf3Config(String mServerAddr, int mServerPort, int parallels) {
        config(mServerAddr, mServerPort, parallels);
    }

    public void config(String mServerAddr, int mServerPort, int parallels) {
        this.mServerAddr = mServerAddr;
        this.mServerPort = mServerPort;
        this.parallels = parallels;
    }

    public void config(String mServerAddr, int mServerPort, int parallels, boolean isDownMode) {
        config(mServerAddr, mServerPort, parallels);
        this.isDownMode = isDownMode;
    }
}
