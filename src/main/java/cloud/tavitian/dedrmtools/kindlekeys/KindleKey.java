/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import cloud.tavitian.dedrmtools.Debug;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

import static cloud.tavitian.dedrmtools.CryptoUtils.aescbcdecrypt;
import static cloud.tavitian.dedrmtools.CryptoUtils.pbkdf2hmacsha1;
import static cloud.tavitian.dedrmtools.Util.byteArrayToHexString;
import static cloud.tavitian.dedrmtools.Util.formatByteArray;

public abstract class KindleKey implements KindleKeyManager {
    private static final Gson gson = new Gson();
    private static final String osName = System.getProperty("os.name").toLowerCase();

    public static KindleKey getInstance() {
        if (osName.startsWith("win")) return new KindleKeyWindows();
        else if (osName.startsWith("mac") || osName.startsWith("darwin")) return new KindleKeyMacOS();
        else throw new UnsupportedOperationException("Unsupported operating system: " + osName);
    }

    // Method to decrypt the encrypted data using a derived key and IV
    public static byte[] unprotectHeaderData(byte[] encryptedData) throws Exception {
        Debug.println("Encrypted data: " + formatByteArray(encryptedData));

        char[] passwdData = "header_key_data".toCharArray(); // Password equivalent
        byte[] salt = "HEADER.2011".getBytes(StandardCharsets.US_ASCII); // Salt equivalent

        int iterationCount = 128; // PBKDF2 iteration count
        int keyLength = 256; // Desired key length in bits

        byte[] keyIv = pbkdf2hmacsha1(passwdData, salt, iterationCount, keyLength);

        Debug.println("Derived key: " + formatByteArray(keyIv));

        // Extract the AES key and IV from the derived key material
        byte[] key = Arrays.copyOfRange(keyIv, 0, 32); // First 32 bytes for AES key
        byte[] iv = Arrays.copyOfRange(keyIv, 32, 48); // Next 16 bytes for IV

        // Decrypt the data
        byte[] decryptedData = aescbcdecrypt(key, iv, encryptedData);

        Debug.println("Decrypted data: " + formatByteArray(decryptedData));

        return decryptedData;
    }

    static List<Integer> primes(int n) {
        // Return a list of prime integers smaller than or equal to n
        // :param n: int
        // :return: List<Integer>

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

    KindleKeys kindleKeys(List<String> files) {
        // If files is null, retrieve the Kindle info files
        if (files == null || files.isEmpty()) files = getKindleInfoFiles();

        KindleKeys keys = new KindleKeys();

        // Process each file
        for (String file : files) {
            KindleDatabase<byte[]> key = getDbFromFile(file);

            if (key != null && !key.isEmpty()) {
                // Convert all values to hex strings
                KindleDatabaseStringValues nKey = new KindleDatabaseStringValues();

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

    public boolean getKey(String outpath, List<String> files) {
        // Check if files list is null, and initialize it if necessary
        if (files == null) files = List.of();  // Creates an empty list

        // Retrieve Kindle keys using the kindleKeys method
        KindleKeys keys = kindleKeys(files);

        if (!keys.isEmpty()) {
            File outFile = new File(outpath);

            // Check if the output path is a directory or a file
            if (!outFile.isDirectory()) {
                // If it's not a directory, assume it's a file path
                try (FileWriter keyfileOut = new FileWriter(outFile);
                     JsonWriter jsonWriter = new JsonWriter(keyfileOut)) {
                    // Write the first key to the specified file
                    gson.toJson(keys.getFirst(), KindleDatabase.class, jsonWriter);
                    System.out.printf("Saved a key to %s%n", outFile.getAbsolutePath());
                } catch (IOException e) {
                    System.err.printf("Error saving key to file: %s%n", e.getMessage());
                    return false;
                }
            } else {
                // If it's a directory, save each key to a separate file with a unique name
                int keyCount = 0;

                for (KindleDatabase<String> key : keys) {
                    while (true) {
                        keyCount++;
                        String outfile = Paths.get(outpath, String.format("kindlekey%d.k4i", keyCount)).toString();

                        // Check if the file already exists
                        if (!new File(outfile).exists()) {
                            try (FileWriter keyfileOut = new FileWriter(outfile);
                                 JsonWriter jsonWriter = new JsonWriter(keyfileOut)) {
                                gson.toJson(key, KindleDatabase.class, jsonWriter);
                                System.out.printf("Saved a key to %s%n", outfile);
                            } catch (IOException e) {
                                e.printStackTrace();
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
