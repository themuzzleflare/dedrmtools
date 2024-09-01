/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.CRC32;

import static cloud.tavitian.dedrmtools.HashUtils.md5;
import static cloud.tavitian.dedrmtools.Util.indexOf;

public final class KindleKeyUtils {
    private KindleKeyUtils() {
    }

    // Encode the bytes in data using the characters in charMap
    // Both data and charMap should be byte arrays
    static byte[] encode(byte[] data, byte[] charMap) {
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
    static byte[] encodeHash(byte[] data, byte[] charMap) throws NoSuchAlgorithmException {
        return encode(md5(data), charMap);
    }

    // Decode the byte array `data` using the byte array `map`. Returns the decoded bytes as a new byte array.
    static byte[] decode(byte[] data, byte[] map) {
        byte[] result = new byte[0];

        for (int i = 0; i < data.length - 1; i += 2) {
            int high = indexOf(map, data[i]);
            int low = indexOf(map, data[i + 1]);

            if (high == -1 || low == -1) {
                break;
            }

            int value = (((high * map.length) ^ 0x80) & 0xFF) + low;
            result = Arrays.copyOf(result, result.length + 1);
            result[result.length - 1] = (byte) value;
        }

        return result;
    }

    static long crc32(byte[] data) {
        CRC32 crc32 = new CRC32();

        crc32.update(0xFF);
        crc32.update(0xFF);
        crc32.update(0xFF);
        crc32.update(0xFF);
        crc32.update(data);

        return ~crc32.getValue() & 0xFFFFFFFFL;
    }

    public static String checksumPid(String data, byte[] charMap) throws IOException {
        return new String(checksumPid(data.getBytes(), charMap));
    }

    public static byte[] checksumPid(byte[] data, byte[] charMap) throws IOException {
        int crc = (int) crc32(data);

        crc = crc ^ (crc >> 16);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(data);

        for (int i = 0; i < 2; i++) {
            int b = crc & 0xff;
            int pos = (b / charMap.length) ^ (b % charMap.length);

            outputStream.write(charMap[pos % charMap.length]);

            crc >>= 8;
        }

        return outputStream.toByteArray();
    }
}
