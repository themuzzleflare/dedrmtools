/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import java.nio.charset.StandardCharsets;

public final class CharMaps {
    public static final byte[] charMap1 = "n5Pr6St7Uv8Wx9YzAb0Cd1Ef2Gh3Jk4M".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] charMap3 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] charMap4 = "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] testMap8 = "YvaZ3FfUm9Nn_c1XuG4yCAzB0beVg-TtHh5SsIiR6rJjQdW2wEq7KkPpL8lOoMxD".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] asteriskBytes = "*".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] letters = charMap4;
    public static final byte[] bookmobiBytes = "BOOKMOBI".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] textreadBytes = "TEXtREAd".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] mopBytes = "%MOP".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] exthBytes = "EXTH".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] kfxDrmIonBytes = {(byte) 0xEA, 0x44, 0x52, 0x4D, 0x49, 0x4F, 0x4E, (byte) 0xEE};
    public static final byte[] voucherBytes = {(byte) 0xe0, 0x01, 0x00, (byte) 0xea};
    public static final byte[] protectedDataBytes = "ProtectedData".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] topazBytes = "TPZ".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] pkBytes = {0x50, 0x4B, 0x03, 0x04};
    public static final byte[] pidv3Bytes = "PIDv3".getBytes(StandardCharsets.US_ASCII);

    private CharMaps() {
    }
}
