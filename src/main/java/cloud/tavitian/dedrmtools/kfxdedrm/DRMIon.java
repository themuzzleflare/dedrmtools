/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kfxdedrm;

import org.tukaani.xz.LZMAInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import static cloud.tavitian.dedrmtools.CryptoUtils.aescbcdecrypt;
import static cloud.tavitian.dedrmtools.kfxdedrm.IonUtils.*;

final class DRMIon {
    private final BinaryIonParser ion;
    private final DRMIonVoucher voucher;
    private String voucherName = "";
    private byte[] key;

    public DRMIon(BytesIOInputStream ionStream, DRMIonVoucher voucher) {
        ion = new BinaryIonParser(ionStream);
        addProtTable(ion);
        this.voucher = voucher;
    }

    private static void decompress(byte[] compressedData, OutputStream outpages) throws IOException {
        try (LZMAInputStream lzmaInputStream = new LZMAInputStream(new ByteArrayInputStream(compressedData))) {
            byte[] segmentBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = lzmaInputStream.read(segmentBuffer)) != -1) outpages.write(segmentBuffer, 0, bytesRead);
        }
    }

    public void parse(ByteArrayOutputStream outpages) throws Exception {
        ion.reset();

        if (!ion.hasNext()) throw new Exception("DRMION envelope is empty");

        if (ion.next() != TID_SYMBOL || !"doctype".equals(ion.getTypeName()))
            throw new Exception("Expected doctype symbol");

        if (ion.next() != TID_LIST || (!"com.amazon.drm.Envelope@1.0".equals(ion.getTypeName()) &&
                !"com.amazon.drm.Envelope@2.0".equals(ion.getTypeName())))
            throw new Exception(String.format("Unknown type encountered in DRMION envelope, expected Envelope, got %s", ion.getTypeName()));

        while (true) {
            if ("enddoc".equals(ion.getTypeName())) break;

            ion.stepIn();

            while (ion.hasNext()) {
                ion.next();

                if ("com.amazon.drm.EnvelopeMetadata@1.0".equals(ion.getTypeName()) || "com.amazon.drm.EnvelopeMetadata@2.0".equals(ion.getTypeName())) {
                    ion.stepIn();

                    while (ion.hasNext()) {
                        ion.next();

                        if (!"encryption_voucher".equals(ion.getFieldName())) continue;

                        if (voucherName.isEmpty()) {
                            voucherName = ion.stringValue();
                            key = voucher.getSecretKey();

                            if (key == null) throw new Exception("Unable to obtain secret key from voucher");
                        } else {
                            if (!voucherName.equals(ion.stringValue()))
                                throw new Exception("Unexpected: Different vouchers required for same file?");
                        }
                    }

                    ion.stepOut();
                } else if ("com.amazon.drm.EncryptedPage@1.0".equals(ion.getTypeName()) || "com.amazon.drm.EncryptedPage@2.0".equals(ion.getTypeName())) {
                    boolean decompress = false;
                    boolean decrypt = true;
                    byte[] ct = null;
                    byte[] civ = null;

                    ion.stepIn();

                    while (ion.hasNext()) {
                        ion.next();

                        if ("com.amazon.drm.Compressed@1.0".equals(ion.getTypeName())) decompress = true;

                        if ("cipher_text".equals(ion.getFieldName())) ct = ion.lobValue();
                        else if ("cipher_iv".equals(ion.getFieldName())) civ = ion.lobValue();
                    }

                    if (ct != null && civ != null) processPage(ct, civ, outpages, decompress, decrypt);

                    ion.stepOut();
                } else if ("com.amazon.drm.PlainText@1.0".equals(ion.getTypeName()) || "com.amazon.drm.PlainText@2.0".equals(ion.getTypeName())) {
                    boolean decompress = false;
                    boolean decrypt = false;
                    byte[] plaintext = null;

                    ion.stepIn();

                    while (ion.hasNext()) {
                        ion.next();

                        if ("com.amazon.drm.Compressed@1.0".equals(ion.getTypeName())) decompress = true;

                        if ("data".equals(ion.getFieldName())) plaintext = ion.lobValue();
                    }

                    if (plaintext != null) processPage(plaintext, null, outpages, decompress, decrypt);

                    ion.stepOut();
                }
            }

            ion.stepOut();

            if (!ion.hasNext()) break;

            ion.next();
        }
    }

    private void processPage(byte[] ct, byte[] civ, ByteArrayOutputStream outpages, boolean decompress, boolean decrypt) throws Exception {
        byte[] msg;

        if (decrypt) {
            byte[] keyRange = Arrays.copyOfRange(key, 0, 16);
            byte[] civRange = Arrays.copyOfRange(civ, 0, 16);

            msg = aescbcdecrypt(keyRange, civRange, ct);
        } else msg = ct;

        if (!decompress) {
            outpages.write(msg);
            return;
        }

        if (msg[0] != 0) throw new Exception("LZMA UseFilter not supported");

        decompress(Arrays.copyOfRange(msg, 1, msg.length), outpages);
    }
}
