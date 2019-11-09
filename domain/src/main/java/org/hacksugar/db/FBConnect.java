package org.hacksugar.db;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class FBConnect {
    private static DatabaseReference ref;
    private static String pubKey = "";
    private static Cipher cipher;

    public FBConnect() throws NoSuchPaddingException, NoSuchAlgorithmException {
        cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        System.out.println("WRIING");
        ref = myRef;
    }
    public void addNumber(String key, String hash) {
        ref.child("keys").child(hash).setValue(key);
    }
    public static boolean checkNums(List<String> nums) {
        final boolean[] allNumsEncrypt = {true};
        List<String> newNums = new ArrayList<>();
        for(String number : nums) {
            if (number.contains("+")) {
                newNums.add(number.replace("+", ""));
            }
        }
        for(String number : newNums) {
            System.out.println(hash(number));
            ref.child("keys").child(hash(number)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        allNumsEncrypt[0] = false;
                    } else {
                        pubKey = String.valueOf(dataSnapshot.getValue());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        return allNumsEncrypt[0];
    }
    public static String encryptMessage() throws NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        String message = "";

        byte[] byteKey = Base64.decode(pubKey.getBytes(), Base64.DEFAULT);
        X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        PublicKey pubkey =  kf.generatePublic(X509publicKey);

        return encrypt(message, pubkey);
    }
    public static String hash(String password) {
        try {
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e1) {
                e1.printStackTrace();
            }
            digest.reset();
            return bin2hex(digest.digest(password.getBytes()));
        } catch (Exception ignored) {
            return null;
        }
    }
    private static String bin2hex(byte[] data) {
        StringBuilder hex = new StringBuilder(data.length * 2);
        for (byte b : data)
            hex.append(String.format("%02x", b & 0xFF));
        return hex.toString();
    }
    static String encrypt(String data, PublicKey key) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] bytes = cipher.doFinal(data.getBytes());
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
}
