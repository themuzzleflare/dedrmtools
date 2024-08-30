/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import java.util.*;

public final class Util {
    public static final String copyright = "Copyright © 2022-2024 Paul Tavitian";

    private Util() {
    }

    /**
     * @param byteArray The byte array to format as a string.
     * @return A string representation of the byte array, resembling a Python byte string.
     */
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

    /**
     * @param data    The byte array to to left-justify.
     * @param width   The desired width of the resulting byte array.
     * @param padByte The byte to use for padding.
     * @return A new byte array of the specified width, with <code>data</code> left-justified and padded with <code>padByte</code>.
     */
    public static byte[] ljustBytes(byte[] data, int width, byte padByte) {
        if (data.length >= width)
            return data; // No padding needed if the original array is already long enough

        byte[] result = Arrays.copyOf(data, width); // Create a new array of 'width' size and copy 'data' into it

        Arrays.fill(result, data.length, width, padByte); // Fill the rest of the array with 'padByte'

        return result;
    }

    /**
     * @param data The byte array to calculate the sum of.
     * @return The checksum of the byte array.
     */
    public static int sumBytes(byte[] data) {
        int sum = 0;

        for (byte b : data) sum += b & 0xFF; // Use & 0xFF to ensure each byte is treated as an unsigned value

        return sum & 0xFF; // Apply & 0xFF to keep the result within 8 bits
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

    // Helper function to convert byte array to hex string
    public static String byteArrayToHexString(byte[] data) {
        return HexFormat.of().formatHex(data);
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

    // Helper method to find the index of a byte in a byte array, similar to Python's find() method.
    public static int indexOf(byte[] array, byte value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1; // Returns -1 if the byte is not found in the array
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
