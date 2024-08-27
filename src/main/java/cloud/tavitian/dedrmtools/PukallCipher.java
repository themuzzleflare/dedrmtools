/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

public final class PukallCipher {
    public static byte[] pc1(byte[] key, byte[] src, boolean decryption) throws Exception {
        int sum1 = 0, sum2 = 0, keyXorVal = 0;

        if (key.length != 16) throw new Exception("PC1: Bad key length");

        int[] wkey = new int[8];

        for (int i = 0; i < 8; i++) wkey[i] = ((key[i * 2] & 0xFF) << 8) | (key[i * 2 + 1] & 0xFF);

        byte[] dst = new byte[src.length];

        for (int i = 0; i < src.length; i++) {
            int temp1 = 0, byteXorVal = 0;

            for (int j = 0; j < 8; j++) {
                temp1 ^= wkey[j];
                sum2 = (sum2 + j) * 20021 + sum1;
                sum1 = (temp1 * 346) & 0xFFFF;
                sum2 = (sum2 + sum1) & 0xFFFF;
                temp1 = (temp1 * 20021 + 1) & 0xFFFF;
                byteXorVal ^= temp1 ^ sum2;
            }

            int curByte = src[i] & 0xFF;

            if (!decryption) keyXorVal = curByte * 257;

            curByte = ((curByte ^ (byteXorVal >> 8)) ^ byteXorVal) & 0xFF;

            if (decryption) keyXorVal = curByte * 257;

            for (int j = 0; j < 8; j++) wkey[j] ^= keyXorVal;

            dst[i] = (byte) curByte;
        }

        return dst;
    }
}
