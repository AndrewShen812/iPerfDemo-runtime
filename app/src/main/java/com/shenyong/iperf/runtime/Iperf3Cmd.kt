package com.shenyong.iperf.runtime

import android.content.Context
import android.util.Log
import org.apache.commons.io.FileUtils
import java.io.File

/**
 *
 * @author shenyong
 * @date 2020-11-10
 */
class Iperf3Cmd(private val context: Context) {

    companion object {
        private const val BINARY_NAME = "iperf3"
    }

    private val cmdPath
        get(): String {
        return "${context.filesDir.absolutePath}/$BINARY_NAME"
    }

    fun prepare() {
        val cmdFile = File(cmdPath)
        if (cmdFile.exists()) {
            cmdFile.setExecutable(true, true)
            return
        }
        try {
            FileUtils.copyInputStreamToFile(context.resources.assets.open(BINARY_NAME), cmdFile)
            cmdFile.setExecutable(true, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exec(vararg args: String): Process {
        val cmdAndArgs = arrayOfNulls<String>(args.size + 1)
        cmdAndArgs[0] = cmdPath
        System.arraycopy(args, 0, cmdAndArgs, 1, args.size)
        Log.d("iperf3", "exec command: ${cmdAndArgs.contentToString()}")

        return Runtime.getRuntime().exec(cmdAndArgs)
    }
}