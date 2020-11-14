package com.shenyong.iperf.runtime

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 *
 * @author shenyong
 * @date 2020-11-14
 */
class MainViewModel : ViewModel() {

    val serverAddr = MutableLiveData<String>("bouygues.iperf.fr")

    val serverPort = MutableLiveData<String>("9200")

    val execOutput = MutableLiveData<String>("")

    private lateinit var iperf3Cmd: Iperf3Cmd

    fun execIperf(context: Context) {
        execOutput.value = ""
        val addr = serverAddr.value?.trim() ?: ""
        val port = serverPort.value?.trim() ?: ""
        viewModelScope.launch(Dispatchers.IO) {
            if (!(::iperf3Cmd.isInitialized)) {
                iperf3Cmd = Iperf3Cmd(context.applicationContext)
                iperf3Cmd.prepare()
            }
            // test down speed
            iperfTest(addr, port, true)
            // test up speed
            iperfTest(addr, port, false)
        }
    }

    @WorkerThread
    private suspend fun iperfTest(serverAddr: String, serverPort: String, isDownMode: Boolean) {
        var outReader: BufferedReader? = null
        var errReader: BufferedReader? = null
        try {
            val dir = if (isDownMode) "-R" else ""
            val process = iperf3Cmd.exec("-c", serverAddr, "-p", serverPort, dir)
//            val process = Runtime.getRuntime().exec("ping -c 4 $serverAddr")
            outReader= BufferedReader(InputStreamReader(process.inputStream))
            errReader = BufferedReader(InputStreamReader(process.errorStream))
            var line = outReader.readLine()
            Log.d("iperf3", "----iperf3 output:")
            while (line != null) {
                Log.d("iperf3", line)
                withContext(Dispatchers.Main) {
                    execOutput.value = "${execOutput.value}$line\n"
                }
                line = outReader.readLine()
            }

            line = errReader.readLine()
            while (line != null) {
                Log.e("iperf3", line)
                withContext(Dispatchers.Main) {
                    execOutput.value = "${execOutput.value}$line\n"
                }
                line = errReader.readLine()
            }

            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                outReader?.close()
            } catch (e: Exception) {
            }
            try {
                errReader?.close()
            } catch (e: Exception) {
            }
        }
    }
}