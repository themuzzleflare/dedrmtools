/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import cloud.tavitian.dedrmtools.Debug;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cloud.tavitian.dedrmtools.CharMaps.testMap8;
import static cloud.tavitian.dedrmtools.CryptoUtils.aesctrdecrypt;
import static cloud.tavitian.dedrmtools.CryptoUtils.pbkdf2hmacsha1;
import static cloud.tavitian.dedrmtools.HashUtils.sha1;
import static cloud.tavitian.dedrmtools.HashUtils.sha256;
import static cloud.tavitian.dedrmtools.Util.concatenateArrays;
import static cloud.tavitian.dedrmtools.Util.formatByteArray;
import static cloud.tavitian.dedrmtools.kindlekeys.KindleKeyUtils.*;

final class KindleKeyWindows extends KindleKey {
    private static final byte[] charMap2 = "AaZzB0bYyCc1XxDdW2wEeVv3FfUuG4g-TtHh5SsIiR6rJjQq7KkPpL8lOoMm9Nn_".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] charMap5 = "AzB0bYyCeVvaZ3FfUuG4g-TtHh5SsIiR6rJjQq7KkPpL8lOoMm9Nn_c1XxDdW2wE".getBytes(StandardCharsets.US_ASCII);

    private static final byte[] testMap1 = "n5Pr6St7Uv8Wx9YzAb0Cd1Ef2Gh3Jk4M".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] testMap6 = "9YzAb0Cd1Ef2n5Pr6St7Uvh3Jk4M8WxG".getBytes(StandardCharsets.US_ASCII);

    private static String getSystemDirectory() {
        char[] buffer = new char[256];

        Kernel32.INSTANCE.GetSystemDirectoryW(buffer, buffer.length);

        String sysDir = Native.toString(buffer);

        Debug.printf("sysDir: %s%n", sysDir);

        return sysDir;
    }

    private static String getVolumeSerialNumber() {
        char[] volumeNameBuffer = new char[256];

        IntByReference serialNumber = new IntByReference();
        IntByReference maxComponentLen = new IntByReference();
        IntByReference fileSystemFlags = new IntByReference();

        char[] fileSystemNameBuffer = new char[256];

        String rootPath = getSystemDirectory().split("\\\\")[0] + "\\\\";
        Kernel32.INSTANCE.GetVolumeInformationW(rootPath, volumeNameBuffer, volumeNameBuffer.length,
                serialNumber, maxComponentLen, fileSystemFlags, fileSystemNameBuffer, fileSystemNameBuffer.length);

        String serialnum = String.valueOf(serialNumber.getValue() & 0xFFFFFFFFL);

        Debug.printf("serialnum: %s%n", serialnum);

        return serialnum;
    }

    private static byte[] cryptUnprotectData(byte[] data, byte[] entropy, int flags) {
        DATA_BLOB inData = new DATA_BLOB();
        inData.pbData = new Memory(data.length);
        inData.cbData = data.length;
        inData.pbData.write(0, data, 0, data.length);

        DATA_BLOB entropyBlob = new DATA_BLOB();
        entropyBlob.pbData = new Memory(entropy.length);
        entropyBlob.cbData = entropy.length;
        entropyBlob.pbData.write(0, entropy, 0, entropy.length);

        DATA_BLOB outData = new DATA_BLOB();
        PointerByReference ppszDataDescr = new PointerByReference();

        if (!Crypt32.INSTANCE.CryptUnprotectData(inData, ppszDataDescr, entropyBlob, null, null, flags, outData))
            return "failed".getBytes();

        return outData.pbData.getByteArray(0, outData.cbData);
    }

    private static String getEnvironmentVariable(String name) {
        char[] buffer = new char[256];
        int size = Kernel32.INSTANCE.GetEnvironmentVariableW(name, buffer, buffer.length);
        if (size == 0) return null;
        return Native.toString(buffer);
    }

    private static void checkAndAddFile(String filePath, String successMessage, List<String> kInfoFiles) {
        File file = new File(filePath);
        if (file.isFile()) {
            System.out.println(successMessage + filePath);
            kInfoFiles.add(filePath);
        }
    }

    private static byte[] getIdString() {
        return getVolumeSerialNumber().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] getUsername() {
        char[] buffer = new char[256];

        IntByReference size = new IntByReference(buffer.length);

        while (!Advapi32.INSTANCE.GetUserNameW(buffer, size)) {
            int error = Kernel32.INSTANCE.GetLastError();

            // ERROR_MORE_DATA
            if (error == 234) return "AlternateUserName".getBytes(StandardCharsets.UTF_8);

            // Double the buffer size
            buffer = new char[buffer.length * 2];
            size.setValue(buffer.length);
        }

        return Native.toString(buffer).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public List<String> getKindleInfoFiles() {
        List<String> kInfoFiles = new ArrayList<>();
        String path = "";

        // Retrieve the LOCALAPPDATA environment variable
        if (System.getenv("LOCALAPPDATA") != null) {
            path = System.getenv("LOCALAPPDATA");

            if (!new File(path).isDirectory()) path = "";
        } else {
            // Try to get the Local AppData path from registry keys
            try {
                path = Advapi32Util.registryGetStringValue(
                        WinReg.HKEY_CURRENT_USER,
                        "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\User Shell Folders",
                        "Local AppData");

                if (!new File(path).isDirectory()) {
                    path = "";

                    try {
                        path = Advapi32Util.registryGetStringValue(
                                WinReg.HKEY_CURRENT_USER,
                                "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders",
                                "Local AppData");

                        if (!new File(path).isDirectory()) path = "";
                    } catch (Exception _) {
                        // Ignore exception, just try the next approach
                    }
                }
            } catch (Exception _) {
                // Ignore exception, just proceed
            }
        }

        boolean found = false;

        if (path.isEmpty()) System.out.println("Could not find the folder in which to look for Kindle info files.");
        else {
            System.out.printf("Searching for Kindle info files in %s%n", path);

            // Check for various Kindle info files based on the version
            checkAndAddFile(path + "\\Amazon\\Kindle\\storage\\.kinf2018", "Found K4PC 1.25+ kinf2018 file: ", kInfoFiles);
            checkAndAddFile(path + "\\Amazon\\Kindle\\storage\\.kinf2011", "Found K4PC 1.9+ kinf2011 file: ", kInfoFiles);
            checkAndAddFile(path + "\\Amazon\\Kindle\\storage\\rainier.2.1.1.kinf", "Found K4PC 1.6-1.8 kinf file: ", kInfoFiles);
            checkAndAddFile(path + "\\Amazon\\Kindle For PC\\storage\\rainier.2.1.1.kinf", "Found K4PC 1.5 kinf file: ", kInfoFiles);
            checkAndAddFile(path + "\\Amazon\\Kindle For PC\\{AMAwzsaPaaZAzmZzZQzgZCAkZ3AjA_AY}\\kindle.info", "Found K4PC kindle.info file: ", kInfoFiles);
        }

        if (kInfoFiles.isEmpty()) System.out.println("No K4PC kindle.info/kinf/kinf2011 files have been found.");

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

        // assume .kinf2011 or .kinf2018 style .kinf file
        // the .kinf file uses "/" to separate it into records
        // so remove the trailing "/" to make it easy to use split

        byte[] data = Arrays.copyOf(fileData, fileData.length - 1);

        List<String> items = new ArrayList<>(Arrays.asList(new String(data).split("/")));

        // starts with an encoded and encrypted header blob
        String headerblob = items.removeFirst();
        byte[] encryptedValue = decode(headerblob.getBytes(), testMap1);

        try {
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

            String addedEntropy = "";
            byte[] key = null;

            if (version == 5) {
                // .kinf2011
                Debug.println("version 5");

                addedEntropy = build + guid;
            } else if (version == 6) {
                // .kinf2018
                Debug.println("version 6");

                byte[] salt = (0x6d8 * Integer.parseInt(build) + guid).getBytes(StandardCharsets.UTF_8);
                byte[] sp = concatenateArrays(getUsername(), "+@#$%+".getBytes(StandardCharsets.UTF_8), getIdString());
                byte[] passwd = encode(sha256(sp), charMap5);

                key = Arrays.copyOfRange(pbkdf2hmacsha1(passwd, salt, 10000, 0x400), 0, 32);

                Debug.printf("salt: %s%n", formatByteArray(salt));
                Debug.printf("sp: %s%n", formatByteArray(sp));
                Debug.printf("passwd: %s%n", formatByteArray(passwd));
                Debug.printf("key: %s%n", formatByteArray(key));
            }

            // Process each item
            while (!items.isEmpty()) {
                String item = items.removeFirst();
                byte[] keyHash = item.substring(0, 32).getBytes(StandardCharsets.UTF_8);
                byte[] srcnt = decode(item.substring(34).getBytes(), charMap5);

                int rcnt = Integer.parseInt(new String(srcnt));

                Debug.printf("keyHash: %s%n", formatByteArray(keyHash));
                Debug.printf("srcnt: %s%n", formatByteArray(srcnt));
                Debug.printf("rcnt: %d%n", rcnt);

                ByteArrayOutputStream edlst = new ByteArrayOutputStream();

                for (int i = 0; i < rcnt; i++) {
                    String record = items.removeFirst();
                    edlst.write(record.getBytes(StandardCharsets.UTF_8));
                }

                String keyName = "unknown";
                for (byte[] name : KindleDatabase.keyBytesList) {
                    if (Arrays.equals(encodeHash(name, testMap8), keyHash)) {
                        keyName = new String(name);
                        break;
                    }
                }

                if (keyName.equals("unknown")) keyName = new String(keyHash);

                Debug.printf("keyName: %s%n", keyName);

                // Decode the data
                byte[] encdata = edlst.toByteArray();

                Debug.printf("encdata: %s%n", formatByteArray(encdata));

                int noffset = encdata.length - primes(encdata.length / 3).getLast();
                byte[] pfx = Arrays.copyOfRange(encdata, 0, noffset);
                byte[] suffix = Arrays.copyOfRange(encdata, noffset, encdata.length);

                encdata = concatenateArrays(suffix, pfx);

                Debug.printf("encdata: %s%n", formatByteArray(encdata));

                byte[] clearText = null;

                if (version == 5) {
                    Debug.println("version 5");

                    encryptedValue = decode(encdata, testMap8);
                    byte[] entropy = concatenateArrays(sha1(keyHash), addedEntropy.getBytes(StandardCharsets.UTF_8));
                    clearText = cryptUnprotectData(encryptedValue, entropy, 1);
                } else if (version == 6) {
                    Debug.println("version 6");

                    byte[] ivCiphertext = decode(encdata, testMap8);
                    byte[] iv = concatenateArrays(Arrays.copyOfRange(ivCiphertext, 0, 12), new byte[]{0x00, 0x00, 0x00, 0x02});
                    byte[] ciphertext = Arrays.copyOfRange(ivCiphertext, 12, ivCiphertext.length);

                    Debug.printf("ivCiphertext: %s%n", formatByteArray(ivCiphertext));
                    Debug.printf("iv: %s%n", formatByteArray(iv));
                    Debug.printf("ciphertext: %s%n", formatByteArray(ciphertext));

                    byte[] decrypted = aesctrdecrypt(key, iv, ciphertext);

                    Debug.printf("decrypted: %s%n", formatByteArray(decrypted));

                    clearText = decode(decrypted, charMap5);

                    Debug.printf("clearText: %s%n", formatByteArray(clearText));
                }

                if (clearText != null && clearText.length > 0) db.put(keyName, clearText);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (db.size() > 6) {
            db.put("IDString", getIdString());
            db.put("UserName", getUsername());

            System.out.printf("Decrypted key file using IDString '%s' and UserName '%s'%n", new String(getIdString()), new String(getUsername()));
        } else {
            db.clear();

            System.out.println("Couldn't decrypt file.");
        }

        return db;
    }
}
