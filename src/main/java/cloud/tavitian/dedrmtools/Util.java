/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class Util {
    public static final String COPYRIGHT = "Copyright © 2024 Paul Tavitian";

    private Util() {
    }

    /**
     * @param data The byte array to format as a string.
     * @return A string representation of the byte array, resembling a Python byte string.
     */
    public static @NotNull String formatByteArray(byte[] data) {
        if (data == null) return "b''"; // Return an empty byte string if the array is null

        StringBuilder sb = new StringBuilder("b'");

        for (byte b : data) {
            int unsignedByte = b & 0xFF; // Convert signed byte to unsigned
            if (unsignedByte >= 32 && unsignedByte <= 126) sb.append((char) unsignedByte); // Printable ASCII range
            else sb.append(String.format("\\x%02x", unsignedByte)); // Non-printable range, display as \xNN
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
    public static byte @NotNull [] ljustBytes(byte[] data, int width, byte padByte) {
        if (data == null) {
            data = new byte[width]; // Create a new array of 'width' size if the original array is null
            Arrays.fill(data, padByte); // Fill the array with 'padByte'
            return data;
        }

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
        if (data == null) return 0;

        int sum = 0;

        for (byte b : data) sum += b & 0xFF; // Use & 0xFF to ensure each byte is treated as an unsigned value

        return sum & 0xFF; // Apply & 0xFF to keep the result within 8 bits
    }

    /**
     * @param data The byte array to search for a null byte.
     * @return The index of the first null byte in the array, or the length of the array if no null byte is found.
     */
    public static int indexOfNullByte(byte[] data) {
        if (data == null) return 0;

        for (int i = 0; i < data.length; i++) if (data[i] == 0) return i;

        return data.length;
    }


    /**
     * @param s The hexadecimal string to convert to a byte array.
     * @return The byte array representation of the hexadecimal string.
     */
    public static byte[] hexStringToByteArray(CharSequence s) {
        return HexFormat.of().parseHex(s);
    }

    /**
     * @param data The byte array to convert to a hexadecimal string.
     * @return The hexadecimal string representation of the byte array.
     */
    public static String byteArrayToHexString(byte[] data) {
        return HexFormat.of().formatHex(data);
    }

    // Helper function to concatenate byte arrays
    public static byte @NotNull [] concatenateArrays(byte[]... data) {
        int totalLength = 0;

        for (byte[] array : data) totalLength += array.length;

        byte[] result = new byte[totalLength];
        int offset = 0;

        for (byte[] array : data) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }

        return result;
    }

    @Contract("null -> new")
    public static int @NotNull [] toIntegerArray(List<Integer> list) {
        if (list == null) return new int[0];

        int[] data = new int[list.size()];

        for (int i = 0; i < list.size(); i++) data[i] = list.get(i);

        return data;
    }

    public static @NotNull List<Integer> toIntegerList(int[] data) {
        if (data == null) return Collections.emptyList();

        List<Integer> list = new ArrayList<>();

        for (int i : data) list.add(i);

        return list;
    }

    public static @NotNull List<Integer> toIntegerList(byte[] data) {
        return ordList(data);
    }

    @Contract("null -> new")
    public static byte @NotNull [] toByteArray(List<Integer> list) {
        if (list == null) return new byte[0];

        return toByteArray(list, list.size());
    }

    @Contract("null, _ -> new")
    public static byte @NotNull [] toByteArray(List<Integer> list, int length) {
        if (list == null) return new byte[0];

        byte[] array = new byte[length];

        for (int i = 0; i < length; i++) array[i] = list.get(i).byteValue();

        return array;
    }

    @Contract(value = "null -> new", pure = true)
    public static byte @NotNull [] toByteArray(int[] data) {
        if (data == null) return new byte[0];

        return toByteArray(data, data.length);
    }

    @Contract(value = "null, _ -> new", pure = true)
    public static byte @NotNull [] toByteArray(int[] data, int length) {
        if (data == null) return new byte[0];

        byte[] result = new byte[length];

        for (int i = 0; i < length; i++) result[i] = (byte) data[i];

        return result;
    }

    @Contract("null -> new")
    @SuppressWarnings("unused")
    public static byte @NotNull [] byteListToByteArray(List<Byte> list) {
        if (list == null) return new byte[0];

        byte[] data = new byte[list.size()];

        for (int i = 0; i < list.size(); i++) data[i] = list.get(i);

        return data;
    }

    @SuppressWarnings("unused")
    public static @NotNull List<Byte> toByteList(byte[] data) {
        if (data == null) return Collections.emptyList();

        List<Byte> list = new ArrayList<>();

        for (byte b : data) list.add(b);

        return list;
    }

    @Contract(pure = true)
    public static int ord(byte @NotNull [] data) {
        return ord(data[0]);
    }

    public static int ord(byte data) {
        return data & 0xFF;
    }

    public static @NotNull List<Integer> ordList(byte[] data) {
        if (data == null) return Collections.emptyList();

        List<Integer> list = new ArrayList<>();

        for (byte b : data) list.add(ord(b));

        return list;
    }

    public static boolean contains(byte[] haystack, byte[] needle) {
        if (haystack == null || needle == null) return false;

        for (int i = 0; i < haystack.length - needle.length + 1; i++) {
            if (Arrays.equals(Arrays.copyOfRange(haystack, i, i + needle.length), needle)) return true;
        }

        return false;
    }

    public static boolean contains(int[] haystack, int needle) {
        if (haystack == null) return false;

        for (int i : haystack) {
            if (i == needle) return true;
        }

        return false;
    }

    public static Set<String> toSet(String string) {
        if (string == null) return Collections.emptySet();
        return Collections.singleton(string);
    }

    public static Set<String> toSet(String... strings) {
        return Arrays.stream(strings).collect(LinkedHashSet::new, Set::add, Set::addAll);
    }

    /**
     * @param s a comma-separated string.
     * @return an ordered set of strings.
     */
    public static Set<String> commaSeparatedStringToSet(String s) {
        if (s == null) return Collections.emptySet();
        return Arrays.stream(s.split(",")).collect(LinkedHashSet::new, Set::add, Set::addAll);
    }

    /**
     * @param s a comma-separated string.
     * @return an ordered, sanitised set of strings.
     */
    public static Set<String> commaSeparatedStringToSanitisedSet(String s) {
        return sanitiseSet(commaSeparatedStringToSet(s));
    }

    // Helper method to find the index of a byte in a byte array, similar to Python's find() method.
    public static int indexOf(byte[] data, byte value) {
        if (data == null) return -1; // Returns -1 if the array is null

        for (int i = 0; i < data.length; i++) {
            if (data[i] == value) return i;
        }

        return -1; // Returns -1 if the byte is not found in the array
    }

    @Contract("null -> new")
    public static String @NotNull [] toStringArray(List<String> list) {
        if (list == null) return new String[0]; // Return an empty array if the list is null

        return list.toArray(new String[0]); // Convert the list to an array
    }

    /**
     * @param set a set of strings.
     * @return <code>true</code> if the set is <code>null</code>, empty or contains only blank strings, <code>false</code> otherwise.
     */
    public static boolean practicalIsEmpty(Set<String> set) {
        return set == null || set.isEmpty() || set.stream().allMatch(String::isBlank);
    }

    public static Set<String> sanitiseSet(Set<String> set) {
        if (practicalIsEmpty(set)) return Collections.emptySet();

        return set.stream().map(String::trim).filter(s -> !s.isBlank()).collect(LinkedHashSet::new, Set::add, Set::addAll);
    }
}
