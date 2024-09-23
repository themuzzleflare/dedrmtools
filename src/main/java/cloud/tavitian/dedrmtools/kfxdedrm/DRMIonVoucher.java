/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kfxdedrm;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cloud.tavitian.dedrmtools.CharMaps.pidv3Bytes;
import static cloud.tavitian.dedrmtools.CryptoUtils.aescbcdecrypt;
import static cloud.tavitian.dedrmtools.CryptoUtils.hmacsha256;
import static cloud.tavitian.dedrmtools.kfxdedrm.IonUtils.*;

final class DRMIonVoucher {
    private final BinaryIonParser envelope;
    private final List<String> lockParams = new ArrayList<>();
    private final byte[] dsn;
    private final byte[] secret;
    private int version;
    private BinaryIonParser voucher;
    private BinaryIonParser drmKey;
    private String licenceType = "Unknown";
    private String encAlgorithm = "";
    private String encTransformation = "";
    private String hashAlgorithm = "";
    private byte[] cipherText;
    private byte[] cipherIv;
    private byte[] secretKey;

    public DRMIonVoucher(BytesIOInputStream voucherenv, String dsn, String secret) {
        this.dsn = dsn.getBytes(StandardCharsets.US_ASCII);
        this.secret = secret.getBytes(StandardCharsets.US_ASCII);
        envelope = new BinaryIonParser(voucherenv);

        addProtTable(envelope);
    }

    @SuppressWarnings("unused")
    public DRMIonVoucher(BytesIOInputStream voucherenv, byte[] dsn, byte[] secret) {
        this.dsn = dsn;
        this.secret = secret;
        envelope = new BinaryIonParser(voucherenv);

        addProtTable(envelope);
    }

    public void decryptVoucher() throws Exception {
        // Step 1: Prepare the shared secret using encryption algorithm, transformation, and hashing algorithm
        ByteArrayOutputStream sharedBuilder = new ByteArrayOutputStream();
        sharedBuilder.write(pidv3Bytes);
        sharedBuilder.write(encAlgorithm.getBytes(StandardCharsets.US_ASCII));
        sharedBuilder.write(encTransformation.getBytes(StandardCharsets.US_ASCII));
        sharedBuilder.write(hashAlgorithm.getBytes(StandardCharsets.US_ASCII));

        // Sort lock parameters to maintain consistency
        Collections.sort(lockParams);

        // Step 2: Add lock parameters to the shared secret
        for (String param : lockParams) {
            switch (param) {
                case "ACCOUNT_SECRET" -> {
                    sharedBuilder.write(param.getBytes(StandardCharsets.US_ASCII));
                    sharedBuilder.write(secret);
                }
                case "CLIENT_ID" -> {
                    sharedBuilder.write(param.getBytes(StandardCharsets.US_ASCII));
                    sharedBuilder.write(dsn);
                }
                default -> throw new Exception(String.format("Unknown lock parameter: %s", param));
            }
        }

        byte[] shared = sharedBuilder.toByteArray();

        // Step 3: Generate multiple possible shared secrets using different obfuscation methods
        byte[][] sharedSecrets = new byte[][]{
                obfuscate(shared, version),
                obfuscate2(shared, version),
                obfuscate3(shared, version),
                processV9708(shared),
                processV1031(shared),
                processV2069(shared),
                processV9041(shared),
                processV3646(shared),
                processV6052(shared),
                processV9479(shared),
                processV9888(shared),
                processV4648(shared),
                processV5683(shared)
        };

        boolean decrypted = false;
        Exception ex = null;

        // Step 4: Attempt to decrypt using each shared secret
        for (byte[] sharedSecret : sharedSecrets) {
            try {
                // Generate the key using HMAC-SHA256
                // Step 1: HMAC-SHA256 to generate the key
                byte[] key = hmacsha256(sharedSecret, pidv3Bytes);
                byte[] decryptedData = aescbcdecrypt(Arrays.copyOfRange(key, 0, 32), Arrays.copyOfRange(cipherIv, 0, 16), cipherText);

                // Parse the decrypted data as a BinaryIonParser
                drmKey = new BinaryIonParser(new BytesIOInputStream(decryptedData));
                addProtTable(drmKey);

                // Verify that the decrypted data is a valid KeySet
                if (!drmKey.hasNext() || drmKey.next() != TID_LIST || !"com.amazon.drm.KeySet@1.0".equals(drmKey.getTypeName()))
                    throw new Exception(String.format("Expected KeySet, got %s", drmKey.getTypeName()));

                decrypted = true;
                System.out.println("Voucher decryption succeeded");
                break;
            } catch (Exception exception) {
                // Print exception for debugging and continue to the next fallback
                System.out.println("Voucher decryption failed, trying next fallback");
                ex = exception;
            }
        }

        // Step 5: Handle decryption failure
        if (!decrypted) throw ex;

        // Step 6: Parse the decrypted key data
        drmKey.stepIn();

        while (drmKey.hasNext()) {
            drmKey.next();

            if (!"com.amazon.drm.SecretKey@1.0".equals(drmKey.getTypeName())) continue;

            drmKey.stepIn();

            while (drmKey.hasNext()) {
                drmKey.next();

                if (null != drmKey.getFieldName()) switch (drmKey.getFieldName()) {
                    case "algorithm" -> {
                        if (!"AES".equals(drmKey.stringValue()))
                            throw new Exception(String.format("Unknown cipher algorithm: %s", drmKey.stringValue()));
                    }
                    case "format" -> {
                        if (!"RAW".equals(drmKey.stringValue()))
                            throw new Exception(String.format("Unknown key format: %s", drmKey.stringValue()));
                    }
                    case "encoded" -> secretKey = drmKey.lobValue();
                    default -> {
                    }
                }
            }

            drmKey.stepOut();
            break;
        }

        drmKey.stepOut();
    }

