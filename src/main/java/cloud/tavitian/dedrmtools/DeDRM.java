/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import cloud.tavitian.dedrmtools.kfxdedrm.KFXZipBook;
import cloud.tavitian.dedrmtools.kindlekeys.KDatabaseRecord;
import cloud.tavitian.dedrmtools.kindlekeys.KindleDatabase;
import cloud.tavitian.dedrmtools.kindlekeys.KindleKey;
import cloud.tavitian.dedrmtools.mobidedrm.MobiBook;
import cloud.tavitian.dedrmtools.topazextract.TopazBook;
import org.apache.commons.text.StringEscapeUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cloud.tavitian.dedrmtools.CharMaps.*;
import static cloud.tavitian.dedrmtools.Util.*;
import static cloud.tavitian.dedrmtools.kindlekeys.KindlePID.getPidSet;

public final class DeDRM {
    private static final String VERSION = "2.0";

    private DeDRM() {
    }

    private static String cleanupName(String name) {
        // Substitute filename unfriendly characters
        name = name.replace("<", "[")
                .replace(">", "]")
                .replace(" : ", " – ")
                .replace(": ", " – ")
                .replace(":", "—")
                .replace("/", "_")
                .replace("\\", "_")
                .replace("|", "_")
                .replace("\"", "'")
                .replace("*", "_")
                .replace("?", "");

        // Whitespace to single space, delete leading and trailing white space
        name = name.replaceAll("\\s+", " ").trim();

        // Delete control characters
        name = name.chars()
                .filter(c -> c >= 32)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        // Delete non-ASCII characters
        name = name.chars()
                .filter(c -> c <= 126)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        // Remove leading dots
        while (name.startsWith(".")) name = name.substring(1);

        // Remove trailing dots
        while (name.endsWith(".")) name = name.substring(0, name.length() - 1);

        // If the name is empty, assign a default value
        if (name.isEmpty()) name = "DecryptedBook";

        return name;
    }

    private static String unescape(String text) {
        Pattern pattern = Pattern.compile("&#?\\w+;");
        Matcher matcher = pattern.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String match = matcher.group();
            String replacement;

            if (match.startsWith("&#")) {
                if (match.startsWith("&#x"))
                    replacement = String.valueOf((char) Integer.parseInt(match.substring(3, match.length() - 1), 16));
                else replacement = String.valueOf((char) Integer.parseInt(match.substring(2, match.length() - 1)));
            } else replacement = StringEscapeUtils.unescapeHtml4(match);

            matcher.appendReplacement(result, replacement);
        }

        matcher.appendTail(result);

