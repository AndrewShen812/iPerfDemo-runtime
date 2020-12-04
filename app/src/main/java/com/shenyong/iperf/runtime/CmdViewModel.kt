package com.shenyong.iperf.runtime

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iperf3.cmd.CmdCallback
import com.iperf3.cmd.Iperf3Cmd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *
 * @author shenyong
 * @date 2020-11-10
 */
class CmdViewModel : ViewModel(), CmdCallback {

    companion object {
        private const val TAG = "iperf3Cmd"
    }

//    val addr = MutableLiveData<String>("bouygues.iperf.fr")
//    val port = MutableLiveData<String>("9200")
    val addr = MutableLiveData<String>("192.168.42.90")
    val port = MutableLiveData<String>("5201")
    val parallel = MutableLiveData<String>("2")
    val bandwidth = MutableLiveData<String>("1000")
    val preview = MutableLiveData<String>("command")
    val isUdp = MutableLiveData<Boolean>(false)
    val isDown = MutableLiveData<Boolean>(true)
    val log = MutableLiveData<String>("test result")
    val bandwidthFloat = MutableLiveData<Float>(0f)

    lateinit var iperf3Cmd: Iperf3Cmd

    private fun getCmdArgs(context: Context, includePath: Boolean = false): Array<String> {
        val addrStr = if (addr.value.isNullOrEmpty()) "192.168.42.90" else addr.value!!
        val portStr = if (port.value.isNullOrEmpty()) "5201" else port.value!!
        val parallelStr = if (parallel.value.isNullOrEmpty()) "1" else parallel.value!!
        val bandwidthStr = if (bandwidth.value.isNullOrEmpty()) "1" else bandwidth.value!!
        val tmpPath = context.filesDir.absolutePath

        val args = ArrayList<String>()
        args.add("-c")
        args.add(addrStr)
        args.add("-p")
        args.add(portStr)
        args.add("-b")
        args.add("${bandwidthStr}M")
        args.add("-P")
        args.add(parallelStr)
        if (includePath) {
            args.add("--tmp-path")
            args.add("$tmpPath/iperf3.XXXXXX")
        }
        if (isDown.value == true) {
            args.add("-R")
        }
        if (isUdp.value == true) {
            args.add("-u")
        }
        args.add("-f")
        args.add("m")
        return args.toTypedArray()
    }

    fun iperfTest(context: Context) {
        clearLog()

        if (!::iperf3Cmd.isInitialized) {
            iperf3Cmd = Iperf3Cmd(context.applicationContext, this)
        }

        viewModelScope.launch(Dispatchers.IO) {
            iperf3Cmd.prepare()
            try {
                val finalArgs = getCmdArgs(context, includePath = true)
                iperf3Cmd.exec(*finalArgs)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshCmdPreview(context: Context) {
        val args = getCmdArgs(context)
        val sb = StringBuffer("iperf3 ")
        args.forEach {
            sb.append("$it ")
        }
        preview.value = sb.toString()
    }

    fun clearLog() {
        bandwidthFloat.value = 0f
        log.value = ""
    }

    /**************** CmdCallback start ****************/

    override fun onRawOutput(rawOutputLine: String) {
        Log.d("iperf", rawOutputLine)
        log.value = "${log.value}\n$rawOutputLine"
    }

    override fun onConnecting(destHost: String?, destPort: Int) {
        Log.d(TAG, "onConnecting: $destHost:$destPort")
    }

    override fun onResult(
        timeStart: Float,
        timeEnd: Float,
        sendBytes: String,
        bandWidth: String,
        isDown: Boolean
    ) {
        Log.d(TAG, "onResult: $timeStart-$timeEnd\t$sendBytes\t$bandWidth\tisDown:$isDown")
        bandwidthFloat.value = bandWidth.split(" Mbit")[0].toFloat()
    }

    override fun onConnected(localAddr: String?, localPort: Int, destAddr: String?, destPort: Int) {
        Log.d(TAG, "onConnected: $localAddr:$localPort --> $destAddr:$destPort")
    }

    override fun onInterval(
        timeStart: Float,
        timeEnd: Float,
        sendBytes: String,
        bandWidth: String,
        isDown: Boolean
    ) {
        Log.d(TAG, "onInterval: $timeStart-$timeEnd\t$sendBytes\t$bandWidth\tisDown:$isDown")
        bandwidthFloat.value = bandWidth.split(" Mbit")[0].toFloat()
    }

    override fun onError(errMsg: String?) {
        Log.d(TAG, "onError: $errMsg")
    }
    /**************** CmdCallback end ****************/
}
