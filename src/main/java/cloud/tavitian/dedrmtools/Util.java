/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.CRC32;

public final class Util {
    public static final String copyright = "Copyright © 2022-2024 Paul Tavitian";

    public static String formatByteArray(byte[] byteArray) {
        StringBuilder sb = new StringBuilder("b'");

        for (byte b : byteArray) {
            int unsignedByte = b & 0xFF; // Convert signed byte to unsigned
            // Printable ASCII range
            // Non-printable range, display as \xNN
            if (unsignedByte >= 32 && unsignedByte <= 126) sb.append((char) unsignedByte);
            else sb.append(String.format("\\x%02x", unsignedByte));
        }

        sb.append("'");

        return sb.toString();
    }

    public static byte[] leftJustifyBytes(byte[] data, int width, byte padByte) {
        if (data.length >= width)
            return data; // No padding needed if the original array is already long enough

        byte[] result = Arrays.copyOf(data, width);

        Arrays.fill(result, data.length, width, padByte);

        return result;
    }

    public static int checksum(byte[] data) {
        int sum = 0;

        for (byte b : data) sum += (b & 0xFF);

        return sum & 0xFF;
    }

    /**
     * @param array The byte array to search for a null byte.
     * @return The index of the first null byte in the array, or the length of the array if no null byte is found.
     */
    public static int indexOfNullByte(byte[] array) {
        for (int i = 0; i < array.length; i++) if (array[i] == 0) return i;

        return array.length;
    }

    // Helper function to convert hex string to byte array
    public static byte[] hexStringToByteArray(String s) {
        return HexFormat.of().parseHex(s);
    }

    // Helper function to concatenate byte arrays
    public static byte[] concatenateArrays(byte[]... arrays) {
        int totalLength = 0;

        for (byte[] array : arrays) totalLength += array.length;

        byte[] result = new byte[totalLength];
        int offset = 0;

        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }

        return result;
    }

    public static byte[] sha256(byte[]... data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        for (byte[] array : data) digest.update(array);

        return digest.digest();
    }

    public static byte[] md5(byte[]... data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");

        for (byte[] array : data) digest.update(array);

        return digest.digest();
    }

    public static byte[] sha1(byte[]... data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        for (byte[] array : data) digest.update(array);

        return digest.digest();
    }

    public static int[] toIntegerArray(List<Integer> list) {
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    public static List<Integer> toIntegerList(int[] data) {
        List<Integer> list = new ArrayList<>();

        for (int i : data) list.add(i);

        return list;
    }

    public static List<Integer> toIntegerList(byte[] data) {
        return ordList(data);
    }

    public static byte[] toByteArray(List<Integer> list) {
        return toByteArray(list, list.size());
    }

    public static byte[] toByteArray(List<Integer> list, int length) {
        byte[] array = new byte[length];

        for (int i = 0; i < length; i++) array[i] = list.get(i).byteValue();

        return array;
    }

    public static byte[] toByteArray(int[] array) {
        byte[] result = new byte[array.length];

        for (int i = 0; i < array.length; i++) result[i] = (byte) array[i];

        return result;
    }

    public static int ord(byte[] data) {
        return ord(data[0]);
    }

    public static int ord(byte data) {
        return data & 0xFF;
    }

    public static List<Integer> ordList(byte[] data) {
        List<Integer> list = new ArrayList<>();

        for (byte b : data) list.add(ord(b));

        return list;
    }

    public static boolean contains(byte[] haystack, byte[] needle) {
        for (int i = 0; i < haystack.length - needle.length + 1; i++) {
            if (Arrays.equals(Arrays.copyOfRange(haystack, i, i + needle.length), needle)) return true;
        }

        return false;
    }

    public static boolean contains(int[] haystack, int needle) {
        for (int i : haystack) {
            if (i == needle) return true;
        }

        return false;
    }

    public static long crc32(byte[] data) {
        CRC32 crc32 = new CRC32();

        crc32.update(0xFF);
        crc32.update(0xFF);
        crc32.update(0xFF);
        crc32.update(0xFF);
        crc32.update(data);

        return ~crc32.getValue() & 0xFFFFFFFFL;
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

    public static Set<String> toSet(String string) {
        return Set.of(string);
    }

    public static Set<String> toSet(String... strings) {
        return Arrays.stream(strings).collect(LinkedHashSet::new, Set::add, Set::addAll);
    }

    /**
     * @param s comma separated string
     * @return a set of strings
     */
    public static Set<String> commaSeparatedStringToSet(String s) {
        // split the string by commas, and remove any leading or trailing whitespace
        String[] parts = s.split("\\s*,\\s*");

        return Arrays.stream(parts).collect(LinkedHashSet::new, Set::add, Set::addAll);
    }

    /**
     * @param s string to print to the console. This is a helper function to avoid having to type System.out.println() every time.
     */
    public static void print(String s) {
        System.out.println(s);
    }

    public static void printKeyVal(String key, String val) {
        System.out.printf("%s: %s%n", key, val);
    }
}
