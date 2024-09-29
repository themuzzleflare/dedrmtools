/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kfxdedrm;

import cloud.tavitian.dedrmtools.Book;
import cloud.tavitian.dedrmtools.BookFile;
import cloud.tavitian.dedrmtools.Debug;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static cloud.tavitian.dedrmtools.CharMaps.*;
import static cloud.tavitian.dedrmtools.Util.contains;

public final class KFXZipBook extends Book {
    private static final String VERSION = "2.0";

    private final String infile;
    private final KFXDecryptedDictionary decrypted = new KFXDecryptedDictionary();
    private DRMIonVoucher voucher;

    public KFXZipBook(String infile) {
        super();
        System.out.printf("KFXDeDRM v%s.%n", VERSION);
        System.out.println("Removes DRM protection from KFX-ZIP and KFX eBooks.");
        this.infile = infile;
    }

    @Override
    public @NotNull String getBookTitle() {
        BookFile bookFile = new BookFile(infile);
        return bookFile.getRoot();
    }

    @Contract(pure = true)
    @Override
    public @NotNull String getBookType() {
        return "KFX-ZIP";
    }

    @Contract(pure = true)
    @Override
    public @NotNull String getBookExtension() {
        return ".kfx-zip";
    }

    @Override
    public void getFile(String outpath) throws IOException {
        if (decrypted.isEmpty()) Files.copy(Paths.get(infile), Paths.get(outpath));
        else {
            try (KFXZipFile zif = new KFXZipFile(infile);
                 ZipOutputStream zof = new ZipOutputStream(new FileOutputStream(outpath))) {
                Enumeration<? extends ZipEntry> entries = zif.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();

                    byte[] content = decrypted.getOrDefault(entry.getName(), zif.getInputStream(entry).readAllBytes());

                    ZipEntry newEntry = new ZipEntry(entry.getName());

                    zof.putNextEntry(newEntry);
                    zof.write(content);
                    zof.closeEntry();
                }
            }
        }
    }

    @Override
    public void processBook(Set<String> pidSet) throws Exception {
        try (FileInputStream fis = new FileInputStream(infile);
             ZipInputStream zis = new ZipInputStream(fis)) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                byte[] data = zis.readNBytes(8);

                if (!Arrays.equals(data, kfxDrmIonBytes)) continue;

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                os.write(data);
                os.write(zis.readAllBytes());
                data = os.toByteArray();

                if (voucher == null) decryptVoucher(pidSet);

                System.out.printf("Decrypting KFX DRMION: %s%n", entry.getName());

                ByteArrayOutputStream outfile = new ByteArrayOutputStream();

                new DRMIon(new BytesIOInputStream(Arrays.copyOfRange(data, 8, data.length - 8)), voucher).parse(outfile);

                decrypted.put(entry.getName(), outfile.toByteArray());
            }
        }

        if (decrypted.isEmpty()) System.out.println("The .kfx-zip archive does not contain an encrypted DRMION file");
    }

    private void decryptVoucher(Set<String> pidSet) throws Exception {
        String voucherFilename = null;
        byte[] voucherData = null;
        boolean decryptionSucceeded = false;
        DRMIonVoucher decryptedVoucher = null;

        try (FileInputStream fis = new FileInputStream(infile);
             ZipInputStream zis = new ZipInputStream(fis)) {

            boolean foundVoucher = false;
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                byte[] data = zis.readNBytes(4);

                if (!Arrays.equals(data, voucherBytes)) continue;

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                os.write(data);
                os.write(zis.readAllBytes());
                data = os.toByteArray();

                if (contains(data, protectedDataBytes)) {
                    foundVoucher = true;
                    voucherFilename = entry.getName();
                    voucherData = data;
                    break; // Found DRM voucher
                }
            }

            if (!foundVoucher)
                throw new Exception("The .kfx-zip archive contains an encrypted DRMION file without a DRM voucher");
        }

        System.out.printf("Decrypting KFX DRM voucher: %s%n", voucherFilename);

        Debug.printf("PIDs: %s%n", pidSet);

        outerLoop:
        for (String pid : pidSet) {
            for (int[] lengths : new int[][]{{0, 0}, {16, 0}, {16, 40}, {32, 0}, {32, 40}, {40, 0}, {40, 40}}) {
                int dsn_len = lengths[0];
                int secret_len = lengths[1];

                if (pid.length() == dsn_len + secret_len) {
                    // Split the PID into DSN and account secret
                    String dsn = pid.substring(0, dsn_len);
                    String accountSecret = pid.substring(dsn_len);

                    try {
                        Debug.printf("DSN: %s%n", dsn);
                        Debug.printf("Account secret: %s%n", accountSecret);

                        DRMIonVoucher localVoucher = new DRMIonVoucher(new BytesIOInputStream(voucherData), dsn, accountSecret);
                        localVoucher.parse();
                        localVoucher.decryptVoucher();

                        decryptionSucceeded = true;
                        decryptedVoucher = localVoucher;
                        break outerLoop; // Break out of both loops if successful
                    } catch (Exception _) {
                    }
                }
            }
        }

        if (!decryptionSucceeded)
            throw new Exception("Failed to decrypt KFX DRM voucher with any key");

        System.out.println("KFX DRM voucher successfully decrypted");

        String licenceType = decryptedVoucher.getLicenceType();

        if (!"Purchase".equals(licenceType))
            System.out.printf("Warning: This book is licensed as %s. These tools are intended for use on purchased books. Continuing...%n", licenceType);

        voucher = decryptedVoucher;
    }
}
