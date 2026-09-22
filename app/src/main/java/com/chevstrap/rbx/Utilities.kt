package com.chevstrap.rbx

import kotlin.math.max

object Utilities {
    fun getVersionFromString(version: String?): IntArray {
        var version = version
        if (version.isNullOrEmpty()) return intArrayOf(0)

        if (version.startsWith("v")) version = version.substring(1)

        val idx = version.indexOf("+")
        if (idx != -1) version = version.substring(0, idx)

        val parts: Array<String?> =
            version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val numbers = IntArray(parts.size)
        for (i in parts.indices) {
            try {
                numbers[i] = parts[i]!!.toInt()
            } catch (_: NumberFormatException) {
                numbers[i] = 0
            }
        }
        return numbers
    }

    @JvmStatic
    fun compareVersions(versionStr1: String?, versionStr2: String?): VersionComparison {
        try {
            val v1 = getVersionFromString(versionStr1)
            val v2 = getVersionFromString(versionStr2)

            val length = max(v1.size, v2.size)
            for (i in 0..<length) {
                val num1 = if (i < v1.size) v1[i] else 0
                val num2 = if (i < v2.size) v2[i] else 0

                if (num1 < num2) return VersionComparison.LESS
                if (num1 > num2) return VersionComparison.GREATER
            }

            return VersionComparison.EQUAL
        } catch (e: Exception) {
            App.logger.writeLine(
                "Utilities::CompareVersions",
                "An exception occurred when comparing versions"
            )
            App.logger.writeLine(
                "Utilities::CompareVersions",
                "versionStr1=$versionStr1 versionStr2=$versionStr2"
            )
            throw e
        }
    }

    enum class VersionComparison {
        LESS,
        EQUAL,
        GREATER
    }
}
