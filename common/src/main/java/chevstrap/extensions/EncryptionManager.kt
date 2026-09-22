package chevstrap.extensions

import java.io.FileInputStream
import java.io.IOException

object EncryptionManager {
    private const val IV_LENGTH = 12
    private const val TAG_LENGTH = 16

    private val KEY = byteArrayOf(
        0x01,
        0x23,
        0x45,
        0x67,
        0x89.toByte(),
        0xAB.toByte(),
        0xCD.toByte(),
        0xEF.toByte(),
        0x10,
        0x32,
        0x54,
        0x76,
        0x98.toByte(),
        0xBA.toByte(),
        0xDC.toByte(),
        0xFE.toByte(),
        0x55,
        0xAA.toByte(),
        0x11,
        0x22,
        0x33,
        0x44,
        0x66,
        0x77,
        0x88.toByte(),
        0x99.toByte(),
        0x00,
        0x12,
        0x34,
        0x56,
        0x78,
        0x9A.toByte()
    )

    private val ROUND_KEYS = expandKey()

    private fun u8(value: Byte): Int {
        return value.toInt() and 0xFF
    }

    private fun rotl8(value: Int, shift: Int): Int {
        return ((value shl shift) or (value ushr (8 - shift))) and 0xFF
    }

    private fun gfMulByte(a0: Int, b0: Int): Int {
        var a = a0
        var b = b0
        var result = 0

        repeat(8) {
            if ((b and 1) != 0) {
                result = result xor a
            }

            val high = a and 0x80

            a = (a shl 1) and 0xFF

            if (high != 0) {
                a = a xor 0x1B
            }

            b = b ushr 1
        }

        return result
    }

    private fun multiplicativeInverse(value: Int): Int {
        if (value == 0) {
            return 0
        }

        var result = 1
        var base = value
        var exponent = 254

        while (exponent != 0) {
            if ((exponent and 1) != 0) {
                result = gfMulByte(result, base)
            }

            base = gfMulByte(base, base)
            exponent = exponent ushr 1
        }

        return result
    }

    private fun sBox(value: Int): Int {
        val inverse = multiplicativeInverse(value)

        return (
                inverse xor
                        rotl8(inverse, 1) xor
                        rotl8(inverse, 2) xor
                        rotl8(inverse, 3) xor
                        rotl8(inverse, 4) xor
                        0x63
                ) and 0xFF
    }

    private fun recon(round: Int): Int {
        var value = 1

        repeat(round - 1) {
            value = gfMulByte(value, 2)
        }

        return value
    }

    private fun expandKey(): ByteArray {
        require(KEY.size == 32)

        val expanded = ByteArray(240)

        KEY.copyInto(expanded)

        var bytesGenerated = 32
        var rconIndex = 1

        val temp = ByteArray(4)

        while (bytesGenerated < expanded.size) {

            for (i in 0 until 4) {
                temp[i] = expanded[bytesGenerated - 4 + i]
            }

            if (bytesGenerated % 32 == 0) {

                val t = temp[0]
                temp[0] = temp[1]
                temp[1] = temp[2]
                temp[2] = temp[3]
                temp[3] = t

                for (i in 0 until 4) {
                    temp[i] = sBox(u8(temp[i])).toByte()
                }

                temp[0] = (
                        u8(temp[0]) xor recon(rconIndex)
                        ).toByte()

                rconIndex++

            } else if (bytesGenerated % 32 == 16) {

                for (i in 0 until 4) {
                    temp[i] = sBox(u8(temp[i])).toByte()
                }
            }

            for (i in 0 until 4) {
                expanded[bytesGenerated] = (
                        expanded[bytesGenerated - 32].toInt() xor
                                temp[i].toInt()
                        ).toByte()

                bytesGenerated++
            }
        }

        return expanded
    }

    private fun addRoundKey(
        state: ByteArray,
        round: Int
    ) {
        val offset = round * 16

        for (i in 0 until 16) {
            state[i] = (
                    state[i].toInt() xor
                            ROUND_KEYS[offset + i].toInt()
                    ).toByte()
        }
    }

    private fun subBytes(state: ByteArray) {
        for (i in state.indices) {
            state[i] = sBox(u8(state[i])).toByte()
        }
    }

    private fun shiftRows(state: ByteArray) {
        val original = state.copyOf()

        for (row in 0 until 4) {
            for (column in 0 until 4) {
                val sourceColumn = (column + row) % 4

                state[column * 4 + row] =
                    original[sourceColumn * 4 + row]
            }
        }
    }

    private fun mixColumns(state: ByteArray) {

        for (column in 0 until 4) {

            val offset = column * 4

            val a0 = u8(state[offset])
            val a1 = u8(state[offset + 1])
            val a2 = u8(state[offset + 2])
            val a3 = u8(state[offset + 3])

            state[offset] = (
                    gfMulByte(a0, 2) xor
                            gfMulByte(a1, 3) xor
                            a2 xor
                            a3
                    ).toByte()

            state[offset + 1] = (
                    a0 xor
                            gfMulByte(a1, 2) xor
                            gfMulByte(a2, 3) xor
                            a3
                    ).toByte()

            state[offset + 2] = (
                    a0 xor
                            a1 xor
                            gfMulByte(a2, 2) xor
                            gfMulByte(a3, 3)
                    ).toByte()

            state[offset + 3] = (
                    gfMulByte(a0, 3) xor
                            a1 xor
                            a2 xor
                            gfMulByte(a3, 2)
                    ).toByte()
        }
    }

