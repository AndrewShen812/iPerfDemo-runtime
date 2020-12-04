package com.iperf3.cmd

import com.iperf3.jni.Iperf3Callback

/**
 *
 * @author shenyong
 * @date 2020/12/3
 */
interface CmdCallback : Iperf3Callback {

    fun onRawOutput(rawOutputLine: String)
}