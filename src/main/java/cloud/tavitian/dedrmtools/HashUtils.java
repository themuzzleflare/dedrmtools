/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtils {
    private HashUtils() {
    }

    public static byte[] sha256(byte[]... data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        for (byte[] bytes : data) digest.update(bytes);

        return digest.digest();
    }

    public static byte[] md5(byte[]... data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");

        for (byte[] bytes : data) digest.update(bytes);

        return digest.digest();
    }

    public static byte[] sha1(byte[]... data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        for (byte[] bytes : data) digest.update(bytes);

        return digest.digest();
    }
}
