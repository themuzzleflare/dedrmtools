/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static cloud.tavitian.dedrmtools.Util.concatenateArrays;
import static cloud.tavitian.dedrmtools.Util.toIntegerArray;

public final class KGenPids {
    private static final byte[] charMap1 = "n5Pr6St7Uv8Wx9YzAb0Cd1Ef2Gh3Jk4M".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] charMap3 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] charMap4 = "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] asteriskBytes = "*".getBytes(StandardCharsets.US_ASCII);

    private static byte[] md5(byte[]... data) throws NoSuchAlgorithmException {
        return Util.md5(data);
    }

    private static byte[] sha1(byte[]... data) throws NoSuchAlgorithmException {
        return Util.sha1(data);
    }

    // Generate the encryption table used to generate the device PID
    private static int[] generatePidEncryptionTable() {
        List<Integer> table = new ArrayList<>();

        for (int counter1 = 0; counter1 < 0x100; counter1++) {
            int value = counter1;

            for (int counter2 = 0; counter2 < 8; counter2++) {
                if ((value & 1) == 0) value = value >>> 1; // Logical right shift (unsigned shift)
                else {
                    value = value >>> 1; // Logical right shift (unsigned shift)
                    value = value ^ 0xEDB88320;
                }
            }

            table.add(value);
        }

        return toIntegerArray(table);
    }

    // Seed value used to generate the device PID
    private static int generatePidSeed(int[] table, byte[] dsn) {
        int value = 0;

        for (int counter = 0; counter < 4; counter++) {
            int index = (dsn[counter] ^ value) & 0xFF;
            value = (value >>> 8) ^ table[index];
        }

        return value;
    }

    // Generate the device PID
    private static byte[] generateDevicePid(int[] table, byte[] dsn, int nbRoll) {
        // Generate the seed
        int seed = generatePidSeed(table, dsn);

        ByteArrayOutputStream pidAscii = new ByteArrayOutputStream();

        // Initialize the pid array
        int[] pid = {
                (seed >> 24) & 0xFF, (seed >> 16) & 0xFF, (seed >> 8) & 0xFF, seed & 0xFF,
                (seed >> 24) & 0xFF, (seed >> 16) & 0xFF, (seed >> 8) & 0xFF, seed & 0xFF
        };

        int index = 0;

        // Apply rolling operation using DSN and nbRoll
        for (int counter = 0; counter < nbRoll; counter++) {
            pid[index] = pid[index] ^ (dsn[counter] & 0xFF); // XOR with DSN
            index = (index + 1) % 8;
        }

        // Convert pid to encoded ASCII using the charMap4
        for (int counter = 0; counter < 8; counter++) {
            index = ((((pid[counter] >> 5) & 3) ^ pid[counter]) & 0x1F) + (pid[counter] >> 7);
            pidAscii.write(charMap4[index]);
        }

        return pidAscii.toByteArray();
    }

    // Returns two bit at offset from a bit field
    private static int getTwoBitsFromBitField(byte[] bitField, int offset) {
        int byteNumber = offset / 4;
        int bitPosition = 6 - 2 * (offset % 4);

        return (bitField[byteNumber] >> bitPosition) & 3;
    }

    // Returns six bits at the given offset from a bit field
    private static int getSixBitsFromBitField(byte[] bitField, int offset) {
        offset *= 3;

        return (getTwoBitsFromBitField(bitField, offset) << 4)
                + (getTwoBitsFromBitField(bitField, offset + 1) << 2)
                + getTwoBitsFromBitField(bitField, offset + 2);
    }

    // 8 bits to six bits encoding from hash to generate PID string
    private static byte[] encodePid(byte[] hashVal) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (int i = 0; i < 8; i++) {
            int sixBits = getSixBitsFromBitField(hashVal, i);

            outputStream.write(charMap3[sixBits]);
        }

        return outputStream.toByteArray();
    }

    // convert from 8 digit PID to 10 digit PID with checksum
    private static byte[] checksumPid(String data) throws IOException {
        return checksumPid(data.getBytes(StandardCharsets.UTF_8));
    }

    // convert from 8 digit PID to 10 digit PID with checksum
    private static byte[] checksumPid(byte[] data) throws IOException {
        return Util.checksumPid(data, charMap4);
    }

    private static long crc32(byte[] data) {
        return Util.crc32(data);
    }

    private static long crc32(String data) {
        return crc32(data.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] pidFromSerial(byte[] serial, int length) {
        int crc = (int) crc32(serial);

        // Initialize arr1 with length l and fill with zeros
        int[] arr1 = new int[length];

        // XOR each byte of s with arr1
        for (int i = 0; i < serial.length; i++) {
            arr1[i % length] ^= serial[i];
        }

        // Extract the CRC bytes
        int[] crcBytes = new int[]{
                crc >> 24 & 0xff,
                crc >> 16 & 0xff,
                crc >> 8 & 0xff,
                crc & 0xff
        };

        // XOR arr1 with crcBytes
        for (int i = 0; i < length; i++) {
            arr1[i] ^= crcBytes[i & 3];
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (int i = 0; i < length; i++) {
            int b = arr1[i] & 0xff;
            int charIndex = (b >> 7) + ((b >> 5 & 3) ^ (b & 0x1f));

            outputStream.write(charMap4[charIndex]);
        }

        return outputStream.toByteArray();
    }

    private static Set<String> getKindlePids(byte[] rec209, byte[] token, String serialnum) throws Exception {
        return getKindlePids(rec209, token, serialnum.getBytes(StandardCharsets.UTF_8));
    }

    private static Set<String> getKindlePids(byte[] rec209, byte[] token, byte[] serialnum) throws Exception {
        Set<String> pids = new LinkedHashSet<>();

        if (rec209 == null) return Set.of(new String(serialnum));

        // Compute book PID
        byte[] bookPidHash = sha1(serialnum, rec209, token); // compute the book pid hash by calculating the sha1 of the kindle serial number, and the rec209 & token from the pid meta info of the book file
        byte[] bookPid = encodePid(bookPidHash); // encode the hash to get the pid
        bookPid = checksumPid(bookPid); // convert the pid to a 10 digit pid with checksum
        pids.add(new String(bookPid));

        // compute fixed pid for old pre 2.5 firmware update pid as well
        ByteArrayOutputStream kindlePidOutputStream = new ByteArrayOutputStream();
        kindlePidOutputStream.write(pidFromSerial(serialnum, 7));
        kindlePidOutputStream.write(asteriskBytes);
        byte[] kindlePid = checksumPid(kindlePidOutputStream.toByteArray());
        pids.add(new String(kindlePid));

        return pids;
    }

    // parse the Kindleinfo file to calculate the book pid.
    private static Set<String> getK4Pids(byte[] rec209, byte[] token, KDatabase kindleDatabase) throws Exception {
        Set<String> pids = new LinkedHashSet<>();

        byte[] kindleAccountToken = kindleDatabase.kindleDatabase().genKindleAccountToken();
        byte[] dsn;

        try {
            dsn = kindleDatabase.kindleDatabase().genDSN();
        } catch (Exception _) {
            System.out.printf("Keys not found in the database %s.%n", kindleDatabase.dbFile());
            return pids;
        }

        if (rec209 == null) {
            pids.add(new String(concatenateArrays(dsn, kindleAccountToken)));
            return pids;
        }

        // Compute the device PID
        int[] table = generatePidEncryptionTable();
        byte[] devicePid = generateDevicePid(table, dsn, 4);
        devicePid = checksumPid(devicePid);
        pids.add(new String(devicePid));

        // Compute book PIDs

        // Book PID
        byte[] pidHash = sha1(dsn, kindleAccountToken, rec209, token);
        byte[] bookPid = encodePid(pidHash);
        bookPid = checksumPid(bookPid);
        pids.add(new String(bookPid));

        // Variant 1
        pidHash = sha1(kindleAccountToken, rec209, token);
        bookPid = encodePid(pidHash);
        bookPid = checksumPid(bookPid);
        pids.add(new String(bookPid));

        // Variant 2
        pidHash = sha1(dsn, rec209, token);
        bookPid = encodePid(pidHash);
        bookPid = checksumPid(bookPid);
        pids.add(new String(bookPid));

        return pids;
    }

    public static Set<String> getPidSet(byte[] rec209, byte[] token, Set<String> serials, Set<KDatabase> kDatabases) {
        Set<String> pids = new LinkedHashSet<>();

        for (KDatabase kDatabase : kDatabases) {
            try {
                Set<String> k4Pids = getK4Pids(rec209, token, kDatabase);
                pids.addAll(k4Pids);
            } catch (Exception e) {
                System.err.printf("Error getting PIDs from database %s: %s%n", kDatabase.dbFile(), e.getMessage());
                e.printStackTrace();
            }
        }

        for (String serial : serials) {
            try {
                Set<String> kindlePids = getKindlePids(rec209, token, serial);
                pids.addAll(kindlePids);
            } catch (Exception e) {
                System.err.printf("Error getting PIDs from serial number %s: %s%n", serial, e.getMessage());
                e.printStackTrace();
            }
        }

        return pids;
    }
}
