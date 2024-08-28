/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class UtilTest {
    @Test
    void formatByteArray() {
        byte[] bytes1 = new byte[]{(byte) 0xEA, 'D', 'R', 'M', 'I', 'O', 'N', (byte) 0xEE};
        byte[] bytes2 = new byte[]{'T', 'P', 'Z'};
        byte[] bytes3 = new byte[]{'P', 'K', 0x03, 0x04};

        String expected1 = "b'\\xeaDRMION\\xee'";
        String expected2 = "b'TPZ'";
        String expected3 = "b'PK\\x03\\x04'";

        String result1 = Util.formatByteArray(bytes1);
        String result2 = Util.formatByteArray(bytes2);
        String result3 = Util.formatByteArray(bytes3);

        assertEquals(expected1, result1);
        assertEquals(expected2, result2);
        assertEquals(expected3, result3);
    }

    @Test
    void ljustBytes() {
    }

    @Test
    void sumBytes() {
    }

    @Test
    void indexOfNullByte() {
    }

    @Test
    void hexStringToByteArray() {
    }

    @Test
    void concatenateArrays() {
    }

    @Test
    void sha256() {
    }

    @Test
    void md5() {
    }

    @Test
    void sha1() {
    }

    @Test
    void toIntegerArray() {
    }

    @Test
    void toIntegerList() {
    }

    @Test
    void testToIntegerList() {
    }

    @Test
    void toByteArray() {
    }

    @Test
    void testToByteArray() {
    }

    @Test
    void testToByteArray1() {
    }

    @Test
    void ord() {
    }

    @Test
    void testOrd() {
    }

    @Test
    void ordList() {
    }

    @Test
    void contains() {
    }

    @Test
    void testContains() {
    }

    @Test
    void crc32() {
        byte[] pid1 = "vCNIml/c".getBytes();
        byte[] pid2 = "JBJfi+Wm".getBytes();
        byte[] pid3 = "5m9pZCYO".getBytes();
        byte[] pid4 = "bEQyy4Rz".getBytes();
        byte[] pid5 = "EGnqh3QS".getBytes();

        long crc321Expected = 827044802;
        long crc322Expected = 1740101228;
        long crc323Expected = 2795348181L;
        long crc324Expected = 3135611226L;
        long crc325Expected = 2682308693L;


        long crc321 = Util.crc32(pid1);
        long crc322 = Util.crc32(pid2);
        long crc323 = Util.crc32(pid3);
        long crc324 = Util.crc32(pid4);
        long crc325 = Util.crc32(pid5);

        assertEquals(crc321Expected, crc321);
        assertEquals(crc322Expected, crc322);
        assertEquals(crc323Expected, crc323);
        assertEquals(crc324Expected, crc324);
        assertEquals(crc325Expected, crc325);
    }

    @Test
    void checksumPid() {
    }

    @Test
    void toSet() {
        for (int i = 0; i < 1000; i++) {
            Set<String> expectedSet1 = new LinkedHashSet<>() {{
                add("a");
                add("b");
                add("c");
            }};


            Set<String> expectedSet2 = new LinkedHashSet<>() {{
                add("a");
                add("b");
                add("c");
                add("d");
                add("e");
                add("f");
                add("g");
            }};

            Set<String> expectedSet3 = new LinkedHashSet<>() {{
                add("d");
                add("z");
                add("e");
                add("f");
                add("o");
                add("p");
            }};

            Set<String> set1 = Util.toSet("a", "b", "c");
            Set<String> set2 = Util.toSet("a", "a", "b", "c", "c", "d", "e", "f", "f", "g");
            Set<String> set3 = Util.toSet("d", "z", "e", "f", "o", "p");

            assertEquals(expectedSet1, set1);
            assertEquals(expectedSet2, set2);
            assertEquals(expectedSet3, set3);
        }
    }

    @Test
    void commaSeparatedStringToSet() {
        String str1 = "a,b,c";
        String str2 = "a, b, c";
        String str3 = "a, b, c, ";
        String str4 = "a,a,b,c,c,d,e,f,f,g";
        String str5 = "d, z, e, f, o ,p";

        for (int i = 0; i < 1000; i++) {
            Set<String> expectedSet1 = new LinkedHashSet<>() {{
                add("a");
                add("b");
                add("c");
            }};

            Set<String> expectedSet2 = new LinkedHashSet<>() {{
                add("a");
                add("b");
                add("c");
            }};

            Set<String> expectedSet3 = new LinkedHashSet<>() {{
                add("a");
                add("b");
                add("c");
            }};


            Set<String> expectedSet4 = new LinkedHashSet<>() {{
                add("a");
                add("b");
                add("c");
                add("d");
                add("e");
                add("f");
                add("g");
            }};

            Set<String> expectedSet5 = new LinkedHashSet<>() {{
                add("d");
                add("z");
                add("e");
                add("f");
                add("o");
                add("p");
            }};

            Set<String> set1 = Util.commaSeparatedStringToSet(str1);
            Set<String> set2 = Util.commaSeparatedStringToSet(str2);
            Set<String> set3 = Util.commaSeparatedStringToSet(str3);
            Set<String> set4 = Util.commaSeparatedStringToSet(str4);
            Set<String> set5 = Util.commaSeparatedStringToSet(str5);

            assertEquals(expectedSet1, set1);
            assertEquals(expectedSet2, set2);
            assertEquals(expectedSet3, set3);
            assertEquals(expectedSet4, set4);
            assertEquals(expectedSet5, set5);
        }
    }
}
