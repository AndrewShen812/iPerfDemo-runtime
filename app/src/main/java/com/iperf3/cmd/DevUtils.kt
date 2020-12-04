package com.iperf3.cmd

import java.io.BufferedReader
import java.io.InputStreamReader

/**
 *
 * @author shenyong
 * @date 2020-11-10
 */
object DevUtils {
    var abiInfo = ""
    fun getCpuApi(): String {
        if (abiInfo.isEmpty()) {
            var outReader: BufferedReader? = null
            try {
                outReader = BufferedReader(InputStreamReader(
                    Runtime.getRuntime().exec("getprop ro.product.cpu.abi").inputStream))
                abiInfo = outReader.readLine()
            } finally {
                try {
                    outReader?.close()
                } catch (e: Exception) {
                }
            }
        }
        return abiInfo
    }

    fun isArmAbi(): Boolean {
        return getCpuApi().startsWith("armeabi")
    }

    fun isArm64Abi(): Boolean {
        return getCpuApi().startsWith("arm64")
    }

    fun isX86Abi(): Boolean {
        return getCpuApi().startsWith("x86")
    }
}