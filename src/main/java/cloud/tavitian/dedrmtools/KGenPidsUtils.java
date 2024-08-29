/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import static cloud.tavitian.dedrmtools.Util.md5;

final class KGenPidsUtils {
    public static final byte[] charMap1 = "n5Pr6St7Uv8Wx9YzAb0Cd1Ef2Gh3Jk4M".getBytes(StandardCharsets.US_ASCII);

    private KGenPidsUtils() {
    }

    // Encode the bytes in data using the characters in charMap
    // Both data and charMap should be byte arrays
    public static byte[] encode(byte[] data, byte[] charMap) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        for (byte b : data) {
            int value = b & 0xFF; // Convert byte to an unsigned integer (0 to 255)

            int q = (value ^ 0x80) / charMap.length;
            int r = value % charMap.length;

            result.write(charMap[q]);
            result.write(charMap[r]);
        }

        return result.toByteArray();
    }

    // Hash the bytes in data and then encode the digest with the characters in map
    public static byte[] encodeHash(byte[] data, byte[] charMap) throws NoSuchAlgorithmException {
        return encode(md5(data), charMap);
    }
}
