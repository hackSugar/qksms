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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import javax.crypto.Cipher
import javax.crypto.Cipher.*
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class SendMessage @Inject constructor(
    private val context: Context,
    private val conversationRepo: ConversationRepository,
    private val messageRepo: MessageRepository,
    private val updateBadge: UpdateBadge
) : Interactor<SendMessage.Params>() {

    data class Params(
        val subId: Int,
        val threadId: Long,
        val addresses: List<String>,
        val body: String,
        val attachments: List<Attachment> = listOf(),
        val delay: Int = 0
    )

    fun encrypt(context:Context, strToEncrypt: String): ByteArray {
        val plainText = strToEncrypt.toByteArray(Charsets.UTF_8)
        val keygen = KeyGenerator.getInstance("AES")
        keygen.init(256)
        var key = keygen.generateKey()
        saveSecretKey(context, key)
        val cipher = getInstance("AES/GCM/NoPadding")
        cipher.init(ENCRYPT_MODE, key)
        val cipherText = cipher.doFinal(plainText)
        val sb = StringBuilder()
        sb.append(Base64.encodeToString(cipherText, Base64.DEFAULT));
//        sb.append(Base64.encodeToString(cipher.iv, Base64.DEFAULT));
        val jointCrypt = cipherText + cipher.iv;
        print("ENCRYPTED: $sb");
        return jointCrypt
    }

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
                val encd = encrypt(context, params.body);
                println(Base64.encodeToString(encd, Base64.DEFAULT));
                messageRepo.sendMessage(params.subId, threadId, params.addresses, Base64.encodeToString(encd, Base64.DEFAULT), params.attachments,
                        params.delay)
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