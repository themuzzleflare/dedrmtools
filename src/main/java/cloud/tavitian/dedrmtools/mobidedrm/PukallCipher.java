/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.mobidedrm;

final class PukallCipher {
    private static final int KEY_LENGTH = 16;
    private static final int KEY_COMPONENTS = 8;
    private static final int BYTE_MASK = 0xFF;
    private static final int MULT1 = 20021;
    private static final int MULT2 = 346;
    private static final int KEY_XOR_MULTIPLIER = 257;
    private static final int MASK16 = 0xFFFF;

    private PukallCipher() {
    }

    public static byte[] pc1(byte[] key, byte[] src, boolean decryption) throws Exception {
        validateKeyLength(key);

        int[] wkey = initializeWKey(key);
        byte[] dst = new byte[src.length];

        processSourceArray(src, decryption, wkey, dst);

        return dst;
    }

    private static void validateKeyLength(byte[] key) throws Exception {
        if (key.length != KEY_LENGTH) throw new Exception("PC1: Bad key length");
    }

    private static int[] initializeWKey(byte[] key) {
        int[] wkey = new int[KEY_COMPONENTS];

        for (int i = 0; i < KEY_COMPONENTS; i++)
            wkey[i] = ((key[i * 2] & BYTE_MASK) << 8) | (key[i * 2 + 1] & BYTE_MASK);

        return wkey;
    }

    private static void processSourceArray(byte[] src, boolean decryption, int[] wkey, byte[] dst) {
        int sum1 = 0, sum2 = 0, keyXorVal = 0;

        for (int i = 0; i < src.length; i++) {
            int temp1 = 0, byteXorVal = 0;

            for (int j = 0; j < KEY_COMPONENTS; j++) {
                temp1 ^= wkey[j];
                sum2 = (sum2 + j) * MULT1 + sum1;
                sum1 = (temp1 * MULT2) & MASK16;
                sum2 = (sum2 + sum1) & MASK16;
                temp1 = (temp1 * MULT1 + 1) & MASK16;
                byteXorVal ^= temp1 ^ sum2;
            }

            int curByte = src[i] & BYTE_MASK;

            if (!decryption) keyXorVal = curByte * KEY_XOR_MULTIPLIER;

            curByte = ((curByte ^ (byteXorVal >> 8)) ^ byteXorVal) & BYTE_MASK;

            if (decryption) keyXorVal = curByte * KEY_XOR_MULTIPLIER;

            for (int j = 0; j < KEY_COMPONENTS; j++) wkey[j] ^= keyXorVal;

            dst[i] = (byte) curByte;
        }
    }
}
