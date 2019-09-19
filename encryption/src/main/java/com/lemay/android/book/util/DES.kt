package com.lemay.android.book.util

import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec

/**
 * DES，3DES，ASE 对称加密工具类
 * 对称密码算法的加密密钥和解密密钥相同，对于大多数对称密码算法，加解密过程互逆
 * AES对DES提高了安全性，有限选择AES加密方式
 * Created by Song on 2017/2/21.
 */
class DES private constructor() {

    init {
        throw UnsupportedOperationException("constrontor cannot be init")
    }

    companion object {

        /**
         * 生成秘钥
         *
         * @return
         */
        fun generateKey(): ByteArray {

            var keyGen: KeyGenerator? = null
            try {
                keyGen = KeyGenerator.getInstance("DES") // 秘钥生成器
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }

            keyGen!!.init(56) // 初始秘钥生成器
            val secretKey = keyGen.generateKey() // 生成秘钥
            return secretKey.encoded // 获取秘钥字节数组
        }

        /**
         * 加密
         *
         * @return
         */
        fun encrypt(data: ByteArray, key: ByteArray): ByteArray? {

            val secretKey = SecretKeySpec(key, "DES") // 恢复秘钥
            var cipher: Cipher? = null
            var cipherBytes: ByteArray? = null
            try {
                cipher = Cipher.getInstance("DES") // 对Cipher完成加密或解密工作
                cipher!!.init(Cipher.ENCRYPT_MODE, secretKey) // 对Cipher初始化,加密模式
                cipherBytes = cipher.doFinal(data) // 加密数据
            } catch (e: InvalidKeyException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: NoSuchPaddingException) {
                e.printStackTrace()
            } catch (e: BadPaddingException) {
                e.printStackTrace()
            } catch (e: IllegalBlockSizeException) {
                e.printStackTrace()
            }

            return cipherBytes
        }

        /**
         * 解密
         *
         * @return
         */
        fun decrypt(data: ByteArray, key: ByteArray): ByteArray? {

            val secretKey = SecretKeySpec(key, "DES") // 恢复秘钥
            var cipher: Cipher? = null
            var plainBytes: ByteArray? = null

            try {
                cipher = Cipher.getInstance("DES") // 对Cipher初始化,加密模式
                cipher!!.init(Cipher.DECRYPT_MODE, secretKey) // 对Cipher初始化,加密模式
                plainBytes = cipher.doFinal(data) // 解密数据
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: NoSuchPaddingException) {
                e.printStackTrace()
            } catch (e: BadPaddingException) {
                e.printStackTrace()
            } catch (e: IllegalBlockSizeException) {
                e.printStackTrace()
            } catch (e: InvalidKeyException) {
                e.printStackTrace()
            }

            return plainBytes
        }
    }
}
