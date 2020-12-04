package com.iperf3.cmd

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*
import java.util.regex.Pattern

/**
 *
 * @author shenyong
 * @date 2020-11-10
 */
class Iperf3Cmd(private val context: Context, private val callback: CmdCallback) {

    companion object {
        private const val EXECUTABLE_X86 = "iperf3-x86"
        private const val EXECUTABLE_ARM64 = "iperf3-arm64-v8a"
        private const val EXECUTABLE_ARM = "iperf3-armeabi-v7a"

        private val CONNECTING_PATTERN = Regex("(Connecting to host (.*), port (\\d+))")
        private val CONNECTED_PATTERN = Regex("(local (.*) port (\\d+) connected to (.*) port (\\d+))")
        private val REPORT_PATTERN = Regex("(\\d{1,2}.\\d{2})-(\\d{1,2}.\\d{2})\\s+sec" +
                "\\s+(\\d+(.\\d+)? [KMGT]?Bytes)\\s+(\\d+(.\\d+)? Mbits/sec)")
        private val UDP_LOSS = Regex("\\d+/\\d+ \\([\\d+-.e]+%\\)")
        private val TITLE_PATTERN = Regex("\\[\\s+ID\\]\\s+Interval\\s+Transfer\\s+Bandwidth")
        private const val ERR_PATTERN = "iperf3: error"
    }

    private var parallels = 0
    private var isDown = false
    // title出现次数。title栏输出第一次之后的、第二次之前的，是中间结果，第二次之后的是最终平均速率
    private var titleCnt = 0

    private val executableName
        get(): String {
            return when (DevUtils.getCpuApi()) {
                "x86" -> EXECUTABLE_X86
                "arm64-v8a" -> EXECUTABLE_ARM64
                "armeabi-v7a" -> EXECUTABLE_ARM
                else -> ""
            }
        }

    private val cmdPath
        get(): String {
            return "${context.filesDir.absolutePath}/$executableName"
        }

