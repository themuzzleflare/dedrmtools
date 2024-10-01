/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Util Tests")
final class UtilTests {
    @Test
    @DisplayName("Format Byte Array Test")
    void formatByteArray() {
        byte[] bytes1 = {(byte) 0xEA, 'D', 'R', 'M', 'I', 'O', 'N', (byte) 0xEE};
        byte[] bytes2 = {'T', 'P', 'Z'};
        byte[] bytes3 = {'P', 'K', 0x03, 0x04};

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
    @DisplayName("To Set Test")
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
    @DisplayName("Comma Separated String To Sanitised Set Test")
    void commaSeparatedStringToSanitisedSet() {
        String str1 = "a,b,c";
        String str2 = "a, b, c";
        String str3 = "a, b, c, ";
        String str4 = "a,a,b,c,c,d,e,f,f,g";
        String str5 = "d, z, e, f, o ,p";
        String str6 = "a,,z,q, ,,l";

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

            Set<String> expectedSet6 = new LinkedHashSet<>() {{
                add("a");
                add("z");
                add("q");
                add("l");
            }};

            Set<String> set1 = Util.commaSeparatedStringToSanitisedSet(str1);
            Set<String> set2 = Util.commaSeparatedStringToSanitisedSet(str2);
            Set<String> set3 = Util.commaSeparatedStringToSanitisedSet(str3);
            Set<String> set4 = Util.commaSeparatedStringToSanitisedSet(str4);
            Set<String> set5 = Util.commaSeparatedStringToSanitisedSet(str5);
            Set<String> set6 = Util.commaSeparatedStringToSanitisedSet(str6);

            assertEquals(expectedSet1, set1);
            assertEquals(expectedSet2, set2);
            assertEquals(expectedSet3, set3);
            assertEquals(expectedSet4, set4);
            assertEquals(expectedSet5, set5);
            assertEquals(expectedSet6, set6);
        }
    }
}
