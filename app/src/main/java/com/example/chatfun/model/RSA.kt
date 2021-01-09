package com.example.chatfun.model

import android.util.Base64
import java.security.*
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import kotlin.collections.HashMap

class RSA {
    val KEY_ALGORITHM = "RSA"
    val SIGNATURE_ALGORITHM = "MD5withRSA"

    private val PUBLIC_KEY = "RSAPublicKey"
    private val PRIVATE_KEY = "RSAPrivateKey"

    @Throws(Exception::class)
    fun decryptBASE64(key: String?): ByteArray {
        return Base64.decode(key, Base64.DEFAULT)
    }

    @Throws(Exception::class)
    fun encryptBASE64(key: ByteArray?): String? {
        return Base64.encodeToString(key, Base64.DEFAULT)
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param data
     * 加密数据
     * @param privateKey
     * 私钥
     *
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun sign(data: ByteArray?, privateKey: String?): String? {
        // 解密由base64编码的私钥
        val keyBytes = decryptBASE64(privateKey)

        // 构造PKCS8EncodedKeySpec对象
        val pkcs8KeySpec = PKCS8EncodedKeySpec(keyBytes)

        // KEY_ALGORITHM 指定的加密算法
        val keyFactory: KeyFactory = KeyFactory.getInstance(KEY_ALGORITHM)

        // 取私钥匙对象
        val priKey: PrivateKey = keyFactory.generatePrivate(pkcs8KeySpec)

        // 用私钥对信息生成数字签名
        val signature: Signature = Signature.getInstance(SIGNATURE_ALGORITHM)
        signature.initSign(priKey)
        signature.update(data)
        return encryptBASE64(signature.sign())
    }

    /**
     * 校验数字签名
     *
     * @param data
     * 加密数据
     * @param publicKey
     * 公钥
     * @param sign
     * 数字签名
     *
     * @return 校验成功返回true 失败返回false
     * @throws Exception
     */
    @Throws(Exception::class)
    fun verify(data: ByteArray?, publicKey: String?, sign: String?): Boolean {

        // 解密由base64编码的公钥
        val keyBytes = decryptBASE64(publicKey)

        // 构造X509EncodedKeySpec对象
        val keySpec = X509EncodedKeySpec(keyBytes)

        // KEY_ALGORITHM 指定的加密算法
        val keyFactory: KeyFactory = KeyFactory.getInstance(KEY_ALGORITHM)

        // 取公钥匙对象
        val pubKey: PublicKey = keyFactory.generatePublic(keySpec)
        val signature: Signature = Signature.getInstance(SIGNATURE_ALGORITHM)
        signature.initVerify(pubKey)
        signature.update(data)

        // 验证签名是否正常
        return signature.verify(decryptBASE64(sign))
    }

    /**
     * 解密<br></br>
     * 用私钥解密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun decryptByPrivateKey(data: ByteArray?, key: String?): ByteArray? {
        // 对密钥解密
        val keyBytes = decryptBASE64(key)

        // 取得私钥
        val pkcs8KeySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory: KeyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
        val privateKey: Key = keyFactory.generatePrivate(pkcs8KeySpec)

        // 对数据解密
        val cipher: Cipher = Cipher.getInstance(keyFactory.getAlgorithm())
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        return cipher.doFinal(data)
    }

    /**
     * 解密<br></br>
     * 用公钥解密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun decryptByPublicKey(data: ByteArray?, key: String?): ByteArray? {
        // 对密钥解密
        val keyBytes = decryptBASE64(key)

        // 取得公钥
        val x509KeySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory: KeyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
        val publicKey: Key = keyFactory.generatePublic(x509KeySpec)

        // 对数据解密
        val cipher: Cipher = Cipher.getInstance(keyFactory.getAlgorithm())
        cipher.init(Cipher.DECRYPT_MODE, publicKey)
        return cipher.doFinal(data)
    }

    /**
     * 加密<br></br>
     * 用公钥加密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun encryptByPublicKey(data: ByteArray?, key: String?): ByteArray? {
        // 对公钥解密
        val keyBytes = decryptBASE64(key)

        // 取得公钥
        val x509KeySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory: KeyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
        val publicKey: Key = keyFactory.generatePublic(x509KeySpec)

        // 对数据加密
        val cipher: Cipher = Cipher.getInstance(keyFactory.getAlgorithm())
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(data)
    }

    /**
     * 加密<br></br>
     * 用私钥加密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun encryptByPrivateKey(data: ByteArray?, key: String?): ByteArray? {
        // 对密钥解密
        val keyBytes = decryptBASE64(key)

        // 取得私钥
        val pkcs8KeySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory: KeyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
        val privateKey: Key = keyFactory.generatePrivate(pkcs8KeySpec)

        // 对数据加密
        val cipher: Cipher = Cipher.getInstance(keyFactory.getAlgorithm())
        cipher.init(Cipher.ENCRYPT_MODE, privateKey)
        return cipher.doFinal(data)
    }

    /**
     * 取得私钥
     *
     * @param keyMap
     * @return
     * @throws Exception
     */
    fun getPrivateKey(keyMap: Map<String, Any>?): String? {
        val key: Key? = keyMap?.get(PRIVATE_KEY) as Key?
        return encryptBASE64(key!!.getEncoded())
    }

    /**
     * 取得公钥
     *
     * @param keyMap
     * @return
     * @throws Exception
     */
    fun getPublicKey(keyMap: Map<String, Any>?): String? {
        val key: Key? = keyMap?.get(PUBLIC_KEY) as Key?
        return encryptBASE64(key!!.getEncoded())
    }

    /**
     * 初始化密钥
     *
     * @return
     * @throws Exception
     */
    fun initKey(): Map<String, Any>? {
        val keyPairGen: KeyPairGenerator = KeyPairGenerator
            .getInstance(KEY_ALGORITHM)
        keyPairGen.initialize(1024)
        val keyPair: KeyPair = keyPairGen.generateKeyPair()

        // 公钥
        val publicKey: RSAPublicKey = keyPair.getPublic() as RSAPublicKey

        // 私钥
        val privateKey: RSAPrivateKey = keyPair.getPrivate() as RSAPrivateKey
        val keyMap: MutableMap<String, Any> = HashMap(2)
        keyMap[PUBLIC_KEY] = publicKey
        keyMap[PRIVATE_KEY] = privateKey
        return keyMap
    }
}