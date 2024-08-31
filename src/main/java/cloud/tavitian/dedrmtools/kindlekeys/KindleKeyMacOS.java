/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import cloud.tavitian.dedrmtools.Debug;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cloud.tavitian.dedrmtools.CharMaps.charMap1;
import static cloud.tavitian.dedrmtools.CharMaps.testMap8;
import static cloud.tavitian.dedrmtools.CryptoUtils.aesctrdecrypt;
import static cloud.tavitian.dedrmtools.CryptoUtils.pbkdf2hmacsha1;
import static cloud.tavitian.dedrmtools.HashUtils.sha256;
import static cloud.tavitian.dedrmtools.Util.concatenateArrays;
import static cloud.tavitian.dedrmtools.Util.formatByteArray;
import static cloud.tavitian.dedrmtools.kindlekeys.KindleKeyUtils.*;

final class KindleKeyMacOS extends KindleKey {
    private static final byte[] charMap2 = "ZB0bYyc1xDdW2wEV3Ff7KkPpL8UuGA4gz-Tme9Nn_tHh5SvXCsIiR6rJjQaqlOoM".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] charMap5 = charMap2;

    private static List<byte[]> getMacAddressesMunged() {
        List<byte[]> macNums = new ArrayList<>();

        String macNum = System.getenv("MYMACNUM");

        // Add MYMACNUM environment variable if it's set
        if (macNum != null) macNums.add(macNum.getBytes(StandardCharsets.UTF_8));

        // Command to list all hardware ports on macOS
        String command = "networksetup -listallhardwareports";

        try {
            // Execute the command
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            // Read the output line by line
            while ((line = reader.readLine()) != null) {
                int pp = line.indexOf("Ethernet Address: ");

                if (pp >= 0) {
                    // Extract the MAC address and process it
                    String mac = line.substring(pp + 18).trim();
                    String[] macList = mac.split(":");

                    // Ensure it's a valid MAC address with 6 components
                    if (macList.length != 6) continue;

                    // Convert MAC address parts to integers
                    int[] macIntList = new int[6];
                    for (int i = 0; i < 6; i++) macIntList[i] = Integer.parseInt(macList[i], 16);

                    // Munging the MAC address by XOR with 0xa5 and swapping elements
                    int[] mungedMac = new int[6];
                    mungedMac[5] = macIntList[5] ^ 0xa5;
                    mungedMac[4] = macIntList[3] ^ 0xa5;
                    mungedMac[3] = macIntList[4] ^ 0xa5;
                    mungedMac[2] = macIntList[2] ^ 0xa5;
                    mungedMac[1] = macIntList[1] ^ 0xa5;
                    mungedMac[0] = macIntList[0] ^ 0xa5;

                    // Format the munged MAC address as bytes
                    String mungedMacStr = String.format("%02x%02x%02x%02x%02x%02x",
                            mungedMac[0], mungedMac[1], mungedMac[2], mungedMac[3], mungedMac[4], mungedMac[5]);
                    macNums.add(mungedMacStr.getBytes(StandardCharsets.UTF_8));
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return macNums;
    }

    private static List<byte[]> getVolumesSerialNumbers() {
        List<byte[]> serNums = new ArrayList<>();
        String serNum = System.getenv("MYSERIALNUMBER");

        // Add MYSERIALNUMBER environment variable if it's set
        if (serNum != null) serNums.add(serNum.trim().getBytes(StandardCharsets.UTF_8));

        // Command to get the hard drive serial numbers using ioreg
        String command = "/usr/sbin/ioreg -w 0 -r -c AppleAHCIDiskDriver";

        try {
            // Execute the command
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            // Read the output line by line
            while ((line = reader.readLine()) != null) {
                int pp = line.indexOf("\"Serial Number\" = \"");

                if (pp >= 0) {
                    // Extract the serial number and process it
                    String serial = line.substring(pp + 19).trim();
                    // Remove the trailing quotation mark if present
                    if (serial.endsWith("\"")) serial = serial.substring(0, serial.length() - 1);
                    serNums.add(serial.getBytes(StandardCharsets.UTF_8));
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return serNums;
    }

    private static List<byte[]> getDiskPartitionNames() {
        List<byte[]> names = new ArrayList<>();

        // Command to list mounted partitions
        String command = "/sbin/mount";

        try {
            // Execute the command
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            // Read the output line by line
            while ((line = reader.readLine()) != null) {
                // Check if the line starts with '/dev', indicating a disk partition
                if (line.startsWith("/dev")) {
                    // Split the line into device part and mount path
                    String[] parts = line.split(" on ");
                    if (parts.length > 0) {
                        // Extract the partition name by removing the "/dev" prefix
                        String partitionName = parts[0].substring(5);
                        names.add(partitionName.getBytes(StandardCharsets.UTF_8));
                    }
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return names;
    }

    private static List<byte[]> getDiskPartitionUUIDs() {
        List<byte[]> uuids = new ArrayList<>();
        String uuidNum = System.getenv("MYUUIDNUMBER");

        // Add MYUUIDNUMBER environment variable if it's set
        if (uuidNum != null) uuids.add(uuidNum.trim().getBytes(StandardCharsets.UTF_8));

        // Command to get UUIDs of all disk partitions
        String command = "/usr/sbin/ioreg -l -S -w 0 -r -c AppleAHCIDiskDriver";

        try {
            // Execute the command
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            // Read the output line by line
            while ((line = reader.readLine()) != null) {
                int pp = line.indexOf("\"UUID\" = \"");

                if (pp >= 0) {
                    // Extract the UUID and process it
                    String uuid = line.substring(pp + 10).trim();
                    // Remove the trailing quotation mark if present
                    if (uuid.endsWith("\"")) uuid = uuid.substring(0, uuid.length() - 1);
                    uuids.add(uuid.getBytes(StandardCharsets.UTF_8));
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return uuids;
    }

    @Override
    public List<byte[]> getIdStrings() {
        // Return all possible ID Strings
        List<byte[]> strings = new ArrayList<>();

        // Extend the list with results from various methods
        strings.addAll(getMacAddressesMunged());
        strings.addAll(getVolumesSerialNumbers());
        strings.addAll(getDiskPartitionNames());
        strings.addAll(getDiskPartitionUUIDs());

        // Add a fixed byte array as per the original code
        strings.add("9999999999".getBytes());

        return strings;
    }

    @Override
    public byte[] getUsername() {
        // Get the username from the environment variables
        String username = System.getenv("USER");
        return username.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public List<String> getKindleInfoFiles() {
        // List to store found paths
        List<String> kInfoFiles = new ArrayList<>();
        boolean found = false;

        // Get the HOME environment variable
        String home = System.getenv("HOME");

        // List of known paths to check for the Kindle info files
        String[] pathsToCheck = {
                home + "/Library/Containers/com.amazon.Kindle/Data/Library/Application Support/Kindle/storage/.kinf2018",
                home + "/Library/Application Support/Kindle/storage/.kinf2018",
                home + "/Library/Containers/com.amazon.Kindle/Data/Library/Application Support/Kindle/storage/.kinf2011",
                home + "/Library/Application Support/Kindle/storage/.kinf2011",
                home + "/Library/Application Support/Kindle/storage/.rainier-2.1.1-kinf",
                home + "/Library/Application Support/Kindle/storage/.kindle-info",
                home + "/Library/Application Support/Amazon/Kindle/storage/.kindle-info",
                home + "/Library/Application Support/Amazon/Kindle for Mac/storage/.kindle-info"
        };

        // Check each path to see if the file exists
        for (String testPath : pathsToCheck) {
            File file = new File(testPath);

            if (file.exists()) {
                kInfoFiles.add(testPath);
                System.out.println("Found Kindle info file: " + testPath);
                found = true;
            }
        }

        // Print message if no files were found
        if (!found) System.out.println("No k4Mac kindle-info/rainier/kinf2011 files have been found.");

        return kInfoFiles;
    }

    @Override
    public KindleDatabase<byte[]> getDbFromFile(String kInfoFile) {
        KindleDatabaseByteValues db = new KindleDatabaseByteValues();

        // Read file content
        byte[] fileData;
        try (FileInputStream fileInputStream = new FileInputStream(kInfoFile)) {
            fileData = fileInputStream.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return db;
        }

        byte[] data = Arrays.copyOf(fileData, fileData.length - 1);

        String[] items = new String(data).split("/");

        List<byte[]> idStrings = getIdStrings();

        System.out.printf("trying username %s on file %s%n", new String(getUsername()), kInfoFile);

        byte[] foundIdString = null;

        for (byte[] idString : idStrings) {
            System.out.printf("trying IDString: %s%n", new String(idString));

            try {
                db.clear();
                List<String> itemList = new ArrayList<>(Arrays.asList(items));

                // Extract headerblob
                String headerblob = itemList.removeFirst();
                byte[] encryptedValue = decode(headerblob.getBytes(), charMap1);
                byte[] cleartext = unprotectHeaderData(encryptedValue);

                // Regex pattern for extracting version, build, and guid
                Pattern pattern = Pattern.compile("\\[Version:(\\d+)]\\[Build:(\\d+)]\\[Cksum:([^]]+)]\\[Guid:([{}a-z0-9\\-]+)]", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(new String(cleartext));

                int version = 0;
                String build = "";
                String guid = "";

                while (matcher.find()) {
                    version = Integer.parseInt(matcher.group(1));
                    build = matcher.group(2);
                    guid = matcher.group(4);
                }

                CryptUnprotectData cud = null;
                byte[] key = null;

                if (version == 5) {
                    Debug.println("version 5");
                    // Handle version 5
                    byte[] entropy = (0x2df * Integer.parseInt(build) + guid).getBytes(StandardCharsets.UTF_8);
                    cud = new CryptUnprotectData(entropy, idString);
                } else if (version == 6) {
                    Debug.println("version 6");
                    // Handle version 6
                    byte[] salt = (0x6d8 * Integer.parseInt(build) + guid).getBytes(StandardCharsets.UTF_8);
                    byte[] sp = concatenateArrays(getUsername(), "+@#$%+".getBytes(StandardCharsets.UTF_8), idString);
                    byte[] passwd = encode(sha256(sp), charMap5);
                    key = Arrays.copyOfRange(pbkdf2hmacsha1(passwd, salt, 10000, 0x400), 0, 32);

                    Debug.println("salt: " + formatByteArray(salt));
                    Debug.println("sp: " + formatByteArray(sp));
                    Debug.println("passwd: " + formatByteArray(passwd));
                    Debug.println("key: " + formatByteArray(key));
                }

                // Process each item
                while (!itemList.isEmpty()) {
                    String item = itemList.removeFirst();
                    byte[] keyHash = item.substring(0, 32).getBytes(StandardCharsets.UTF_8);
                    byte[] srcnt = decode(item.substring(34).getBytes(), charMap5);

                    int recordCount = Integer.parseInt(new String(srcnt));

                    Debug.println("keyHash: " + formatByteArray(keyHash));
                    Debug.println("srcnt: " + formatByteArray(srcnt));
                    Debug.println("recordCount: " + recordCount);

                    List<byte[]> edlst = new ArrayList<>();
                    for (int i = 0; i < recordCount; i++) {
                        String record = itemList.removeFirst();
                        edlst.add(record.getBytes(StandardCharsets.UTF_8));
                    }

                    // Process and store the clearText data
                    String keyName = "unknown";
                    for (byte[] name : KindleDatabase.keyBytesList) {
                        if (Arrays.equals(encodeHash(name, testMap8), keyHash)) {
                            keyName = new String(name);
                            break;
                        }
                    }

                    if (keyName.equals("unknown")) keyName = new String(keyHash);

                    // Decode the data
                    byte[] encdata = concatenateArrays(edlst.toArray(new byte[0][]));

                    Debug.println("encdata: " + formatByteArray(encdata));

                    int noffset = encdata.length - primes(encdata.length / 3).getLast();
                    byte[] pfx = Arrays.copyOfRange(encdata, 0, noffset);
                    byte[] suffix = Arrays.copyOfRange(encdata, noffset, encdata.length);
                    encdata = concatenateArrays(suffix, pfx);

                    Debug.println("encdata: " + formatByteArray(encdata));

                    byte[] clearText = null;

                    if (version == 5) {
                        Debug.println("version 5");

                        byte[] decryptedValue = decode(encdata, testMap8);
                        clearText = cud.decrypt(decryptedValue);
                    } else if (version == 6) {
                        Debug.println("version 6");

                        byte[] ivCiphertext = decode(encdata, testMap8);
                        byte[] iv = concatenateArrays(Arrays.copyOfRange(ivCiphertext, 0, 12), new byte[]{0x00, 0x00, 0x00, 0x02});
                        byte[] ciphertext = Arrays.copyOfRange(ivCiphertext, 12, ivCiphertext.length);

                        Debug.println("ivCiphertext: " + formatByteArray(ivCiphertext));
                        Debug.println("iv: " + formatByteArray(iv));
                        Debug.println("ciphertext: " + formatByteArray(ciphertext));

                        byte[] decrypted = aesctrdecrypt(key, iv, ciphertext);

                        clearText = decode(decrypted, charMap5);

                        Debug.println("clearText: " + formatByteArray(clearText));
                    }

                    if (clearText != null && clearText.length > 0) db.put(keyName, clearText);
                }

                if (db.size() > 6) {
                    foundIdString = idString;
                    break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (foundIdString != null) {
            System.out.printf("Decrypted key file using IDString '%s' and UserName '%s'%n", new String(foundIdString), new String(getUsername()));
            db.put("IDString", foundIdString);
            db.put("UserName", getUsername());
        } else {
            System.out.println("Couldn't decrypt file.");
            db.clear();
        }

        return db;
    }

    private class CryptUnprotectData {
        private byte[] key;
        private byte[] iv;
        private Cipher crp;

        public CryptUnprotectData(byte[] entropy, byte[] idString) {
            try {
                // Concatenate username and id_string with the specific pattern
                byte[] sp = concatenateArrays(getUsername(), "+@#$%+".getBytes(StandardCharsets.UTF_8), idString);

                // Encode using SHA-256 and custom character map (placeholder for your encode method)
                byte[] passwdData = encode(sha256(sp), charMap2);

                // Use PBKDF2 with SHA-1 to derive the key and IV
                byte[] salt = entropy;
                byte[] keyIv = pbkdf2hmacsha1(passwdData, salt, 0x800, 0x400);

                // Split the derived data into key and IV
                this.key = Arrays.copyOfRange(keyIv, 0, 32);
                this.iv = Arrays.copyOfRange(keyIv, 32, 48);

                // Initialize the cipher for decryption
                this.crp = Cipher.getInstance("AES/CBC/PKCS5Padding");
                this.crp.init(Cipher.DECRYPT_MODE, new SecretKeySpec(this.key, "AES"), new IvParameterSpec(this.iv));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public byte[] decrypt(byte[] encryptedData) {
            try {
                // Decrypt the data using the initialized cipher
                byte[] cleartext = this.crp.doFinal(encryptedData);

                return decode(cleartext, charMap2);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
