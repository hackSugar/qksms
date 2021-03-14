/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.interactor

import android.content.Context
import android.preference.PreferenceManager
import android.util.Base64
import com.moez.QKSMS.compat.TelephonyCompat
import com.moez.QKSMS.extensions.mapNotNull
import com.moez.QKSMS.model.Attachment
import com.moez.QKSMS.repository.ConversationRepository
import com.moez.QKSMS.repository.MessageRepository
import io.reactivex.Flowable
import org.hacksugar.db.FBConnect
import java.security.PublicKey
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import android.R.attr.key
import android.util.Log
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.X509EncodedKeySpec
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher.ENCRYPT_MODE
import javax.crypto.Cipher.getInstance
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


class SendMessage @Inject constructor(
    private val context: Context,
    private val conversationRepo: ConversationRepository,
    private val messageRepo: MessageRepository,
    private val updateBadge: UpdateBadge
) : Interactor<SendMessage.Params>() {
    fun encrypt(data: String, key: PublicKey?): String {
        val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val bytes = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
    fun decrypt(data: String, key: PrivateKey?): String {
        val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val encryptedData = Base64.decode(data, Base64.NO_WRAP)
        val decodedData = cipher.doFinal(encryptedData)
        return String(decodedData)
    }

    val pubKey = "MIGeMA0GCSqGSIb3DQEBAQUAA4GMADCBiAKBgHwbTk8D9i5/JzZJHpjlR5e6SnyT\n" +
            "HOGuz7kWJqyinIQPVI10osbeVggjOM2BhM23OF7CYAgSuIdqZYAkHLwxaCuv7OHL\n" +
            "XlpWnWgTcbz84Xg3GthZ46nilGLEBtJqqboxwjTMiTF7ZgkGAquFFW9oiNN5GI/W\n" +
            "TObhaM02YpmFT/GTAgMBAAE="
    val privKey = "MIICWwIBAAKBgHwbTk8D9i5/JzZJHpjlR5e6SnyTHOGuz7kWJqyinIQPVI10osbe\n" +
            "VggjOM2BhM23OF7CYAgSuIdqZYAkHLwxaCuv7OHLXlpWnWgTcbz84Xg3GthZ46ni\n" +
            "lGLEBtJqqboxwjTMiTF7ZgkGAquFFW9oiNN5GI/WTObhaM02YpmFT/GTAgMBAAEC\n" +
            "gYAFqAzypwCSb/MukziUyWZw8OmyMdZQJvKKwgqzNZoinrxA0j8VB08ugcR2AWA3\n" +
            "LBGiqANOeuP0MBI+O+cfYLUZr7bzspNeyQBcsV0LrOsTqs2cdRIs2EGN9eb+i+J8\n" +
            "yMxsjHPldCngJWXB0OeNzYa2UNOGahm/35XzK/jhKWf7OQJBAO20NFPUHqkfWL/z\n" +
            "G5IAxWn7mfsJbVV84c3ZFny5TzXx0Omg+IStfN2FJ2cNC6lLIeOQEwxx2sd+eYu4\n" +
            "9JdM+T8CQQCFqL56GvUcYbfCa0IH/ubrhfs0i7z8TWS5wrx0PEuBaOoyO0dFdI6e\n" +
            "xWdva6kpNq3wOO7Oz/5cGDMbBGTiQf6tAkAVYK2MFHmlcCJFMRH7sYIPpAcXIqPo\n" +
            "mlCceLejA+9xxIurV0TCee/O5FjE1dGEqjMkCiMMbXjllCROQpYMvWl1AkBWml/Q\n" +
            "/maTXT2T26uNQrydHtMF2QU69Wqucl9pcSf7Ud9tbLthZYSDm6TJrRiOe794R2t0\n" +
            "1ZAaXBPBDbfQYrKBAkEAwMPVIkttIERnQMb+Fi3l21P9JGWruy+tDclSarXtDQ5g\n" +
            "uW+6mGYiB5Ho8qqt4rUtPvcKnhWM+ikp0ql6JTn8zQ=="

    data class Params(
        val subId: Int,
        val threadId: Long,
        val addresses: List<String>,
        val body: String,
        val attachments: List<Attachment> = listOf(),
        val delay: Int = 0
    )


//    fun encrypt(context:Context, strToEncrypt: String): ByteArray {
//        val plainText = strToEncrypt.toByteArray(Charsets.UTF_8)
//        val keygen = KeyGenerator.getInstance("AES")
//        keygen.init(256)
//        var key = keygen.generateKey()
//        saveSecretKey(context, key)
//        val cipher = getInstance("AES/GCM/NoPadding")
//        cipher.init(ENCRYPT_MODE, key)
//        val cipherText = cipher.doFinal(plainText)
//        val sb = StringBuilder()
//        sb.append(Base64.encodeToString(cipherText, Base64.DEFAULT));
////        sb.append(Base64.encodeToString(cipher.iv, Base64.DEFAULT));
//        val jointCrypt = cipherText + cipher.iv;
//        print("ENCRYPTED: $sb");
//        return jointCrypt
//    }

//    fun decrypt(context:Context, dataToDecrypt: ByteArray): ByteArray {
//        val cipher = getInstance("AES/CBC/PKCS5PADDING")
//        val ivSpec = IvParameterSpec()
//        cipher.init(DECRYPT_MODE, getSavedSecretKey(context), ivSpec)
//        val cipherText = cipher.doFinal(dataToDecrypt)
//
//        val sb = StringBuilder()
//        for (b in cipherText) {
//            sb.append(b.toChar())
//        }
//        Toast.makeText(context, "dbg decrypted = [" + sb.toString() + "]", Toast.LENGTH_LONG).show()
//
//        return cipherText
//    }

    private fun saveSecretKey(context:Context, secretKey: SecretKey) {
        val baos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(baos)
        oos.writeObject(secretKey)
        val strToSave = String(android.util.Base64.encode(baos.toByteArray(), android.util.Base64.DEFAULT))
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPref.edit()
        editor.putString("secret_key", strToSave)
        editor.apply()
    }

    fun getSavedSecretKey(context: Context): SecretKey {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val strSecretKey = sharedPref.getString("secret_key", "")
        val bytes = android.util.Base64.decode(strSecretKey, android.util.Base64.DEFAULT)
        val ois = ObjectInputStream(ByteArrayInputStream(bytes))
        val secretKey = ois.readObject() as SecretKey
        return secretKey
    }


    override fun buildObservable(params: Params): Flowable<*> = Flowable.just(Unit)
            .filter { params.addresses.isNotEmpty() }
            .doOnNext {
                // If a threadId isn't provided, try to obtain one
                val threadId = when (params.threadId) {
                    0L -> TelephonyCompat.getOrCreateThreadId(context, params.addresses.toSet())
                    else -> params.threadId
                }
                val byteKey = Base64.decode(pubKey.toByteArray(), Base64.DEFAULT)
                val X509publicKey = X509EncodedKeySpec(byteKey)
                val kf = KeyFactory.getInstance("RSA")

                val pubKeyActual = kf.generatePublic(X509publicKey)
                val pkcs8EncodedBytes = Base64.decode(privKey, Base64.DEFAULT)

                // extract the private key

                val keySpec = PKCS8EncodedKeySpec(pkcs8EncodedBytes)
                val kfac = KeyFactory.getInstance("RSA")
                val privKey = kfac.generatePrivate(keySpec)

                println("BODY: " + params.addresses[0])
                val encrypted = encrypt(params.body, pubKeyActual);
                print(decrypt(encrypt(params.body, pubKeyActual), privKey))
                Log.i("WEASEL_MESSAGE_SENT", encrypted)
//                messageRepo.sendMessage(params.subId, threadId, params.addresses, "ACTUAL: " + params.body, params.attachments,
//                        params.delay)
                messageRepo.sendMessage(params.subId, threadId, params.addresses, encrypt(params.body, pubKeyActual), params.attachments,
                        params.delay)
//                messageRepo.sendMessage(params.subId, threadId, params.addresses, "DECRPYPTED: " + decrypt(encrypted, privKey), params.attachments,
//                        params.delay)
            }
            .mapNotNull {
                // If the threadId wasn't provided, then it's probably because it doesn't exist in Realm.
                // Sync it now and get the id
                when (params.threadId) {
                    0L -> conversationRepo.getOrCreateConversation(params.addresses)?.id
                    else -> params.threadId
                }
            }
            .doOnNext { threadId -> conversationRepo.updateConversations(threadId) }
            .doOnNext { threadId -> conversationRepo.markUnarchived(threadId) }
            .flatMap { updateBadge.buildObservable(Unit) } // Update the widget

}