package com.lemay.android.book.util

import android.text.TextUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.channels.FileChannel
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * author: ly
 * date  : 2019/9/19 18:51
 * desc  :
 */
class MD5 private constructor() {

    init {
        throw UnsupportedOperationException("constrontor cannot be init")
    }

    companion object {

        /**
         * 字符串加密
         * @param data 原字符串
         * @return 加密后新字符串
         */
        fun encryptStr(data: String): String {

            val dataBytes = data.toByteArray()
            var md5: MessageDigest? = null
            try {
                md5 = MessageDigest.getInstance("MD5")
                md5!!.update(dataBytes)
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }

            val resultBytes = md5!!.digest()

            val sb = StringBuilder()
            for (b in resultBytes) {
                val byte = 0xFF and b.toInt()
                if (Integer.toHexString(byte).length == 1) {
                    sb.append("0").append(Integer.toHexString(byte))
                } else {
                    sb.append(Integer.toHexString(byte))
                }
            }

            return sb.toString()
        }

        /**
         * 文件加密
         * @param filePath 文件路径
         * @return 加密后的字符串
         */
        fun encryptFile(filePath: String): String {

            val result = ""
            var fis: FileInputStream? = null
            val file = File(filePath)
            val sb = StringBuilder()
            try {
                fis = FileInputStream(file)
                val byteBuffer = fis.channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
                val md5 = MessageDigest.getInstance("MD5")
                md5.update(byteBuffer)
                val resultBytes = md5.digest()

                for (b in resultBytes) {
                    val byte = 0xFF and b.toInt()
                    if (Integer.toHexString(byte).length == 1) {
                        sb.append("0").append(Integer.toHexString(byte))
                    } else {
                        sb.append(Integer.toHexString(byte))
                    }
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }

            return sb.toString()
        }


        /**
         * 跑字典，以穷举法来破解MD5的加密，为了加大破解难度，可以采用以下方式
         */


        /**
         * 多次MD5加密
         * @param data
         * @param time 重复加密次数
         * @return
         */
        fun repeatEncrypt(data: String, time: Int): String {

            if (TextUtils.isEmpty(data)) {
                return ""
            }

            var result = encryptStr(data)
            for (i in 0 until time - 1) {
                result = encryptStr(result)
            }
            return encryptStr(result)
        }

        /**
         * MD5加盐
         *
         * 方式：
         * 1. string + key(盐值) 然后MD5加密
         * 2. 用string明文的hashcode作为盐，然后MD5加密
         * 3. 随机生成一串字符串作为盐值，然后MD5加密
         *
         * 该方法采用 string + key
         * @param data
         * @param salt
         * @return
         */
        fun encryptSalt(data: String, salt: String): String {

            if (TextUtils.isEmpty(data)) {
                return ""
            }

            val sb = StringBuilder()
            try {
                val md5 = MessageDigest.getInstance("MD5")
                val resultBytes = md5.digest((data + salt).toByteArray())
                for (b in resultBytes) {
                    val result = 0xFF and b.toInt()
                    if (Integer.toHexString(result).length == 1) {
                        sb.append("0").append(Integer.toHexString(result))
                    } else {
                        sb.append(Integer.toHexString(result))
                    }
                }
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }

            return sb.toString()
        }
    }
}
