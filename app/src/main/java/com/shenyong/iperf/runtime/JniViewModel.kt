package com.shenyong.iperf.runtime

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iperf3.jni.Iperf3Callback
import com.iperf3.jni.Iperf3Client
import com.iperf3.jni.Iperf3Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *
 * @author shenyong
 * @date 2020-11-10
 */
class JniViewModel : ViewModel(), Iperf3Callback {
    companion object {
        private const val TAG = "iperf3Jni"
    }
    
    val addr = MutableLiveData<String>("iperf.biznetnetworks.com")
    val port = MutableLiveData<String>("5203")
//    val addr = MutableLiveData<String>("192.168.42.90")
//    val port = MutableLiveData<String>("5201")
    val parallel = MutableLiveData<String>("2")
    val bandwidth = MutableLiveData<String>("1000")
    val isDown = MutableLiveData<Boolean>(true)
    val log = MutableLiveData<String>("test result")
    val bandwidthFloat = MutableLiveData<Float>(0f)

    private lateinit var iperf3Client: Iperf3Client

    fun iperfTest(context: Context) {
        bandwidthFloat.value = 0f
        clearLog()
        val addrStr = if (addr.value.isNullOrEmpty()) "192.168.42.90" else addr.value!!
        val portStr = if (port.value.isNullOrEmpty()) "5201" else port.value!!
        val parallelStr = if (parallel.value.isNullOrEmpty()) "1" else parallel.value!!
        val bandwidthStr = if (bandwidth.value.isNullOrEmpty()) "1" else bandwidth.value!!

        val config = Iperf3Config(addrStr, portStr.toInt(), parallelStr.toInt())
        config.bandwidth = bandwidthStr.toInt() * Iperf3Config.BANDWIDTH_1M
        config.isDownMode = isDown.value == true

        if (!::iperf3Client.isInitialized) {
            iperf3Client = Iperf3Client(this)
        }
        viewModelScope.launch(Dispatchers.IO) {
            iperf3Client.exec(config)
        }
    }

    fun clearLog() {
        log.value = ""
    }

    private fun postLog(logStr: String) {
        viewModelScope.launch(Dispatchers.Main) {
            log.value = "${log.value}\n$logStr"
        }
    }

    /**************** CmdCallback start ****************/

    override fun onConnecting(destHost: String?, destPort: Int) {
        Log.d(TAG, "onConnecting: $destHost:$destPort")
        postLog("onConnecting: $destHost:$destPort")
    }

    override fun onResult(
        timeStart: Float,
        timeEnd: Float,
        sendBytes: String,
        bandWidth: String,
        isDown: Boolean
    ) {
        Log.d(TAG, "onResult: $timeStart-$timeEnd\t$sendBytes\t$bandWidth\tisDown:$isDown")
        postLog("[SUM]: $timeStart-$timeEnd\t$sendBytes\t$bandWidth\tisDown:$isDown")

        viewModelScope.launch(Dispatchers.Main) {
            bandwidthFloat.value = bandWidth.split(" Mbit")[0].toFloat()
        }
    }

    override fun onConnected(
        localAddr: String?,
        localPort: Int,
        destAddr: String?,
        destPort: Int
    ) {
        Log.d(TAG, "onConnected: $localAddr:$localPort --> $destAddr:$destPort")
        postLog("onConnected: $localAddr:$localPort --> $destAddr:$destPort")
    }

    override fun onInterval(
        timeStart: Float,
        timeEnd: Float,
        sendBytes: String,
        bandWidth: String,
        isDown: Boolean
    ) {
        Log.d(TAG, "onInterval: $timeStart-$timeEnd\t$sendBytes\t$bandWidth\tisDown:$isDown")
        postLog("$timeStart-$timeEnd\t$sendBytes\t$bandWidth\tisDown:$isDown")

        viewModelScope.launch(Dispatchers.Main) {
            bandwidthFloat.value = bandWidth.split(" Mbit")[0].toFloat()
        }
    }

    override fun onError(errMsg: String?) {
        Log.d(TAG, "onError: $errMsg")
        postLog("onError: $errMsg")
    }
    /**************** CmdCallback end ****************/
}