    fun prepare() {
        val cmdFile = File(cmdPath)
        if (cmdFile.exists()) {
            cmdFile.setExecutable(true, true)
            return
        }
        try {
            FileUtils.copyInputStreamToFile(context.resources.assets.open(executableName), cmdFile)
            cmdFile.setExecutable(true, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @WorkerThread
    suspend fun exec(vararg args: String) {
        val cmdAndArgs = arrayOfNulls<String>(args.size + 1)
        cmdAndArgs[0] = cmdPath
        System.arraycopy(args, 0, cmdAndArgs, 1, args.size)
        Log.d("iperf3", "exec command: ${cmdAndArgs.contentToString()}")

        var outReader: BufferedReader? = null
        var errReader: BufferedReader? = null
        try {
            parseArgs(cmdAndArgs)
            val process = Runtime.getRuntime().exec(cmdAndArgs)
            outReader= BufferedReader(InputStreamReader(process.inputStream))
            errReader = BufferedReader(InputStreamReader(process.errorStream))
            var line = outReader.readLine()
            while (line != null) {
                withContext(Dispatchers.Main) {
                    parseToCallback(line)
                }
                line = outReader.readLine()
            }

            line = errReader.readLine()
            while (line != null) {
                withContext(Dispatchers.Main) {
                    parseToCallback(line)
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

    private fun parseArgs(cmdAndArgs: Array<String?>) {
        isDown = false
        titleCnt = 0
        cmdAndArgs.forEachIndexed { index, s ->
            if ("-P" == s) {
                parallels = cmdAndArgs[index + 1]?.toInt() ?: 0
            } else if ("-R" == s) {
                isDown = true
            }
        }
    }

    private fun parseToCallback(line: String) {
        callback.onRawOutput(line)
        if (line.contains(TITLE_PATTERN)) {
            titleCnt++
        }
        var mr = CONNECTING_PATTERN.find(line)
        if (mr != null) {
            val addr = mr.groupValues[2]
            val port = mr.groupValues[3].toInt()
            callback.onConnecting(addr, port)
        }
        mr = CONNECTED_PATTERN.find(line)
        if (mr != null) {
            val values = mr.groupValues
            val laddr = values[2]
            val lport = values[3].toInt()
            val raddr = values[4]
            val rport = values[5].toInt()
            callback.onConnected(laddr, lport, raddr, rport)
        }
        // 并发连接数为1和>1时，速率报告有以下两种格式，通过正则捕获组来截取数据
        // [  4]   9.00-10.00  sec  2.18 MBytes  18.3 Mbits/sec
        // [SUM]   9.00-10.00  sec  1.85 MBytes  15.5 Mbits/sec
        mr = REPORT_PATTERN.find(line)
        if (mr != null) {
            val values = mr.groupValues
            val st = values[1].toFloat()
            val et = values[2].toFloat()
            val trans = values[3]
            val bw = values[5]
            if (isInterval(line)) {
                callback.onInterval(st, et, trans, bw, isDown)
            } else if (isResult(line)) {
                callback.onResult(st, et, trans, bw, isDown)
            }
        }
        if (line.contains(ERR_PATTERN)) {
            callback.onError(line)
        }
    }

    private fun isInterval(line: String): Boolean {
        return isTcpInterval(line) || isUdpInterval(line)
    }

    private fun isResult(line: String): Boolean {
        return isTcpResult(line) || isUdpResult(line)
    }

    private fun isTcpInterval(line: String): Boolean {
        return titleCnt == 1
                && ((parallels == 1)
                    || (parallels > 1 && line.startsWith("[SUM]")))
    }

    private fun isTcpResult(line: String): Boolean {
        //eg:
        //[ ID] Interval           Transfer     Bandwidth       Retr
        //[  4]   0.00-10.00  sec  19.5 MBytes  16.3 Mbits/sec   19             sender
        //[  4]   0.00-10.00  sec  19.0 MBytes  15.9 Mbits/sec                  receiver
        val isLocalResult = (isDown && line.contains("receiver"))
                || (!isDown && line.contains("sender"))
        return titleCnt > 1 && isLocalResult
                && ((parallels == 1)
                    || (parallels > 1 && line.startsWith("[SUM]")))
    }

    private fun isUdpInterval(line: String): Boolean {
        // parallels == 1 eg:
        //[ ID] Interval           Transfer     Bandwidth       Total Datagrams
        //[  4]   0.00-1.00   sec  9.86 MBytes  82.7 Mbits/sec  1262
        // parallels > 1 eg:
        //[ ID] Interval           Transfer     Bandwidth       Total Datagrams
        //[SUM]   0.00-1.00   sec  10.6 MBytes  88.5 Mbits/sec  1352
        val isUdpUpInterval = (titleCnt == 1 && !line.contains(UDP_LOSS))
                && ((parallels == 1)
                    || (parallels > 1 && line.startsWith("[SUM]")))
        // parallels == 1 eg:
        //[ ID] Interval           Transfer     Bandwidth       Jitter    Lost/Total Datagrams
        //[  4]   0.00-1.00   sec   240 KBytes  1.96 Mbits/sec  2594.444 ms  12595/12625 (1e+02%)
        // parallels > 1 eg:
        //[ ID] Interval           Transfer     Bandwidth       Jitter    Lost/Total Datagrams
        //[SUM]   0.00-1.00   sec   264 KBytes  2.16 Mbits/sec  6458.650 ms  25698/25731 (1e+02%)
        val inUdpDownInterval = (titleCnt == 1 && line.contains(UDP_LOSS))
                && ((parallels == 1)
                    || (parallels > 1 && line.startsWith("[SUM]")))
        return isUdpUpInterval || inUdpDownInterval
    }

    private fun isUdpResult(line: String): Boolean {
        //-------- up --------
        // parallels == 1 eg:
        //[ ID] Interval           Transfer     Bandwidth       Jitter    Lost/Total Datagrams
        //[  4]   0.00-10.00  sec  95.3 MBytes  80.0 Mbits/sec  2.963 ms  11168/12029 (93%)
        // parallels > 1 eg:
        //[ ID] Interval           Transfer     Bandwidth       Jitter    Lost/Total Datagrams
        //[SUM]   0.00-10.00  sec   104 MBytes  86.9 Mbits/sec  7.764 ms  11935/12613 (95%)
        //-------- down --------
        // parallels == 1 eg:
        //[ ID] Interval           Transfer     Bandwidth       Jitter    Lost/Total Datagrams
        //[  4]   0.00-10.00  sec  1.16 GBytes   996 Mbits/sec  4.518 ms  151121/151406 (1e+02%)
        // parallels > 1 eg:
        //[ ID] Interval           Transfer     Bandwidth       Jitter    Lost/Total Datagrams
        //[SUM]   0.00-10.00  sec  2.33 GBytes  2002 Mbits/sec  55.278 ms  299008/299247 (1e+02%)
        return (titleCnt > 1 && line.contains(UDP_LOSS))
                && ((parallels == 1)
                    || (parallels > 1 && line.startsWith("[SUM]")))
    }
}