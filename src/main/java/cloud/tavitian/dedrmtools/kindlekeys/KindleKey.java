/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import cloud.tavitian.dedrmtools.Debug;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

import static cloud.tavitian.dedrmtools.CryptoUtils.aescbcdecrypt;
import static cloud.tavitian.dedrmtools.CryptoUtils.pbkdf2hmacsha1;
import static cloud.tavitian.dedrmtools.Util.byteArrayToHexString;
import static cloud.tavitian.dedrmtools.Util.formatByteArray;

public abstract class KindleKey implements KindleKeyManager {
    private static final String osName = System.getProperty("os.name").toLowerCase();

    public static KindleKey getInstance() throws Exception {
        if (osName.startsWith("win")) return new KindleKeyWindows();
        else if (osName.startsWith("mac") || osName.startsWith("darwin")) return new KindleKeyMacOS();
        else throw new Exception(String.format("Unsupported OS: %s", osName));
    }

    // Method to decrypt the encrypted data using a derived key and IV
    static byte[] unprotectHeaderData(byte[] encryptedData) throws Exception {
        Debug.printf("Encrypted data: %s%n", formatByteArray(encryptedData));

        byte[] passwdData = "header_key_data".getBytes(StandardCharsets.US_ASCII);
        byte[] salt = "HEADER.2011".getBytes(StandardCharsets.US_ASCII);

        byte[] keyIv = pbkdf2hmacsha1(passwdData, salt, 128, 256);

        Debug.printf("Derived key: %s%n", formatByteArray(keyIv));

        // Extract the AES key and IV from the derived key material
        byte[] key = Arrays.copyOfRange(keyIv, 0, 32); // First 32 bytes for AES key
        byte[] iv = Arrays.copyOfRange(keyIv, 32, 48); // Next 16 bytes for IV

        // Decrypt the data
        byte[] decryptedData = aescbcdecrypt(key, iv, encryptedData);

        Debug.printf("Decrypted data: %s%n", formatByteArray(decryptedData));

        return decryptedData;
    }

    static List<Integer> primes(int n) {
        if (n == 2) return List.of(2);
        else if (n < 2) return Collections.emptyList();

        List<Integer> primeList = new ArrayList<>();

        primeList.add(2);

        for (int potentialPrime = 3; potentialPrime <= n; potentialPrime += 2) {
            boolean isItPrime = true;

            for (int prime : primeList) {
                if (potentialPrime % prime == 0) {
                    isItPrime = false;
                    break; // No need to continue checking if it's already not prime
                }
            }

            if (isItPrime) primeList.add(potentialPrime);
        }

        return primeList;
    }

    Set<KindleDatabase> kindleKeys(Set<String> files) {
        // If files is null, retrieve the Kindle info files
        if (files == null || files.isEmpty()) files = getKindleInfoFiles();

        Set<KindleDatabase> keys = new LinkedHashSet<>();

        // Process each file
        for (String file : files) {
            Map<String, byte[]> key = getDbFromFile(file);

            if (key != null && !key.isEmpty()) {
                // Convert all values to hex strings
                KindleDatabase nKey = new KindleDatabase();

                for (Map.Entry<String, byte[]> entry : key.entrySet()) {
                    // Convert the value to a hexadecimal string
                    String keyName = entry.getKey();
                    String hexValue = byteArrayToHexString(entry.getValue());
                    nKey.put(keyName, hexValue);
                }

                keys.add(nKey);
            }
        }

        return keys;
    }

    public boolean getKey(String outpath) {
        return getKey(outpath, null);
    }

    public boolean getKey(String outpath, Set<String> files) {
        // Check if files list is null, and initialize it if necessary
        if (files == null) files = Set.of();  // Creates an empty list

        // Retrieve Kindle keys using the kindleKeys method
        Set<KindleDatabase> keys = kindleKeys(files);

        if (!keys.isEmpty()) {
            File outFile = new File(outpath);

            // Check if the output path is a directory or a file
            if (!outFile.isDirectory()) {
                // If it's not a directory, assume it's a file path
                try {
                    // Write the first key to the specified file
                    keys.iterator().next().writeToFile(outFile);
                    System.out.printf("Saved a key to %s%n", outFile.getAbsolutePath());
                } catch (IOException e) {
                    System.err.printf("Error saving key to file: %s%n", e.getMessage());
                    return false;
                }
            } else {
                // If it's a directory, save each key to a separate file with a unique name
                int keyCount = 0;

                for (KindleDatabase key : keys) {
                    while (true) {
                        keyCount++;
                        String outfile = Paths.get(outpath, String.format("kindlekey%d.k4i", keyCount)).toString();

                        // Check if the file already exists
                        if (!new File(outfile).exists()) {
                            try {
                                key.writeToFile(outfile);
                                System.out.printf("Saved a key to %s%n", outfile);
                            } catch (IOException e) {
                                System.err.printf("Error saving key to file: %s%n", e.getMessage());
                                return false;
                            }

                            break;
                        }
                    }
                }
            }

            return true;
        }

        return false;
    }
}
