/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public final class CryptoUtils {
    private CryptoUtils() {
    }

    /**
     * @param key     The key to use for the HMAC
     * @param message The message to hash
     * @return The HMAC-SHA256 hash of the message as a byte array
     * @throws NoSuchAlgorithmException if the <code>HmacSHA256</code> algorithm is not available
     * @throws InvalidKeyException      if the key is invalid
     */
    public static byte[] hmacsha256(byte[] key, byte[] message) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key, "HmacSHA256");
        hmacSha256.init(secretKey);
        return hmacSha256.doFinal(message);
    }

    /**
     * @param key        The key to use for AES/CBC decryption
     * @param iv         The initialisation vector
     * @param cipherText The encrypted data
     * @return The decrypted data as a byte array
     * @throws NoSuchPaddingException             if the <code>PKCS5Padding</code> padding scheme is not available
     * @throws NoSuchAlgorithmException           if the <code>AES/CBC</code> algorithm is not available
     * @throws InvalidAlgorithmParameterException if the algorithm parameters are invalid
     * @throws InvalidKeyException                if the key is invalid
     * @throws IllegalBlockSizeException          if the block size is invalid
     * @throws BadPaddingException                if the padding is invalid
     */
    public static byte[] aescbcdecrypt(byte[] key, byte[] iv, byte[] cipherText) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        return cipher.doFinal(cipherText);
    }

    /**
     * @param key        The key to use for AES/CTR decryption
     * @param iv         The initialisation vector
     * @param cipherText The encrypted data
     * @return The decrypted data as a byte array
     * @throws NoSuchPaddingException             if the padding scheme is not available
     * @throws NoSuchAlgorithmException           if the <code>AES/CTR</code> algorithm is not available
     * @throws InvalidAlgorithmParameterException if the algorithm parameters are invalid
     * @throws InvalidKeyException                if the key is invalid
     * @throws IllegalBlockSizeException          if the block size is invalid
     * @throws BadPaddingException                if the padding is invalid
     */
    public static byte[] aesctrdecrypt(byte[] key, byte[] iv, byte[] cipherText) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        return cipher.doFinal(cipherText);
    }

    /**
     * @param password       The password to hash
     * @param salt           The salt
     * @param iterationCount The iteration count
     * @param keyLength      The key length in bytes
     * @return The hashed password as a byte array
     * @throws NoSuchAlgorithmException if the <code>PBKDF2WithHmacSHA1</code> algorithm is not available
     * @throws InvalidKeySpecException  if the key specification is invalid
     */
    public static byte[] pbkdf2hmacsha1(byte[] password, byte[] salt, int iterationCount, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String passwdstr = new String(password, StandardCharsets.ISO_8859_1);
        return pbkdf2hmacsha1(passwdstr, salt, iterationCount, keyLength);
    }

    /**
     * @param password       The password to hash
     * @param salt           The salt
     * @param iterationCount The iteration count
     * @param keyLength      The key length in bytes
     * @return The hashed password as a byte array
     * @throws NoSuchAlgorithmException if the <code>PBKDF2WithHmacSHA1</code> algorithm is not available
     * @throws InvalidKeySpecException  if the key specification is invalid
     */
    public static byte[] pbkdf2hmacsha1(String password, byte[] salt, int iterationCount, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        char[] passwdchararr = password.toCharArray();
        return pbkdf2hmacsha1(passwdchararr, salt, iterationCount, keyLength);
    }

    /**
     * @param password       The password to hash
     * @param salt           The salt
     * @param iterationCount The iteration count
     * @param keyLength      The key length in bytes
     * @return The hashed password as a byte array
     * @throws NoSuchAlgorithmException if the <code>PBKDF2WithHmacSHA1</code> algorithm is not available
     * @throws InvalidKeySpecException  if the key specification is invalid
     */
    public static byte[] pbkdf2hmacsha1(char[] password, byte[] salt, int iterationCount, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(password, salt, iterationCount, keyLength * 8);
        return factory.generateSecret(spec).getEncoded();
    }
}