    private fun aesEncryptBlock(input: ByteArray): ByteArray {
        require(input.size == 16)

        val state = input.copyOf()

        addRoundKey(state, 0)

        for (round in 1 until 14) {
            subBytes(state)
            shiftRows(state)
            mixColumns(state)
            addRoundKey(state, round)
        }

        subBytes(state)
        shiftRows(state)
        addRoundKey(state, 14)

        return state
    }

    private fun incrementCounter(counter: ByteArray) {
        for (i in 15 downTo 12) {
            counter[i] = (
                    (u8(counter[i]) + 1) and 0xFF
                    ).toByte()

            if (counter[i].toInt() != 0) {
                break
            }
        }
    }

    private fun xorBlock(
        data: ByteArray,
        offset: Int,
        keystream: ByteArray,
        length: Int
    ) {
        for (i in 0 until length) {
            data[offset + i] = (
                    u8(data[offset + i]) xor
                            u8(keystream[i])
                    ).toByte()
        }
    }

    private fun encryptGcm(
        plaintext: ByteArray,
        iv: ByteArray
    ): ByteArray {

        val hashSubkey = aesEncryptBlock(ByteArray(16))

        val j0 = ByteArray(16)

        iv.copyInto(
            j0,
            destinationOffset = 0,
            startIndex = 0,
            endIndex = iv.size
        )

        j0[15] = 1

        val counter = j0.copyOf()
        val ciphertext = ByteArray(plaintext.size)

        var offset = 0

        while (offset < plaintext.size) {

            incrementCounter(counter)

            val stream = aesEncryptBlock(counter)

            val length = minOf(
                16,
                plaintext.size - offset
            )

            for (i in 0 until length) {
                ciphertext[offset + i] = (
                        u8(plaintext[offset + i]) xor
                                u8(stream[i])
                        ).toByte()
            }

            offset += length
        }

        val tag = calculateTag(
            hashSubkey,
            j0,
            ciphertext
        )

        return ByteArray(ciphertext.size + TAG_LENGTH).apply {
            ciphertext.copyInto(this)
            tag.copyInto(this, ciphertext.size)
        }
    }

    private fun decryptGcm(
        encrypted: ByteArray,
        iv: ByteArray
    ): ByteArray {

        require(encrypted.size >= TAG_LENGTH) {
            "Invalid encrypted data"
        }

        val ciphertextLength = encrypted.size - TAG_LENGTH

        val ciphertext = encrypted.copyOfRange(
            0,
            ciphertextLength
        )

        val receivedTag = encrypted.copyOfRange(
            ciphertextLength,
            encrypted.size
        )

        val hashSubkey = aesEncryptBlock(ByteArray(16))

        val j0 = ByteArray(16)

        iv.copyInto(
            j0,
            destinationOffset = 0,
            startIndex = 0,
            endIndex = iv.size
        )

        j0[15] = 1

        val expectedTag = calculateTag(
            hashSubkey,
            j0,
            ciphertext
        )

        if (!constantTimeEquals(receivedTag, expectedTag)) {
            throw SecurityException("Authentication failed")
        }

        val counter = j0.copyOf()
        val plaintext = ByteArray(ciphertext.size)

        var offset = 0

        while (offset < ciphertext.size) {

            incrementCounter(counter)

            val stream = aesEncryptBlock(counter)

            val length = minOf(
                16,
                ciphertext.size - offset
            )

            xorBlock(
                ciphertext,
                offset,
                stream,
                length
            )

            ciphertext.copyInto(
                plaintext,
                destinationOffset = offset,
                startIndex = offset,
                endIndex = offset + length
            )

            offset += length
        }

        return plaintext
    }

    private fun calculateTag(
        hashSubkey: ByteArray,
        j0: ByteArray,
        ciphertext: ByteArray
    ): ByteArray {

        val hash = gHash(
            hashSubkey,
            ciphertext
        )

        val encryptedJ0 = aesEncryptBlock(j0)

        val tag = ByteArray(16)

        for (i in 0 until 16) {
            tag[i] = (
                    u8(encryptedJ0[i]) xor
                            u8(hash[i])
                    ).toByte()
        }

        return tag
    }

