package com.lemay.android.book.util

import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import kotlin.math.min

/**
 * RSA: 既能用于数据加密也能用于数字签名的算法
 * RSA算法原理如下：
 * 1.随机选择两个大质数p和q，p不等于q，计算N=pq
 * 2.选择一个大于1小于N的自然数e，e必须与(p-1)(q-1)互素
 * 3.用公式计算出d：d×e = 1 (mod (p-1)(q-1))
 * 4.销毁p和q
 * 5.最终得到的N和e就是“公钥”，d就是“私钥”，发送方使用N去加密数据，接收方只有使用d才能解开数据内容
 * 基于大数计算，比DES要慢上几倍，通常只能用于加密少量数据或者加密密钥
 * 私钥加解密都很耗时，服务器要求解密效率高，客户端私钥加密，服务器公钥解密比较好一点
 * Created by Song on 2017/2/22.
 */
class RSA private constructor() {


    init {
        throw UnsupportedOperationException("constrontor cannot be init")
    }

    companion object {

        val RSA = "RSA" // 非对称加密密钥算法
        /**
         * android系统的RSA实现是"RSA/None/NoPadding"，而标准JDK实现是"RSA/None/PKCS1Padding" ，
         * 这造成了在android机上加密后无法在服务器上解密的原因,所以android要和服务器相同即可。
         */
        val ECB_PKCS1_PADDING = "RSA/ECB/PKCS1Padding" //加密填充方式
        val DEFAULT_KEY_SIZE = 2048 //秘钥默认长度
        val DEFAULT_SPLIT = "#PART#".toByteArray()    // 当要加密的内容超过bufferSize，则采用partSplit进行分块加密
        val DEFAULT_BUFFERSIZE = DEFAULT_KEY_SIZE / 8 - 11 // 当前秘钥支持加密的最大字节数

        /**
         * 随机生成RSA密钥对
         *
         * @param keyLength 密钥长度，范围：512～2048
         * 一般1024
         *
         *
         * 使用：
         * KeyPair keyPair=RSAUtils.generateRSAKeyPair(RSAUtils.DEFAULT_KEY_SIZE);
         * 公钥
         * RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
         * 私钥
         * RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
         * @return
         */
        fun generateRSAKeyPair(keyLength: Int): KeyPair? {

            try {
                val kpg = KeyPairGenerator.getInstance(RSA)
                kpg.initialize(keyLength)
                return kpg.genKeyPair()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
                return null
            }

        }


        /**
         * 公钥对字符串进行加密
         *
         * @param data 原文
         */
        @Throws(Exception::class)
        fun encryptByPublicKey(data: ByteArray, publicKey: ByteArray): ByteArray {

            // 得到公钥
            val keySpec = X509EncodedKeySpec(publicKey)
            val kf = KeyFactory.getInstance(RSA)
            val keyPublic = kf.generatePublic(keySpec)
            // 加密数据
            val cp = Cipher.getInstance(ECB_PKCS1_PADDING)
            cp.init(Cipher.ENCRYPT_MODE, keyPublic)
            return cp.doFinal(data)
        }

        /**
         * 私钥加密
         *
         * @param data       待加密数据
         * @param privateKey 密钥
         * @return byte[] 加密数据
         */
        @Throws(Exception::class)
        fun encryptByPrivateKey(data: ByteArray, privateKey: ByteArray): ByteArray {

            // 得到私钥
            val keySpec = PKCS8EncodedKeySpec(privateKey)
            val kf = KeyFactory.getInstance(RSA)
            val keyPrivate = kf.generatePrivate(keySpec)
            // 数据加密
            val cipher = Cipher.getInstance(ECB_PKCS1_PADDING)
            cipher.init(Cipher.ENCRYPT_MODE, keyPrivate)
            return cipher.doFinal(data)
        }

        /**
         * 公钥解密
         *
         * @param data      待解密数据
         * @param publicKey 密钥
         * @return byte[] 解密数据
         */
        @Throws(Exception::class)
        fun decryptByPublicKey(data: ByteArray, publicKey: ByteArray): ByteArray {

            // 得到公钥
            val keySpec = X509EncodedKeySpec(publicKey)
            val kf = KeyFactory.getInstance(RSA)
            val keyPublic = kf.generatePublic(keySpec)
            // 数据解密
            val cipher = Cipher.getInstance(ECB_PKCS1_PADDING)
            cipher.init(Cipher.DECRYPT_MODE, keyPublic)
            return cipher.doFinal(data)
        }

        /**
         * 使用私钥进行解密
         */
        @Throws(Exception::class)
        fun decryptByPrivateKey(encrypted: ByteArray, privateKey: ByteArray): ByteArray {

            // 得到私钥
            val keySpec = PKCS8EncodedKeySpec(privateKey)
            val kf = KeyFactory.getInstance(RSA)
            val keyPrivate = kf.generatePrivate(keySpec)
            // 解密数据
            val cp = Cipher.getInstance(ECB_PKCS1_PADDING)
            cp.init(Cipher.DECRYPT_MODE, keyPrivate)
            return cp.doFinal(encrypted)
        }

        /**
         * 实现分段加密：
         * RSA非对称加密内容长度有限制，1024位key的最多只能加密127位数据，
         * 否则就会报错(javax.crypto.IllegalBlockSizeException: Data must not be longer than 117 bytes)
         * 最近使用时却出现了“不正确的长度”的异常，研究发现是由于待加密的数据超长所致。
         * RSA 算法规定：
         * 待加密的字节数不能超过密钥的长度值除以 8 再减去 11（即：KeySize / 8 - 11），
         * 而加密后得到密文的字节数，正好是密钥的长度值除以 8（即：KeySize / 8）
         */


        /**
         * 用公钥对字符串进行分段加密
         */
        @Throws(Exception::class)
        fun encryptByPublicKeyForSpilt(data: ByteArray, publicKey: ByteArray): ByteArray {

            val dataLen = data.size
            if (dataLen <= DEFAULT_BUFFERSIZE) {
                return encryptByPublicKey(data, publicKey)
            }
            val allBytes = ArrayList<Byte>(2048)
            var bufIndex = 0
            var subDataLoop = 0
            var buf: ByteArray? = ByteArray(DEFAULT_BUFFERSIZE)
            for (i in 0 until dataLen) {
                buf?.set(bufIndex, data[i])
                if (++bufIndex == DEFAULT_BUFFERSIZE || i == dataLen - 1) {
                    subDataLoop++
                    if (subDataLoop != 1) {
                        for (b in DEFAULT_SPLIT) {
                            allBytes.add(b)
                        }
                    }
                    buf?.let {
                        val encryptBytes = encryptByPublicKey(it, publicKey)
                        for (b in encryptBytes) {
                            allBytes.add(b)
                        }
                    }
                    bufIndex = 0
                    if (i == dataLen - 1) {
                        buf = null
                    } else {
                        buf = ByteArray(min(DEFAULT_BUFFERSIZE, dataLen - i - 1))
                    }
                }
            }
            val bytes = ByteArray(allBytes.size)
            run {
                var i = 0
                for (b in allBytes) {
                    bytes[i++] = b
                }
            }
            return bytes
        }

        /**
         * 私钥分段加密
         *
         * @param data       要加密的原始数据
         * @param privateKey 秘钥
         */
        @Throws(Exception::class)
        fun encryptByPrivateKeyForSpilt(data: ByteArray, privateKey: ByteArray): ByteArray {
            val dataLen = data.size
            if (dataLen <= DEFAULT_BUFFERSIZE) {
                return encryptByPrivateKey(data, privateKey)
            }
            val allBytes = ArrayList<Byte>(2048)
            var bufIndex = 0
            var subDataLoop = 0
            var buf: ByteArray? = ByteArray(DEFAULT_BUFFERSIZE)
            for (i in 0 until dataLen) {
                buf?.set(bufIndex, data[i])
                if (++bufIndex == DEFAULT_BUFFERSIZE || i == dataLen - 1) {
                    subDataLoop++
                    if (subDataLoop != 1) {
                        for (b in DEFAULT_SPLIT) {
                            allBytes.add(b)
                        }
                    }
                    buf?.let {
                        val encryptBytes = encryptByPrivateKey(it, privateKey)
                        for (b in encryptBytes) {
                            allBytes.add(b)
                        }
                    }

                    bufIndex = 0
                    if (i == dataLen - 1) {
                        buf = null
                    } else {
                        buf = ByteArray(min(DEFAULT_BUFFERSIZE, dataLen - i - 1))
                    }
                }
            }
            val bytes = ByteArray(allBytes.size)
            run {
                var i = 0
                for (b in allBytes) {
                    bytes[i++] = b
                }
            }
            return bytes
        }

        /**
         * 公钥分段解密
         *
         * @param encrypted 待解密数据
         * @param publicKey 密钥
         */
        @Throws(Exception::class)
        fun decryptByPublicKeyForSpilt(encrypted: ByteArray, publicKey: ByteArray): ByteArray {

            val splitLen = DEFAULT_SPLIT.size
            if (splitLen <= 0) {
                return decryptByPublicKey(encrypted, publicKey)
            }
            val dataLen = encrypted.size
            val allBytes = ArrayList<Byte>(1024)
            var latestStartIndex = 0
            run {
                var i = 0
                while (i < dataLen) {
                    val bt = encrypted[i]
                    var isMatchSplit = false
                    if (i == dataLen - 1) {
                        // 到data的最后了
                        val part = ByteArray(dataLen - latestStartIndex)
                        System.arraycopy(encrypted, latestStartIndex, part, 0, part.size)
                        val decryptPart = decryptByPublicKey(part, publicKey)
                        for (b in decryptPart) {
                            allBytes.add(b)
                        }
                        latestStartIndex = i + splitLen
                        i = latestStartIndex - 1
                    } else if (bt == DEFAULT_SPLIT[0]) {
                        // 这个是以split[0]开头
                        if (splitLen > 1) {
                            if (i + splitLen < dataLen) {
                                // 没有超出data的范围
                                for (j in 1 until splitLen) {
                                    if (DEFAULT_SPLIT[j] != encrypted[i + j]) {
                                        break
                                    }
                                    if (j == splitLen - 1) {
                                        // 验证到split的最后一位，都没有break，则表明已经确认是split段
                                        isMatchSplit = true
                                    }
                                }
                            }
                        } else {
                            // split只有一位，则已经匹配了
                            isMatchSplit = true
                        }
                    }
                    if (isMatchSplit) {
                        val part = ByteArray(i - latestStartIndex)
                        System.arraycopy(encrypted, latestStartIndex, part, 0, part.size)
                        val decryptPart = decryptByPublicKey(part, publicKey)
                        for (b in decryptPart) {
                            allBytes.add(b)
                        }
                        latestStartIndex = i + splitLen
                        i = latestStartIndex - 1
                    }
                    i++
                }
            }
            val bytes = ByteArray(allBytes.size)
            run {
                var i = 0
                for (b in allBytes) {
                    bytes[i++] = b
                }
            }
            return bytes
        }

        /**
         * 私钥分段解密
         */
        @Throws(Exception::class)
        fun decryptByPrivateKeyForSpilt(encrypted: ByteArray, privateKey: ByteArray): ByteArray {

            val splitLen = DEFAULT_SPLIT.size
            if (splitLen <= 0) {
                return decryptByPrivateKey(encrypted, privateKey)
            }
            val dataLen = encrypted.size
            val allBytes = ArrayList<Byte>(1024)
            var latestStartIndex = 0
            run {
                var i = 0
                while (i < dataLen) {
                    val bt = encrypted[i]
                    var isMatchSplit = false
                    if (i == dataLen - 1) {
                        // 到data的最后了
                        val part = ByteArray(dataLen - latestStartIndex)
                        System.arraycopy(encrypted, latestStartIndex, part, 0, part.size)
                        val decryptPart = decryptByPrivateKey(part, privateKey)
                        for (b in decryptPart) {
                            allBytes.add(b)
                        }
                        latestStartIndex = i + splitLen
                        i = latestStartIndex - 1
                    } else if (bt == DEFAULT_SPLIT[0]) {
                        // 这个是以split[0]开头
                        if (splitLen > 1) {
                            if (i + splitLen < dataLen) {
                                // 没有超出data的范围
                                for (j in 1 until splitLen) {
                                    if (DEFAULT_SPLIT[j] != encrypted[i + j]) {
                                        break
                                    }
                                    if (j == splitLen - 1) {
                                        // 验证到split的最后一位，都没有break，则表明已经确认是split段
                                        isMatchSplit = true
                                    }
                                }
                            }
                        } else {
                            // split只有一位，则已经匹配了
                            isMatchSplit = true
                        }
                    }
                    if (isMatchSplit) {
                        val part = ByteArray(i - latestStartIndex)
                        System.arraycopy(encrypted, latestStartIndex, part, 0, part.size)
                        val decryptPart = decryptByPrivateKey(part, privateKey)
                        for (b in decryptPart) {
                            allBytes.add(b)
                        }
                        latestStartIndex = i + splitLen
                        i = latestStartIndex - 1
                    }
                    i++
                }
            }
            val bytes = ByteArray(allBytes.size)
            run {
                var i = 0
                for (b in allBytes) {
                    bytes[i++] = b
                }
            }
            return bytes
        }
    }
}