        return result.toString();
    }

    private static String calculateOutfileName(String filename, String booktitle) {
        String origFnRoot = new BookFile(filename).getRoot();

        String outfilename;

        if (Pattern.matches("^B[A-Z0-9]{9}(_EBOK|_EBSP|_sample)?$", origFnRoot) ||
                Pattern.matches("^[0-9A-F-]{36}$", origFnRoot)) {
            // Kindle for PC/Mac/Android/Fire/iOS
            String cleanTitle = cleanupName(booktitle);
            outfilename = String.format("%s_%s", origFnRoot, cleanTitle);
        } else outfilename = origFnRoot; // eInk Kindle, which already uses a reasonable name

        // Avoid excessively long file names
        if (outfilename.length() > 150)
            outfilename = String.format("%s--%s", outfilename.substring(0, 99), outfilename.substring(outfilename.length() - 49));

        outfilename += "_nodrmjava";

        return outfilename;
    }

    private static Set<KDatabaseRecord> loadKDatabaseRecords(Set<String> kDatabaseFiles) {
        Set<KDatabaseRecord> kDatabaseRecords = new LinkedHashSet<>();

        if (practicalIsEmpty(kDatabaseFiles)) return kDatabaseRecords;

        for (String kDatabaseFile : kDatabaseFiles) {
            try {
                KindleDatabase kindleDatabase = new KindleDatabase(kDatabaseFile);
                KDatabaseRecord kDatabaseRecord = new KDatabaseRecord(kDatabaseFile, kindleDatabase);
                kDatabaseRecords.add(kDatabaseRecord);
            } catch (IOException e) {
                System.err.printf("Error getting database from file %s: %s%n", kDatabaseFile, e.getMessage());
            }
        }

        return kDatabaseRecords;
    }

    @SuppressWarnings("unused")
    private static void decryptionRoutine(String infile, String outdir, Set<KDatabaseRecord> kDatabaseRecords, Set<String> serials, Set<String> pids) throws Exception {
        decryptionRoutine(infile, outdir, kDatabaseRecords, serials, pids, System.currentTimeMillis());
    }

    private static void decryptionRoutine(String infile, String outdir, Set<KDatabaseRecord> kDatabaseRecords, Set<String> serials, Set<String> pids, long startTime) throws Exception {
        try {
            Book mb = getDecryptedBook(infile, kDatabaseRecords, serials, pids);

            String outfilename = calculateOutfileName(infile, mb.getBookTitle());
            String outpath = Path.of(outdir, outfilename + mb.getBookExtension()).toString();

            mb.getFile(outpath);

            System.out.printf("Saved decrypted book %s after %.1f seconds%n", outfilename, (System.currentTimeMillis() - startTime) / 1000.0);

            mb.cleanup();
        } catch (Exception e) {
            System.err.printf("Error decrypting book after %.1f seconds: %s%n", (System.currentTimeMillis() - startTime) / 1000.0, e.getMessage());
            throw e;
        }
    }

    private static Book getDecryptedBook(String infile, Set<KDatabaseRecord> kDatabaseRecords, Set<String> serials, Set<String> pids) throws Exception {
        return getDecryptedBook(infile, kDatabaseRecords, serials, pids, System.currentTimeMillis());
    }

    private static Book getDecryptedBook(String infile, Set<KDatabaseRecord> kDatabaseRecords, Set<String> serials, Set<String> pids, long startTime) throws Exception {
        Book book;

        boolean mobi = true;

        byte[] magic8;
        try (FileInputStream inputStream = new FileInputStream(infile)) {
            magic8 = inputStream.readNBytes(8);
        }

        byte[] magic3 = Arrays.copyOfRange(magic8, 0, 3);
        byte[] magic4 = Arrays.copyOfRange(magic8, 0, 4);

        if (Arrays.equals(magic8, kfxDrmIonBytes))
            throw new Exception("The .kfx DRMION file cannot be decrypted by itself. A .kfx-zip archive containing a DRM voucher is required.");

        if (Arrays.equals(magic3, topazBytes)) mobi = false;

        if (Arrays.equals(magic4, pkBytes)) book = new KFXZipBook(infile);
        else if (mobi) book = new MobiBook(infile);
        else book = new TopazBook(infile);

        String bookname = unescape(book.getBookTitle());
        String booktype = book.getBookType();

        System.out.printf("Decrypting %s eBook: %s%n", booktype, bookname);

        Set<String> totalPids = new LinkedHashSet<>(pids);

        PIDMetaInfo pidMetaInfo = book.getPidMetaInfo();

        byte[] rec209 = pidMetaInfo.rec209();
        byte[] token = pidMetaInfo.token();

        totalPids.addAll(getPidSet(rec209, token, serials, kDatabaseRecords));

        System.out.printf("Found %d keys to try after %.1f seconds%n", totalPids.size(), (System.currentTimeMillis() - startTime) / 1000.0);

        try {
            book.processBook(totalPids);
        } catch (Exception e) {
            book.cleanup(); // for Topaz books
            throw e;
        }

        System.out.printf("Decryption succeeded after %.1f seconds%n", (System.currentTimeMillis() - startTime) / 1000.0);

        return book;
    }

    public static boolean generateKeyfile(String outpath) {
        try {
            KindleKey instance = KindleKey.getInstance();
            return instance.getKey(outpath);
        } catch (Exception e) {
            System.err.printf("Error generating keyfile: %s%n", e.getMessage());
            return false;
        }
    }

    public static void generateKeyfileThrowing(String outpath) throws Exception {
        KindleKey instance = KindleKey.getInstance();
        instance.getKeyThrowing(outpath);
    }

    public static void decryptBook(String infile, String outdir, Set<String> kDatabaseFiles, Set<String> serials, Set<String> pids) {
        kDatabaseFiles = sanitiseSet(kDatabaseFiles);
        serials = sanitiseSet(serials);
        pids = sanitiseSet(pids);

        long startTime = System.currentTimeMillis();

        System.out.printf("K4MobiDeDrm v%s.%n%s.%n", VERSION, COPYRIGHT);
        System.out.println("Removes DRM protection from Mobipocket, Amazon KF8, Amazon Print Replica, and Amazon Topaz eBooks.");

        Set<KDatabaseRecord> kDatabaseRecords = loadKDatabaseRecords(kDatabaseFiles);

        try {
            decryptionRoutine(infile, outdir, kDatabaseRecords, serials, pids, startTime);
        } catch (Exception _) {
        }
    }

    public static void decryptBookThrowing(String infile, String outdir, Set<String> kDatabaseFiles, Set<String> serials, Set<String> pids) throws Exception {
        kDatabaseFiles = sanitiseSet(kDatabaseFiles);
        serials = sanitiseSet(serials);
        pids = sanitiseSet(pids);

        long startTime = System.currentTimeMillis();

        System.out.printf("K4MobiDeDrm v%s.%n%s.%n", VERSION, COPYRIGHT);
        System.out.println("Removes DRM protection from Mobipocket, Amazon KF8, Amazon Print Replica, and Amazon Topaz eBooks.");

        Set<KDatabaseRecord> kDatabaseRecords = loadKDatabaseRecords(kDatabaseFiles);

        decryptionRoutine(infile, outdir, kDatabaseRecords, serials, pids, startTime);
    }

    public static void decryptBooks(Set<String> infiles, String outdir, Set<String> kDatabaseFiles, Set<String> serials, Set<String> pids) {
        long startTime = System.currentTimeMillis();

        kDatabaseFiles = sanitiseSet(kDatabaseFiles);
        serials = sanitiseSet(serials);
        pids = sanitiseSet(pids);

        System.out.printf("K4MobiDeDrm v%s.%n%s.%n", VERSION, COPYRIGHT);
        System.out.println("Removes DRM protection from Mobipocket, Amazon KF8, Amazon Print Replica, and Amazon Topaz eBooks.");

        Set<KDatabaseRecord> kDatabaseRecords = loadKDatabaseRecords(kDatabaseFiles);

        int decryptionCounter = 0;

        for (String infile : infiles) {
            try {
                decryptionRoutine(infile, outdir, kDatabaseRecords, serials, pids, startTime);
                decryptionCounter++;
            } catch (Exception _) {
            } finally {
                System.out.println();
            }
        }

        System.out.printf("Decryption of %d/%d books completed after %.1f seconds%n", decryptionCounter, infiles.size(), (System.currentTimeMillis() - startTime) / 1000.0);
    }

    @SuppressWarnings("unused")
    public static void decryptBook(String infile, String outdir, String kdatabases, String serials, String pids) {
        decryptBook(infile, outdir, commaSeparatedStringToSanitisedSet(kdatabases), commaSeparatedStringToSanitisedSet(serials), commaSeparatedStringToSanitisedSet(pids));
    }

    @SuppressWarnings("unused")
    public static void decryptBookWithSerial(String infile, String outdir, Set<String> serials) {
        decryptBook(infile, outdir, Collections.emptySet(), serials, Collections.emptySet());
    }

    @SuppressWarnings("unused")
    public static void decryptBookWithSerial(String infile, String outdir, String serials) {
        decryptBook(infile, outdir, Collections.emptySet(), commaSeparatedStringToSanitisedSet(serials), Collections.emptySet());
    }

    @SuppressWarnings("unused")
    public static void decryptBookWithPid(String infile, String outdir, Set<String> pids) {
        decryptBook(infile, outdir, Collections.emptySet(), Collections.emptySet(), pids);
    }

    @SuppressWarnings("unused")
    public static void decryptBookWithPid(String infile, String outdir, String pids) {
        decryptBook(infile, outdir, Collections.emptySet(), Collections.emptySet(), commaSeparatedStringToSanitisedSet(pids));
    }

    @SuppressWarnings("unused")
    public static void decryptBookWithKDatabase(String infile, String outdir, Set<String> kdatabases) {
        decryptBook(infile, outdir, kdatabases, Collections.emptySet(), Collections.emptySet());
    }

    @SuppressWarnings("unused")
    public static void decryptBookWithKDatabase(String infile, String outdir, String kdatabases) {
        decryptBook(infile, outdir, commaSeparatedStringToSanitisedSet(kdatabases), Collections.emptySet(), Collections.emptySet());
    }

    @SuppressWarnings("unused")
    public static void decryptBookWithKDatabaseAndSerial(String infile, String outdir, Set<String> kdatabases, Set<String> serials) {
        decryptBook(infile, outdir, kdatabases, serials, Collections.emptySet());
    }

    public static void decryptBookWithKDatabaseAndSerial(String infile, String outdir, String kdatabases, String serials) {
        decryptBook(infile, outdir, commaSeparatedStringToSanitisedSet(kdatabases), commaSeparatedStringToSanitisedSet(serials), Collections.emptySet());
    }

    public static void decryptBookWithKDatabaseAndSerialThrowing(String infile, String outdir, String kdatabases, String serials) throws Exception {
        decryptBookThrowing(infile, outdir, commaSeparatedStringToSanitisedSet(kdatabases), commaSeparatedStringToSanitisedSet(serials), Collections.emptySet());
    }

    @SuppressWarnings("unused")
    public static void decryptBooksWithKDatabaseAndSerial(Set<String> infiles, String outdir, String kdatabases, String serials) {
        decryptBooks(infiles, outdir, commaSeparatedStringToSanitisedSet(kdatabases), commaSeparatedStringToSanitisedSet(serials), Collections.emptySet());
    }
}