    private fun gHash(
        h: ByteArray,
        data: ByteArray
    ): ByteArray {

        var yHigh = 0L
        var yLow = 0L

        val hHigh = readLong(h, 0)
        val hLow = readLong(h, 8)

        var offset = 0

        while (offset < data.size) {

            val block = ByteArray(16)

            val length = minOf(
                16,
                data.size - offset
            )

            data.copyInto(
                block,
                destinationOffset = 0,
                startIndex = offset,
                endIndex = offset + length
            )

            yHigh = yHigh xor readLong(block, 0)
            yLow = yLow xor readLong(block, 8)

            val result = multiplyGcm(
                yHigh,
                yLow,
                hHigh,
                hLow
            )

            yHigh = result.first
            yLow = result.second

            offset += length
        }

        val lengthBlock = ByteArray(16)

        val ciphertextBits = data.size.toLong() * 8L

        writeLong(
            lengthBlock,
            8,
            ciphertextBits
        )

        yHigh = yHigh xor readLong(lengthBlock, 0)
        yLow = yLow xor readLong(lengthBlock, 8)

        val result = multiplyGcm(
            yHigh,
            yLow,
            hHigh,
            hLow
        )

        yHigh = result.first
        yLow = result.second

        val output = ByteArray(16)

        writeLong(output, 0, yHigh)
        writeLong(output, 8, yLow)

        return output
    }

    private fun multiplyGcm(
        xHigh: Long,
        xLow: Long,
        yHigh: Long,
        yLow: Long
    ): Pair<Long, Long> {

        var zHigh = 0L
        var zLow = 0L

        var vHigh = yHigh
        var vLow = yLow

        for (i in 0 until 128) {

            val bit = if (i < 64) {
                (xHigh ushr (63 - i)) and 1L
            } else {
                (xLow ushr (127 - i)) and 1L
            }

            if (bit != 0L) {
                zHigh = zHigh xor vHigh
                zLow = zLow xor vLow
            }

            val lsb = vLow and 1L

            vLow = (
                    (vLow ushr 1) or
                            (vHigh shl 63)
                    )

            vHigh = vHigh ushr 1

            if (lsb != 0L) {
                vHigh = vHigh xor 0xE100000000000000UL.toLong()
            }
        }

        return Pair(zHigh, zLow)
    }

    private fun readLong(
        data: ByteArray,
        offset: Int
    ): Long {

        return (
                (u8(data[offset]).toLong() shl 56) or
                        (u8(data[offset + 1]).toLong() shl 48) or
                        (u8(data[offset + 2]).toLong() shl 40) or
                        (u8(data[offset + 3]).toLong() shl 32) or
                        (u8(data[offset + 4]).toLong() shl 24) or
                        (u8(data[offset + 5]).toLong() shl 16) or
                        (u8(data[offset + 6]).toLong() shl 8) or
                        u8(data[offset + 7]).toLong()
                )
    }

    private fun writeLong(
        data: ByteArray,
        offset: Int,
        value: Long
    ) {
        data[offset] = (value ushr 56).toByte()
        data[offset + 1] = (value ushr 48).toByte()
        data[offset + 2] = (value ushr 40).toByte()
        data[offset + 3] = (value ushr 32).toByte()
        data[offset + 4] = (value ushr 24).toByte()
        data[offset + 5] = (value ushr 16).toByte()
        data[offset + 6] = (value ushr 8).toByte()
        data[offset + 7] = value.toByte()
    }

    private fun constantTimeEquals(
        a: ByteArray,
        b: ByteArray
    ): Boolean {

        if (a.size != b.size) {
            return false
        }

        var difference = 0

        for (i in a.indices) {
            difference = difference or (
                    u8(a[i]) xor u8(b[i])
                    )
        }

        return difference == 0
    }

    private fun generateIv(): ByteArray {
        val iv = ByteArray(IV_LENGTH)

        FileInputStream("/dev/urandom").use { input ->
            var offset = 0

            while (offset < iv.size) {
                val read = input.read(
                    iv,
                    offset,
                    iv.size - offset
                )

                if (read <= 0) {
                    throw IOException(
                        "Unable to obtain secure random data"
                    )
                }

                offset += read
            }
        }

        return iv
    }

    fun encrypt(plainData: ByteArray): ByteArray {

        val iv = generateIv()

        val encrypted = encryptGcm(
            plainData,
            iv
        )

        val result = ByteArray(
            4 + iv.size + encrypted.size
        )

        result[0] = 0
        result[1] = 0
        result[2] = 0
        result[3] = iv.size.toByte()

        iv.copyInto(
            result,
            destinationOffset = 4
        )

        encrypted.copyInto(
            result,
            destinationOffset = 4 + iv.size
        )

        return result
    }

    fun decrypt(encryptedData: ByteArray): ByteArray {

        require(encryptedData.size >= 4) {
            "Invalid encrypted data"
        }

        val ivLength =
            ((u8(encryptedData[0]) shl 24) or
                    (u8(encryptedData[1]) shl 16) or
                    (u8(encryptedData[2]) shl 8) or
                    u8(encryptedData[3]))

        require(ivLength == IV_LENGTH) {
            "Invalid IV length"
        }

        require(
            encryptedData.size >=
                    4 + ivLength + TAG_LENGTH
        ) {
            "Invalid encrypted data"
        }

        val iv = encryptedData.copyOfRange(
            4,
            4 + ivLength
        )

        val encrypted = encryptedData.copyOfRange(
            4 + ivLength,
            encryptedData.size
        )

        return decryptGcm(
            encrypted,
            iv
        )
    }
}