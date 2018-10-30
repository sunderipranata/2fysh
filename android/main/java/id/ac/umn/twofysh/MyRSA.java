package id.ac.umn.twofysh;

import android.content.Context;
import android.content.Intent;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyProperties;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;

/**
 * Created by SUNDERI on 9/25/2017.
 */

public class MyRSA {
    static String TAG="MyRSA";

    static public String createKeyPair(String alias, Context c){
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            int nBefore = keyStore.size();

            // Create the keys if necessary
            if (!keyStore.containsAlias(alias)) {
                Calendar notBefore = Calendar.getInstance();
                Calendar notAfter = Calendar.getInstance();
                notAfter.add(Calendar.YEAR, 1);
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(c)
                        .setAlias(alias)
                        .setKeyType(KeyProperties.KEY_ALGORITHM_RSA)
                        .setKeySize(2048)
                        .setSubject(new X500Principal("CN=test"))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(notBefore.getTime())
                        .setEndDate(notAfter.getTime())
                        .build();
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                generator.initialize(spec);

                KeyPair keyPair = generator.generateKeyPair();
                Log.v("RSA", "new key pair created");
            }
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
            PublicKey publicKey = privateKeyEntry.getCertificate().getPublicKey(); // Don't TypeCast to RSAPublicKey
            PrivateKey privateKey = privateKeyEntry.getPrivateKey(); // Don't TypeCast to RSAPrivateKey

            Log.v("RSA Public key", Base64.encodeToString(publicKey.getEncoded(),Base64.DEFAULT));

            String x= MyRSA.encryptString(alias,"HELLO WORLD!");
            Log.v("RSA encrypt", x);
            String y= MyRSA.decryptString(alias,x);
            Log.v("RSA decrypt",y);

            return Base64.encodeToString(publicKey.getEncoded(),Base64.DEFAULT);

        } catch (Exception e){
            Log.v("RSA", "SOMETHING WENT WRONG!");
            e.printStackTrace();
        }
        return null;
    }


    static public String encryptString(String alias, String plainString) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
            PublicKey publicKey = privateKeyEntry.getCertificate().getPublicKey(); // Don't TypeCast to RSAPublicKey
            PrivateKey privateKey = privateKeyEntry.getPrivateKey(); // Don't TypeCast to RSAPrivateKey

            Log.v(TAG, "unencrypted string: "+plainString);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);

            byte [] vals = cipher.doFinal(plainString.getBytes());

            Log.v(TAG, "encrypted string in base64: "+Base64.encodeToString(vals, Base64.DEFAULT));
            Log.v(TAG, "encrypted string: "+ new String(vals));
            return Base64.encodeToString(vals, Base64.DEFAULT);
        } catch (Exception e) {
            //Toast.makeText(this, "Exception " + e.getMessage() + " occured", Toast.LENGTH_LONG).show();
            Log.e(TAG, Log.getStackTraceString(e));
            return "error";
        }
    }
    static public String decryptString(String alias, String cipherText) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
            PublicKey publicKey = privateKeyEntry.getCertificate().getPublicKey(); // Don't TypeCast to RSAPublicKey
            PrivateKey privateKey = privateKeyEntry.getPrivateKey(); // Don't TypeCast to RSAPrivateKey

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);

            Log.v(TAG,"ciphered text in base64: "+cipherText);
            byte[] cip = Base64.decode(cipherText, Base64.DEFAULT);
            byte[] decryptedText = cipher.doFinal(cip);

            String finalText = new String (decryptedText);


            Log.v(TAG, "decrypted text:"+ finalText);
            return finalText;
        } catch (Exception e) {
            //Toast.makeText(this, "Exception " + e.getMessage() + " occured", Toast.LENGTH_LONG).show();
            Log.e(TAG, Log.getStackTraceString(e));
            return "error";
        }
    }
    static public String decryptSalt(String alias, String cipherText) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
            PublicKey publicKey = privateKeyEntry.getCertificate().getPublicKey(); // Don't TypeCast to RSAPublicKey
            PrivateKey privateKey = privateKeyEntry.getPrivateKey(); // Don't TypeCast to RSAPrivateKey

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            Log.v(TAG,"ciphered text in base64: "+cipherText);
            byte[] cip = Base64.decode(cipherText, Base64.DEFAULT);
            byte[] decryptedText = cipher.doFinal(cip);

            String finalText = new String (decryptedText);


            Log.v(TAG, "decrypted text:"+ finalText);
            return finalText;
        } catch (Exception e) {
            //Toast.makeText(this, "Exception " + e.getMessage() + " occured", Toast.LENGTH_LONG).show();
            Log.e(TAG, Log.getStackTraceString(e));
            return "error";
        }
    }

    static public ArrayList<String> getAllAliasesInTheKeystore() throws KeyStoreException,IOException,NoSuchAlgorithmException,CertificateException{
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        return Collections.list(keyStore.aliases());
    }
    static public String getKeyCount() throws KeyStoreException,IOException,NoSuchAlgorithmException,CertificateException{
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        return String.valueOf(keyStore.size());
    }

    static public void deleteEntry(String alias)throws KeyStoreException,IOException,NoSuchAlgorithmException,CertificateException{
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        keyStore.deleteEntry(alias);
    }


}
