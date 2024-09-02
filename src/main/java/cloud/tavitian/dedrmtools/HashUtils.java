/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtils {
    private HashUtils() {
    }

    /**
     * @param data The data to hash
     * @return The SHA-256 hash of the data
     * @throws NoSuchAlgorithmException if the <code>SHA-256</code> algorithm is not available
     */
    public static byte[] sha256(byte[]... data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        for (byte[] bytes : data) digest.update(bytes);

        return digest.digest();
    }

    /**
     * @param data The data to hash
     * @return The MD5 hash of the data
     * @throws NoSuchAlgorithmException if the <code>MD5</code> algorithm is not available
     */
    public static byte[] md5(byte[]... data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");

        for (byte[] bytes : data) digest.update(bytes);

        return digest.digest();
    }

    /**
     * @param data The data to hash
     * @return The SHA-1 hash of the data
     * @throws NoSuchAlgorithmException if the <code>SHA-1</code> algorithm is not available
     */
    public static byte[] sha1(byte[]... data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        for (byte[] bytes : data) digest.update(bytes);

        return digest.digest();
    }
}
