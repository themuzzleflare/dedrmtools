/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class KindleKeyUtilsTest {
    @Test
    void crc32() {
        byte[] pid1 = "vCNIml/c".getBytes(StandardCharsets.US_ASCII);
        byte[] pid2 = "JBJfi+Wm".getBytes(StandardCharsets.US_ASCII);
        byte[] pid3 = "5m9pZCYO".getBytes(StandardCharsets.US_ASCII);
        byte[] pid4 = "bEQyy4Rz".getBytes(StandardCharsets.US_ASCII);
        byte[] pid5 = "EGnqh3QS".getBytes(StandardCharsets.US_ASCII);

        long crc321Expected = 827044802;
        long crc322Expected = 1740101228;
        long crc323Expected = 2795348181L;
        long crc324Expected = 3135611226L;
        long crc325Expected = 2682308693L;

        long crc321 = KindleKeyUtils.crc32(pid1);
        long crc322 = KindleKeyUtils.crc32(pid2);
        long crc323 = KindleKeyUtils.crc32(pid3);
        long crc324 = KindleKeyUtils.crc32(pid4);
        long crc325 = KindleKeyUtils.crc32(pid5);

        assertEquals(crc321Expected, crc321);
        assertEquals(crc322Expected, crc322);
        assertEquals(crc323Expected, crc323);
        assertEquals(crc324Expected, crc324);
        assertEquals(crc325Expected, crc325);
    }
}