    public void parse() throws Exception {
        envelope.reset();

        if (!envelope.hasNext()) throw new Exception("Envelope is empty");

        if (envelope.next() != TID_STRUCT || !envelope.getTypeName().startsWith("com.amazon.drm.VoucherEnvelope@"))
            throw new Exception("Unknown type encountered in envelope, expected VoucherEnvelope");

        String envelopeTypeNameStr = envelope.getTypeName().split("@")[1];
        version = Integer.parseInt(envelopeTypeNameStr.substring(0, envelopeTypeNameStr.length() - 2));
        envelope.stepIn();

        while (envelope.hasNext()) {
            envelope.next();

            String field = envelope.getFieldName();

            if ("voucher".equals(field)) {
                voucher = new BinaryIonParser(new BytesIOInputStream(envelope.lobValue()));
                addProtTable(voucher);
                continue;
            } else if (!"strategy".equals(field)) continue;

            if (!"com.amazon.drm.PIDv3@1.0".equals(envelope.getTypeName()))
                throw new Exception(String.format("Unknown strategy: %s", envelope.getTypeName()));

            envelope.stepIn();

            while (envelope.hasNext()) {
                envelope.next();
                field = envelope.getFieldName();

                if (null != field) switch (field) {
                    case "encryption_algorithm" -> encAlgorithm = envelope.stringValue();
                    case "encryption_transformation" -> encTransformation = envelope.stringValue();
                    case "hashing_algorithm" -> hashAlgorithm = envelope.stringValue();
                    case "lock_parameters" -> {
                        envelope.stepIn();
                        while (envelope.hasNext()) {
                            if (envelope.next() != TID_STRING)
                                throw new Exception("Expected string list for lock_parameters");

                            lockParams.add(envelope.stringValue());
                        }
                        envelope.stepOut();
                    }
                    default -> {
                    }
                }
            }

            envelope.stepOut();
        }

        parseVoucher();
    }

    private void parseVoucher() throws Exception {
        if (!voucher.hasNext()) throw new Exception("Voucher is empty");

        if (voucher.next() != TID_STRUCT || !"com.amazon.drm.Voucher@1.0".equals(voucher.getTypeName()))
            throw new Exception("Unknown type, expected Voucher");

        voucher.stepIn();

        while (voucher.hasNext()) {
            voucher.next();

            if (null != voucher.getFieldName()) switch (voucher.getFieldName()) {
                case "cipher_iv" -> cipherIv = voucher.lobValue();
                case "cipher_text" -> cipherText = voucher.lobValue();
                case "license" -> {
                    if (!"com.amazon.drm.License@1.0".equals(voucher.getTypeName()))
                        throw new Exception(String.format("Unknown license: %s", voucher.getTypeName()));
                    voucher.stepIn();
                    while (voucher.hasNext()) {
                        voucher.next();

                        if ("license_type".equals(voucher.getFieldName())) licenceType = voucher.stringValue();
                    }
                    voucher.stepOut();
                }
                default -> {
                }
            }
        }
    }

    public String getLicenceType() {
        return licenceType;
    }

    public byte[] getSecretKey() {
        return secretKey;
    }
}
