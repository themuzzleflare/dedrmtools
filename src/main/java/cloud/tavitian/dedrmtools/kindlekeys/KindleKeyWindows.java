/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import cloud.tavitian.dedrmtools.Debug;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final byte[] testMap8 = "YvaZ3FfUm9Nn_c1XuG4yCAzB0beVg-TtHh5SsIiR6rJjQdW2wEq7KkPpL8lOoMxD".getBytes(StandardCharsets.US_ASCII);

    private static String getSystemDirectory() {
        char[] buffer = new char[256];
        Kernel32.INSTANCE.GetSystemDirectoryW(buffer, buffer.length);
        String sysDir = Native.toString(buffer);

        Debug.println("sysDir: " + sysDir);

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

        Debug.println("serialnum: " + serialnum);

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
        if (!Crypt32.INSTANCE.CryptUnprotectData(inData, ppszDataDescr, entropyBlob, null, null, flags, outData)) {
            return "failed".getBytes();
        }

        return outData.pbData.getByteArray(0, outData.cbData);
    }

    private static String getEnvironmentVariable(String name) {
        char[] buffer = new char[256];
        int size = Kernel32.INSTANCE.GetEnvironmentVariableW(name, buffer, buffer.length);
        if (size == 0) {
            return null;
        }
        return Native.toString(buffer);
    }

    private static void checkAndAddFile(String filePath, String successMessage, List<String> kInfoFiles) {
        File file = new File(filePath);
        if (file.isFile()) {
            System.out.println(successMessage + filePath);
            kInfoFiles.add(filePath);
        }
    }

    @Override
    public List<byte[]> getIdStrings() {
        return List.of(getVolumeSerialNumber().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public byte[] getUsername() {
        char[] buffer = new char[256];
        IntByReference size = new IntByReference(buffer.length);

        while (!Advapi32.INSTANCE.GetUserNameW(buffer, size)) {
            int error = Kernel32.INSTANCE.GetLastError();
            if (error == 234) { // ERROR_MORE_DATA
                return "AlternateUserName".getBytes();
            }

            // Double the buffer size
            buffer = new char[buffer.length * 2];
            size.setValue(buffer.length);
        }

        return Native.toString(buffer).getBytes();
    }

    @Override
    public List<String> getKindleInfoFiles() {
        List<String> kInfoFiles = new ArrayList<>();
        String path = "";

        // Retrieve the LOCALAPPDATA environment variable
        if (System.getenv("LOCALAPPDATA") != null) {
            path = System.getenv("LOCALAPPDATA");
            if (!(new File(path).isDirectory())) {
                path = "";
            }
        } else {
            // Try to get the Local AppData path from registry keys
            try {
                path = Advapi32Util.registryGetStringValue(
                        WinReg.HKEY_CURRENT_USER,
                        "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\User Shell Folders",
                        "Local AppData");

                if (!(new File(path).isDirectory())) {
                    path = "";
                    try {
                        path = Advapi32Util.registryGetStringValue(
                                WinReg.HKEY_CURRENT_USER,
                                "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders",
                                "Local AppData");

                        if (!(new File(path).isDirectory())) {
                            path = "";
                        }
                    } catch (Exception e) {
                        // Ignore exception, just try the next approach
                    }
                }
            } catch (Exception e) {
                // Ignore exception, just proceed
            }
        }

        boolean found = false;

        if (path.isEmpty()) {
            System.out.println("Could not find the folder in which to look for Kindle info files.");
        } else {
            System.out.println("Searching for Kindle info files in " + path);

            // Check for various Kindle info files based on the version
            checkAndAddFile(path + "\\Amazon\\Kindle\\storage\\.kinf2018", "Found K4PC 1.25+ kinf2018 file: ", kInfoFiles);
            checkAndAddFile(path + "\\Amazon\\Kindle\\storage\\.kinf2011", "Found K4PC 1.9+ kinf2011 file: ", kInfoFiles);
            checkAndAddFile(path + "\\Amazon\\Kindle\\storage\\rainier.2.1.1.kinf", "Found K4PC 1.6-1.8 kinf file: ", kInfoFiles);
            checkAndAddFile(path + "\\Amazon\\Kindle For PC\\storage\\rainier.2.1.1.kinf", "Found K4PC 1.5 kinf file: ", kInfoFiles);
            checkAndAddFile(path + "\\Amazon\\Kindle For PC\\{AMAwzsaPaaZAzmZzZQzgZCAkZ3AjA_AY}\\kindle.info", "Found K4PC kindle.info file: ", kInfoFiles);
        }

        if (kInfoFiles.isEmpty()) {
            System.out.println("No K4PC kindle.info/kinf/kinf2011 files have been found.");
        }

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
                byte[] encryptedValue = decode(headerblob.getBytes(), testMap1);
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
                    Debug.println("version 5");
                    addedEntropy = build + guid;
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

                    System.out.println("keyname: " + keyName);

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
                        byte[] entropy = concatenateArrays(sha1(keyHash), addedEntropy.getBytes(StandardCharsets.UTF_8));
                        clearText = cryptUnprotectData(decryptedValue, entropy, 1);
                    } else if (version == 6) {
                        Debug.println("version 6");

                        byte[] ivCiphertext = decode(encdata, testMap8);
                        byte[] iv = concatenateArrays(Arrays.copyOfRange(ivCiphertext, 0, 12), new byte[]{0x00, 0x00, 0x00, 0x02});
                        byte[] ciphertext = Arrays.copyOfRange(ivCiphertext, 12, ivCiphertext.length);

                        Debug.println("ivCiphertext: " + formatByteArray(ivCiphertext));
                        Debug.println("iv: " + formatByteArray(iv));
                        Debug.println("ciphertext: " + formatByteArray(ciphertext));

                        byte[] decrypted = aesctrdecrypt(key, iv, ciphertext);

                        Debug.println("decrypted: " + formatByteArray(decrypted));

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

    private interface Kernel32 extends StdCallLibrary {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class, W32APIOptions.DEFAULT_OPTIONS);

        int GetSystemDirectoryW(char[] lpBuffer, int uSize);

        int GetVolumeInformationW(String lpRootPathName, char[] lpVolumeNameBuffer, int nVolumeNameSize,
                                  IntByReference lpVolumeSerialNumber, IntByReference lpMaximumComponentLength,
                                  IntByReference lpFileSystemFlags, char[] lpFileSystemNameBuffer, int nFileSystemNameSize);

        int GetEnvironmentVariableW(String lpName, char[] lpBuffer, int nSize);

        int GetLastError();
    }

    private interface Advapi32 extends StdCallLibrary {
        Advapi32 INSTANCE = Native.load("advapi32", Advapi32.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean GetUserNameW(char[] buffer, IntByReference size);
    }

    private interface Crypt32 extends StdCallLibrary {
        Crypt32 INSTANCE = Native.load("crypt32", Crypt32.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean CryptUnprotectData(DATA_BLOB pDataIn, PointerByReference ppszDataDescr, DATA_BLOB pOptionalEntropy,
                                   Pointer pvReserved, Pointer pPromptStruct, int dwFlags, DATA_BLOB pDataOut);
    }

    private static class DATA_BLOB extends Structure {
        public int cbData;
        public Pointer pbData;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("cbData", "pbData");
        }
    }
}
