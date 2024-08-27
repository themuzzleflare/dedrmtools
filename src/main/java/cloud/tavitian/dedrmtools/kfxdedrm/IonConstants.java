/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kfxdedrm;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cloud.tavitian.dedrmtools.Util.sha256;
import static cloud.tavitian.dedrmtools.Util.toByteArray;
import static cloud.tavitian.dedrmtools.kfxdedrm.kfxtables.KFXTables.*;

public final class IonConstants {
    public static final int TID_NULL = 0;
    public static final int TID_BOOLEAN = 1;
    public static final int TID_POSINT = 2;
    public static final int TID_NEGINT = 3;
    public static final int TID_FLOAT = 4;
    public static final int TID_DECIMAL = 5;
    public static final int TID_TIMESTAMP = 6;
    public static final int TID_SYMBOL = 7;
    public static final int TID_STRING = 8;
    public static final int TID_CLOB = 9;
    public static final int TID_BLOB = 0xA;
    public static final int TID_LIST = 0xB;
    public static final int TID_SEXP = 0xC;
    public static final int TID_STRUCT = 0xD;
    public static final int TID_TYPEDECL = 0xE;
    public static final int TID_UNUSED = 0xF;

    // Symbol IDs (SID)
    public static final int SID_UNKNOWN = -1;
    public static final int SID_ION = 1;
    public static final int SID_ION_1_0 = 2;
    public static final int SID_ION_SYMBOL_TABLE = 3;
    public static final int SID_NAME = 4;
    public static final int SID_VERSION = 5;
    public static final int SID_IMPORTS = 6;
    public static final int SID_SYMBOLS = 7;
    public static final int SID_MAX_ID = 8;
    public static final int SID_ION_SHARED_SYMBOL_TABLE = 9;
    public static final int SID_ION_1_0_MAX = 10;

    // Length Indicators
    public static final int LEN_IS_VAR_LEN = 0xE;
    public static final int LEN_IS_NULL = 0xF;

    // Version Marker
    // python: VERSION_MARKER = [b"\x01", b"\x00", b"\xEA"]
    public static final byte[][] VERSION_MARKER = new byte[][]{new byte[]{0x01}, new byte[]{0x00}, new byte[]{(byte) 0xEA}};

    public static final List<String> SYM_NAMES = new ArrayList<>(Arrays.asList(
            "com.amazon.drm.Envelope@1.0",
            "com.amazon.drm.EnvelopeMetadata@1.0", "size", "page_size",
            "encryption_key", "encryption_transformation",
            "encryption_voucher", "signing_key", "signing_algorithm",
            "signing_voucher", "com.amazon.drm.EncryptedPage@1.0",
            "cipher_text", "cipher_iv", "com.amazon.drm.Signature@1.0",
            "data", "com.amazon.drm.EnvelopeIndexTable@1.0", "length",
            "offset", "algorithm", "encoded", "encryption_algorithm",
            "hashing_algorithm", "expires", "format", "id",
            "lock_parameters", "strategy", "com.amazon.drm.Key@1.0",
            "com.amazon.drm.KeySet@1.0", "com.amazon.drm.PIDv3@1.0",
            "com.amazon.drm.PlainTextPage@1.0",
            "com.amazon.drm.PlainText@1.0", "com.amazon.drm.PrivateKey@1.0",
            "com.amazon.drm.PublicKey@1.0", "com.amazon.drm.SecretKey@1.0",
            "com.amazon.drm.Voucher@1.0", "public_key", "private_key",
            "com.amazon.drm.KeyPair@1.0", "com.amazon.drm.ProtectedData@1.0",
            "doctype", "com.amazon.drm.EnvelopeIndexTableOffset@1.0",
            "enddoc", "license_type", "license", "watermark", "key", "value",
            "com.amazon.drm.License@1.0", "category", "metadata",
            "categorized_metadata", "com.amazon.drm.CategorizedMetadata@1.0",
            "com.amazon.drm.VoucherEnvelope@1.0", "mac", "voucher",
            "com.amazon.drm.ProtectedData@2.0",
            "com.amazon.drm.Envelope@2.0",
            "com.amazon.drm.EnvelopeMetadata@2.0",
            "com.amazon.drm.EncryptedPage@2.0",
            "com.amazon.drm.PlainText@2.0", "compression_algorithm",
            "com.amazon.drm.Compressed@1.0", "page_index_table"
    )) {{
        // Add the range-generated entries
        for (int n : new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 9708, 1031, 2069, 9041, 3646, 6052, 9479, 9888, 4648, 5683}) {
            add(String.format("com.amazon.drm.VoucherEnvelope@%d.0", n));
        }
    }};

    // every VoucherEnvelope version has a corresponding "word" and magic number, used in obfuscating the shared secret
    // 4-digit versions use their own obfuscation/scramble. It does not seem to depend on the "word" and number
    public static final ObfuscationTable OBFUSCATION_TABLE = new ObfuscationTable() {{
        put("V1", new ObfuscationValue(0x00, null));
        put("V2", new ObfuscationValue(0x05, "Antidisestablishmentarianism".getBytes(StandardCharsets.US_ASCII)));
        put("V3", new ObfuscationValue(0x08, "Floccinaucinihilipilification".getBytes(StandardCharsets.US_ASCII)));
        put("V4", new ObfuscationValue(0x07, new byte[]{0x3e, 0x14, 0x0c, 0x12, 0x10, 0x2d, 0x13, 0x26, 0x18, 0x55, 0x1d, 0x05, 0x52, 0x6c, 0x74, 0x03, 0x21, 0x19, 0x1b, 0x13, 0x04, 0x5d, 0x59, 0x19, 0x2c, 0x09, 0x1b}));
        put("V5", new ObfuscationValue(0x06, new byte[]{0x7e, 0x18, 0x7e, 0x16, 0x4a, 0x5c, 0x18, 0x10, 0x05, 0x0b, 0x07, 0x09, 0x0c, 0x5a, 0x0d, 0x7c, 0x1c, 0x15, 0x1d, 0x11, 0x3e, 0x2c, 0x1b, 0x0e, 0x03, 0x22, 0x34, 0x1b, 0x01}));
        put("V6", new ObfuscationValue(0x09, new byte[]{0x33, 0x68, 0x05, 0x35, 0x03, 0x5b, 0x5e, 0x3e, 0x19, 0x1c, 0x08, 0x1b, 0x0d, 0x74, 0x6d, 0x34, 0x02, 0x52, 0x70, 0x0c, 0x16, 0x42, 0x0a}));
        put("V7", new ObfuscationValue(0x05, new byte[]{0x10, 0x1b, 0x4a, 0x18, 0x0a, 0x68, 0x21, 0x10, 0x22, 0x03, 0x3e, 0x5a, 0x27, 0x0d, 0x01, 0x5d, 0x57, 0x06, 0x1c, 0x1e, 0x3f, 0x0f, 0x13}));
        put("V8", new ObfuscationValue(0x09, new byte[]{0x4b, 0x0c, 0x36, 0x1d, 0x1a, 0x17, 0x70, 0x4f, 0x7d, 0x52, 0x6b, 0x1d, 0x27, 0x77, 0x31, 0x5e, 0x1f, 0x24, 0x1c, 0x7b, 0x43, 0x02, 0x51, 0x06, 0x1d, 0x60}));
        put("V9", new ObfuscationValue(0x05, new byte[]{0x58, 0x2e, 0x0e, 0x57, 0x1c, 0x2a, 0x4b, 0x12, 0x12, 0x09, 0x0a, 0x0a, 0x17, 0x57, 0x78, 0x01, 0x02, 0x59, 0x66, 0x0f, 0x18, 0x1b, 0x56, 0x58, 0x50, 0x69, 0x01}));
        put("V10", new ObfuscationValue(0x07, new byte[]{0x7a, 0x33, 0x0a, 0x03, 0x39, 0x12, 0x13, 0x60, 0x06, 0x3d, 0x76, 0x3b, 0x02, 0x4d, 0x54, 0x4b, 0x1e, 0x25, 0x7d, 0x4c, 0x1c, 0x1f, 0x15, 0x0c, 0x11, 0x02, 0x0c, 0x0a, 0x38, 0x17, 0x70}));
        put("V11", new ObfuscationValue(0x05, new byte[]{0x4c, 0x3d, 0x0a, 0x68, 0x56, 0x6d, 0x07, 0x67, 0x6f, 0x0a, 0x36, 0x14, 0x06, 0x16, 0x4c, 0x0d, 0x02, 0x0b, 0x0c, 0x1b, 0x04, 0x23, 0x70, 0x09}));
        put("V12", new ObfuscationValue(0x06, new byte[]{0x3b, 0x6e, 0x1d, 0x0d, 0x6c, 0x13, 0x1c, 0x13, 0x16, 0x70, 0x14, 0x07, 0x55, 0x0c, 0x1f, 0x19, 0x77, 0x16, 0x16, 0x1d, 0x35, 0x54}));
        put("V13", new ObfuscationValue(0x07, new byte[]{0x49, 0x05, 0x09, 0x08, 0x03, 0x72, 0x29, 0x01, 0x24, 0x4e, 0x0f, 0x72, 0x33, 0x6e, 0x0b, 0x30, 0x36, 0x32, 0x44, 0x0f, 0x13}));
        put("V14", new ObfuscationValue(0x05, new byte[]{0x03, 0x02, 0x1c, 0x39, 0x19, 0x15, 0x15, 0x71, 0x10, 0x35, 0x37, 0x08, 0x16, 0x0c, 0x46, 0x1b, 0x2e, 0x46, 0x77, 0x01, 0x12, 0x03, 0x13, 0x02, 0x17, 0x53, 0x27, 0x68, 0x6b, 0x36}));
        put("V15", new ObfuscationValue(0x0A, new byte[]{0x26, 0x2c, 0x34, 0x42, 0x1d, 0x63, 0x49, 0x0b, 0x55, 0x03, 0x49, 0x07, 0x04, 0x1c, 0x09, 0x05, 0x63, 0x07, 0x25, 0x77, 0x73, 0x0c, 0x6a, 0x09, 0x1a, 0x08, 0x0f}));
        put("V16", new ObfuscationValue(0x0A, new byte[]{0x06, 0x18, 0x60, 0x68, 0x3b, 0x3e, 0x3c, 0x06, 0x50, 0x71, 0x52, 0x02, 0x5a, 0x63, 0x03, 0x34, 0x0a, 0x16, 0x1e, 0x18, 0x06, 0x23, 0x65}));
        put("V17", new ObfuscationValue(0x07, new byte[]{0x79, 0x0d, 0x12, 0x08, 0x66, 0x77, 0x2e, 0x5b, 0x02, 0x09, 0x0a, 0x13, 0x11, 0x0c, 0x11, 0x62, 0x1e, 0x38, 0x4c, 0x10, 0x28, 0x13, 0x3c, 0x4a, 0x6e, 0x36, 0x63, 0x0f}));
        put("V18", new ObfuscationValue(0x07, new byte[]{0x49, 0x0b, 0x0e, 0x3b, 0x19, 0x1a, 0x49, 0x61, 0x10, 0x73, 0x19, 0x67, 0x5c, 0x1b, 0x11, 0x21, 0x18, 0x79, 0x66, 0x0f, 0x09, 0x1d, 0x37, 0x5b, 0x53, 0x70, 0x03}));
        put("V19", new ObfuscationValue(0x05, new byte[]{0x0a, 0x36, 0x3e, 0x29, 0x4e, 0x02, 0x18, 0x38, 0x01, 0x36, 0x73, 0x13, 0x14, 0x1b, 0x16, 0x6a, 0x65, 0x4e, 0x0a, 0x14, 0x36, 0x04, 0x18, 0x1c, 0x0c, 0x19, 0x1f, 0x2c, 0x02, 0x5d}));
        put("V20", new ObfuscationValue(0x08, new byte[]{0x5f, 0x0d, 0x01, 0x12, 0x5d, 0x5c, 0x14, 0x2a, 0x17, 0x69, 0x14, 0x0d, 0x09, 0x21, 0x1e, 0x3b, 0x7e, 0x68, 0x5a, 0x12, 0x6a, 0x4b, 0x17, 0x1e, 0x2a, 0x31}));
        put("V21", new ObfuscationValue(0x07, new byte[]{0x65, 0x1d, 0x19, 0x7c, 0x09, 0x79, 0x1d, 0x69, 0x7c, 0x4e, 0x13, 0x0e, 0x04, 0x1b, 0x6a, 0x3c, 0x68, 0x13, 0x15, 0x6b, 0x12, 0x08, 0x3d, 0x1f, 0x16, 0x7e, 0x13, 0x6c}));
        put("V22", new ObfuscationValue(0x08, new byte[]{0x3f, 0x17, 0x79, 0x69, 0x24, 0x6b, 0x37, 0x50, 0x63, 0x09, 0x45, 0x6f, 0x0c, 0x07, 0x07, 0x09, 0x1f, 0x2c, 0x2a, 0x69, 0x12, 0x0c, 0x49, 0x30, 0x10, 0x49, 0x1a, 0x3f, 0x32, 0x04}));
        put("V23", new ObfuscationValue(0x08, new byte[]{0x16, 0x2b, 0x64, 0x62, 0x13, 0x04, 0x18, 0x0d, 0x63, 0x25, 0x14, 0x17, 0x0f, 0x13, 0x46, 0x0c, 0x5b, 0x09, 0x39, 0x1a, 0x79, 0x01, 0x1e, 0x48}));
        put("V24", new ObfuscationValue(0x06, new byte[]{0x7c, 0x36, 0x5c, 0x1a, 0x0d, 0x10, 0x0a, 0x50, 0x07, 0x0f, 0x75, 0x1f, 0x09, 0x3b, 0x0d, 0x72, 0x76, 0x75, 0x5c, 0x7e, 0x35, 0x35, 0x11, 0x5d, 0x4e}));
        put("V25", new ObfuscationValue(0x09, new byte[]{0x07, 0x14, 0x77, 0x1e, 0x3b, 0x5e, 0x79, 0x01, 0x3a, 0x08, 0x07, 0x1f, 0x72, 0x09, 0x55, 0x23, 0x6a, 0x16, 0x12, 0x1e, 0x42, 0x04, 0x16, 0x3d, 0x06, 0x66, 0x5a, 0x07, 0x02, 0x06}));
        put("V26", new ObfuscationValue(0x06, new byte[]{0x03, 0x49, 0x4c, 0x1e, 0x22, 0x4b, 0x1f, 0x0f, 0x1f, 0x70, 0x30, 0x01, 0x60, 0x58, 0x02, 0x7a, 0x30, 0x60, 0x03, 0x0e, 0x4e, 0x07}));
        put("V27", new ObfuscationValue(0x07, new byte[]{0x58, 0x6b, 0x10, 0x79, 0x02, 0x18, 0x10, 0x17, 0x1d, 0x2c, 0x0e, 0x05, 0x65, 0x10, 0x15, 0x22, 0x65, 0x0f, 0x68, 0x28, 0x06, 0x73, 0x1c, 0x08, 0x49, 0x0c, 0x1b, 0x0e}));
        put("V28", new ObfuscationValue(0x0A, new byte[]{0x36, 0x50, 0x1b, 0x73, 0x0f, 0x06, 0x56, 0x2e, 0x1c, 0x4d, 0x14, 0x02, 0x0a, 0x1b, 0x07, 0x7b, 0x50, 0x30, 0x3a, 0x18, 0x7a, 0x61, 0x55, 0x05}));

        put("V9708", new ObfuscationValue(0x05, new byte[]{0x1d, 0x69, 0x49, 0x6d, 0x08, 0x61, 0x17, 0x1e, 0x21, 0x61, 0x6d, 0x1d, 0x1a, 0x51, 0x2e, 0x16, 0x21, 0x06, 0x2a, 0x04, 0x11, 0x09, 0x06, 0x04, 0x3f}));
        put("V1031", new ObfuscationValue(0x08, "Antidisestablishmentarianism".getBytes(StandardCharsets.US_ASCII)));
        put("V2069", new ObfuscationValue(0x07, "Floccinaucinihilipilification".getBytes(StandardCharsets.US_ASCII)));
        put("V9041", new ObfuscationValue(0x06, new byte[]{0x3e, 0x14, 0x0c, 0x12, 0x10, 0x2d, 0x13, 0x26, 0x18, 0x55, 0x1d, 0x05, 0x52, 0x6c, 0x74, 0x03, 0x21, 0x19, 0x1b, 0x13, 0x04, 0x5d, 0x59, 0x19, 0x2c, 0x09, 0x1b}));
        put("V3646", new ObfuscationValue(0x09, new byte[]{0x7e, 0x18, 0x7e, 0x16, 0x4a, 0x5c, 0x18, 0x10, 0x05, 0x0b, 0x07, 0x09, 0x0c, 0x5a, 0x0d, 0x7c, 0x1c, 0x15, 0x1d, 0x11, 0x3e, 0x2c, 0x1b, 0x0e, 0x03, 0x22, 0x34, 0x1b, 0x01}));
        put("V6052", new ObfuscationValue(0x05, new byte[]{0x33, 0x68, 0x05, 0x35, 0x03, 0x5b, 0x5e, 0x3e, 0x19, 0x1c, 0x08, 0x1b, 0x0d, 0x74, 0x6d, 0x34, 0x02, 0x52, 0x70, 0x0c, 0x16, 0x42, 0x0a}));
        put("V9479", new ObfuscationValue(0x09, new byte[]{0x10, 0x1b, 0x4a, 0x18, 0x0a, 0x68, 0x21, 0x10, 0x22, 0x03, 0x3e, 0x5a, 0x27, 0x0d, 0x01, 0x5d, 0x57, 0x06, 0x1c, 0x1e, 0x3f, 0x0f, 0x13}));
        put("V9888", new ObfuscationValue(0x05, new byte[]{0x4b, 0x0c, 0x36, 0x1d, 0x1a, 0x17, 0x70, 0x4f, 0x7d, 0x52, 0x6b, 0x1d, 0x27, 0x77, 0x31, 0x5e, 0x1f, 0x24, 0x1c, 0x7b, 0x43, 0x02, 0x51, 0x06, 0x1d, 0x60}));
        put("V4648", new ObfuscationValue(0x07, new byte[]{0x58, 0x2e, 0x0e, 0x57, 0x1c, 0x2a, 0x4b, 0x12, 0x12, 0x09, 0x0a, 0x0a, 0x17, 0x57, 0x78, 0x01, 0x02, 0x59, 0x66, 0x0f, 0x18, 0x1b, 0x56, 0x58, 0x50, 0x69, 0x01}));
        put("V5683", new ObfuscationValue(0x05, new byte[]{0x7a, 0x33, 0x0a, 0x03, 0x39, 0x12, 0x13, 0x60, 0x06, 0x3d, 0x76, 0x3b, 0x02, 0x4d, 0x54, 0x4b, 0x1e, 0x25, 0x7d, 0x4c, 0x1c, 0x1f, 0x15, 0x0c, 0x11, 0x02, 0x0c, 0x0a, 0x38, 0x17, 0x70}));
    }};

    public static void addProtTable(BinaryIonParser ion) {
        ion.addToCatalog("ProtectedData", 1, SYM_NAMES);
    }

    public static byte[] pkcs7Pad(byte[] msg, int blockLen) {
        int paddingLen = blockLen - (msg.length % blockLen);
        byte padding = (byte) paddingLen;
        byte[] paddingBytes = new byte[paddingLen];
        Arrays.fill(paddingBytes, padding);

        byte[] paddedMsg = new byte[msg.length + paddingLen];
        System.arraycopy(msg, 0, paddedMsg, 0, msg.length);
        System.arraycopy(paddingBytes, 0, paddedMsg, msg.length, paddingLen);

        return paddedMsg;
    }

    public static byte[] pkcs7Unpad(byte[] msg, int blocklen) throws Exception {
        if (msg.length % blocklen != 0)
            throw new IllegalArgumentException("Message length is not a multiple of block size");

        int paddinglen = msg[msg.length - 1] & 0xFF; // Convert the signed byte to an unsigned int

        if (paddinglen <= 0 || paddinglen > blocklen) throw new Exception("Incorrect padding - Wrong key");

        byte[] expectedPadding = new byte[paddinglen];
        Arrays.fill(expectedPadding, (byte) paddinglen);

        byte[] actualPadding = Arrays.copyOfRange(msg, msg.length - paddinglen, msg.length);

        if (!Arrays.equals(expectedPadding, actualPadding)) throw new Exception("Incorrect padding - Wrong key");

        return Arrays.copyOfRange(msg, 0, msg.length - paddinglen);
    }


    public static byte[] obfuscate(byte[] secret, int version) throws NoSuchAlgorithmException {
        if (version == 1) {
            // v1 does not use obfuscation
            return secret;
        }

        // Retrieve the magic number and word from the obfuscation table
        ObfuscationValue obfuscationData = OBFUSCATION_TABLE.get(String.format("V%d", version));
        int magic = obfuscationData.magicNumber();
        byte[] word = obfuscationData.word();

        // Extend secret so that its length is divisible by the magic number
        if (secret.length % magic != 0) {
            int paddingLength = magic - (secret.length % magic);
            byte[] paddedSecret = new byte[secret.length + paddingLength];
            System.arraycopy(secret, 0, paddedSecret, 0, secret.length);
            secret = paddedSecret;
        }

        byte[] obfuscated = new byte[secret.length];
        byte[] wordHash = Arrays.copyOfRange(sha256(word), 0, 16);

        // Shuffle secret and xor it with the first half of the word hash
        for (int i = 0; i < secret.length; i++) {
            int index = (i / (secret.length / magic)) + magic * (i % (secret.length / magic));
            obfuscated[index] = (byte) (secret[i] ^ wordHash[index % 16]);
        }

        return obfuscated;
    }

    public static byte[] scramble(byte[] st, int magic) {
        byte[] ret = new byte[st.length];
        int padLen = st.length;

        for (int counter = 0; counter < st.length; counter++) {
            int ivar2 = (padLen / 2) - 2 * (counter % magic) + magic + counter - 1;
            ret[ivar2 % padLen] = st[counter];
        }

        return ret;
    }

    public static byte[] obfuscate2(byte[] secret, int version) throws NoSuchAlgorithmException {
        if (version == 1) {
            // v1 does not use obfuscation
            return secret;
        }

        // Retrieve the magic number and word from the obfuscation table
        ObfuscationValue obfuscationData = OBFUSCATION_TABLE.get(String.format("V%d", version));
        int magic = obfuscationData.magicNumber();
        byte[] word = obfuscationData.word();

        // Extend secret so that its length is divisible by the magic number
        if (secret.length % magic != 0) {
            int paddingLength = magic - (secret.length % magic);
            secret = Arrays.copyOf(secret, secret.length + paddingLength);
        }

        byte[] obfuscated = new byte[secret.length];
        byte[] wordHash = Arrays.copyOfRange(sha256(word), 16, 32); // Take the last 16 bytes of the hash
        byte[] shuffled = scramble(secret, magic);

        // Perform XOR between shuffled data and the word hash
        for (int i = 0; i < secret.length; i++) obfuscated[i] = (byte) (shuffled[i] ^ wordHash[i % 16]);

        return obfuscated;
    }

    public static byte[] scramble3(byte[] st, int magic) {
        byte[] ret = new byte[st.length];
        int padlen = st.length;
        int divs = padlen / magic;
        int cntr = 0;
        int offset = 0;

        if (0 < ((magic - 1) + divs)) {
            do {
                if ((offset & 1) == 0) {
                    int u_var4 = divs - 1;
                    int i_var3;

                    if (offset < divs) {
                        i_var3 = 0;
                        u_var4 = offset;
                    } else {
                        i_var3 = (offset - divs) + 1;
                    }

                    if (u_var4 >= 0) {
                        int i_var5 = u_var4 * magic;
                        int index = (padlen - 1) - cntr;

                        while (true) {
                            if (magic <= i_var3) break;

                            ret[index] = st[i_var3 + i_var5];
                            i_var3++;
                            cntr++;
                            u_var4--;
                            i_var5 -= magic;
                            index--;

                            if (u_var4 <= -1) break;
                        }
                    }
                } else {
                    int i_var3;

                    if (offset < magic) {
                        i_var3 = 0;
                    } else {
                        i_var3 = (offset - magic) + 1;
                    }

                    if (i_var3 < divs) {
                        int u_var4 = offset;

                        if (magic <= offset) {
                            u_var4 = magic - 1;
                        }

                        int index = (padlen - 1) - cntr;
                        int i_var5 = i_var3 * magic;

                        while (true) {
                            if (u_var4 < 0) break;

                            i_var3++;
                            ret[index] = st[u_var4 + i_var5];
                            u_var4--;
                            index--;
                            i_var5 += magic;
                            cntr++;

                            if (i_var3 >= divs) break;
                        }
                    }
                }

                offset++;

            } while (offset < ((magic - 1) + divs));
        }

        return ret;
    }

    public static byte[] obfuscate3(byte[] secret, int version) throws NoSuchAlgorithmException {
        if (version == 1) {
            // v1 does not use obfuscation
            return secret;
        }

        // Retrieve the magic number and word from the obfuscation table
        ObfuscationValue obfuscationData = OBFUSCATION_TABLE.get(String.format("V%d", version));
        int magic = obfuscationData.magicNumber();
        byte[] word = obfuscationData.word();

        // Extend secret so that its length is divisible by the magic number
        if (secret.length % magic != 0) {
            int paddingLength = magic - (secret.length % magic);
            secret = Arrays.copyOf(secret, secret.length + paddingLength);
        }

        byte[] obfuscated = new byte[secret.length];
        byte[] wordHash = sha256(word); // Compute the SHA-256 hash of the word
        byte[] shuffled = scramble3(secret, magic); // Perform the scramble using the scramble3 method

        // XOR the shuffled data with the first half of the word hash
        for (int i = 0; i < secret.length; i++) obfuscated[i] = (byte) (shuffled[i] ^ wordHash[i % 16]);

        return obfuscated;
    }

    public static byte[] processV9708(byte[] st) {
        Workspace ws = new Workspace(new int[]{0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11});
        int[] repl = new int[]{0, 5, 10, 15, 4, 9, 14, 3, 8, 13, 2, 7, 12, 1, 6, 11};

        int remln = st.length;
        int sto = 0;

        List<Integer> out = new ArrayList<>();

        while (remln > 0) {
            ws.shuffle(repl);
            ws.sbox(d0x6a06ea70, d0x6a0dab50);
            ws.sbox(d0x6a073a70, d0x6a0dab50);
            ws.shuffle(repl);
            ws.exlookup(d0x6a072a70);

            List<Integer> dat = ws.mask(Arrays.copyOfRange(st, sto, sto + 16));
            out.addAll(dat);

            sto += 16;
            remln -= 16;
        }

        return toByteArray(out, st.length);
    }

    public static byte[] processV1031(byte[] st) {
        Workspace ws = new Workspace(Arrays.asList(0x06, 0x18, 0x60, 0x68, 0x3B, 0x62, 0x3E, 0x3C, 0x06, 0x50, 0x71, 0x52, 0x02, 0x5A, 0x63, 0x03));
        List<Integer> repl = Arrays.asList(0, 5, 10, 15, 4, 9, 14, 3, 8, 13, 2, 7, 12, 1, 6, 11);
        int remln = st.length;
        int sto = 0;

        List<Integer> out = new ArrayList<>();

        while (remln > 0) {
            ws.shuffle(repl);
            ws.sbox(d0x6a0797c0, d0x6a0dab50, List.of(3));
            ws.sbox(d0x6a07e7c0, d0x6a0dab50, List.of(3));
            ws.shuffle(repl);
            ws.sbox(d0x6a0797c0, d0x6a0dab50, List.of(3));
            ws.sbox(d0x6a07e7c0, d0x6a0dab50, List.of(3));
            ws.exlookup(d0x6a07d7c0);

            List<Integer> dat = ws.mask(Arrays.copyOfRange(st, sto, sto + 16));
            out.addAll(dat);

            sto += 16;
            remln -= 16;
        }

        return toByteArray(out, st.length);
    }

    public static byte[] processV2069(byte[] st) {
        Workspace ws = new Workspace(Arrays.asList(0x79, 0x0D, 0x12, 0x08, 0x66, 0x77, 0x2E, 0x5B, 0x02, 0x09, 0x0A, 0x13, 0x11, 0x0C, 0x11, 0x62));
        List<Integer> repl = Arrays.asList(0, 5, 10, 15, 4, 9, 14, 3, 8, 13, 2, 7, 12, 1, 6, 11);
        int remln = st.length;
        int sto = 0;

        List<Integer> out = new ArrayList<>();

        while (remln > 0) {
            ws.sbox(d0x6a084498, d0x6a0dab50, List.of(2));
            ws.shuffle(repl);
            ws.sbox(d0x6a089498, d0x6a0dab50, List.of(2));
            ws.sbox(d0x6a089498, d0x6a0dab50, List.of(2));
            ws.sbox(d0x6a084498, d0x6a0dab50, List.of(2));
            ws.shuffle(repl);
            ws.exlookup(d0x6a088498);

            List<Integer> dat = ws.mask(Arrays.copyOfRange(st, sto, sto + 16));
            out.addAll(dat);

            sto += 16;
            remln -= 16;
        }

        return toByteArray(out, st.length);
    }

    public static byte[] processV9041(byte[] st) {
        Workspace ws = new Workspace(new int[]{0x49, 0x0b, 0x0e, 0x3b, 0x19, 0x1a, 0x49, 0x61, 0x10, 0x73, 0x19, 0x67, 0x5c, 0x1b, 0x11, 0x21});
        int[] repl = {0, 5, 10, 15, 4, 9, 14, 3, 8, 13, 2, 7, 12, 1, 6, 11};
        int remln = st.length;
        int sto = 0;
        List<Integer> out = new ArrayList<>();

        while (remln > 0) {
            ws.sbox(d0x6a094170, d0x6a0dab50, new int[]{1});
            ws.shuffle(repl);
            ws.shuffle(repl);
            ws.sbox(d0x6a08f170, d0x6a0dab50, new int[]{1});
            ws.sbox(d0x6a08f170, d0x6a0dab50, new int[]{1});
            ws.sbox(d0x6a094170, d0x6a0dab50, new int[]{1});

            ws.exlookup(d0x6a093170);

            List<Integer> dat = ws.mask(Arrays.copyOfRange(st, sto, sto + 16));
            out.addAll(dat);

            sto += 16;
            remln -= 16;
        }

        return toByteArray(out, st.length);
    }

    public static byte[] processV3646(byte[] st) {
        Workspace ws = new Workspace(new int[]{0x0a, 0x36, 0x3e, 0x29, 0x4e, 0x02, 0x18, 0x38, 0x01, 0x36, 0x73, 0x13, 0x14, 0x1b, 0x16, 0x6a});
        int[] repl = {0, 5, 10, 15, 4, 9, 14, 3, 8, 13, 2, 7, 12, 1, 6, 11};
        int remln = st.length;
        int sto = 0;
        List<Integer> out = new ArrayList<>();

        while (remln > 0) {
            ws.shuffle(repl);
            ws.sbox(d0x6a099e48, d0x6a0dab50, new int[]{2, 3});
            ws.sbox(d0x6a09ee48, d0x6a0dab50, new int[]{2, 3});
            ws.sbox(d0x6a09ee48, d0x6a0dab50, new int[]{2, 3});
            ws.shuffle(repl);
            ws.sbox(d0x6a099e48, d0x6a0dab50, new int[]{2, 3});
            ws.sbox(d0x6a099e48, d0x6a0dab50, new int[]{2, 3});
            ws.shuffle(repl);
            ws.sbox(d0x6a09ee48, d0x6a0dab50, new int[]{2, 3});
            ws.exlookup(d0x6a09de48);

            List<Integer> dat = ws.mask(Arrays.copyOfRange(st, sto, sto + 16));
            out.addAll(dat);

            sto += 16;
            remln -= 16;
        }

        return toByteArray(out, st.length);
    }

    public static byte[] processV6052(byte[] st) {
        Workspace ws = new Workspace(new int[]{0x5f, 0x0d, 0x01, 0x12, 0x5d, 0x5c, 0x14, 0x2a, 0x17, 0x69, 0x14, 0x0d, 0x09, 0x21, 0x1e, 0x3b});
        int[] repl = {0, 5, 10, 15, 4, 9, 14, 3, 8, 13, 2, 7, 12, 1, 6, 11};
        int remln = st.length;
        int sto = 0;
        List<Integer> out = new ArrayList<>();

        while (remln > 0) {
            ws.shuffle(repl);
            ws.sbox(d0x6a0a4b20, d0x6a0dab50, new int[]{1, 3});
            ws.shuffle(repl);
            ws.sbox(d0x6a0a4b20, d0x6a0dab50, new int[]{1, 3});
            ws.sbox(d0x6a0a9b20, d0x6a0dab50, new int[]{1, 3});
            ws.shuffle(repl);
            ws.sbox(d0x6a0a9b20, d0x6a0dab50, new int[]{1, 3});
            ws.sbox(d0x6a0a9b20, d0x6a0dab50, new int[]{1, 3});
            ws.sbox(d0x6a0a4b20, d0x6a0dab50, new int[]{1, 3});

            ws.exlookup(d0x6a0a8b20);

            List<Integer> dat = ws.mask(Arrays.copyOfRange(st, sto, sto + 16));
            out.addAll(dat);

            sto += 16;
            remln -= 16;
        }

        return toByteArray(out, st.length);
    }

    public static byte[] processV9479(byte[] st) {
        Workspace ws = new Workspace(new int[]{0x65, 0x1d, 0x19, 0x7c, 0x09, 0x79, 0x1d, 0x69, 0x7c, 0x4e, 0x13, 0x0e, 0x04, 0x1b, 0x6a, 0x3c});
        int[] repl = {0, 5, 10, 15, 4, 9, 14, 3, 8, 13, 2, 7, 12, 1, 6, 11};
        int remln = st.length;
        int sto = 0;
        List<Integer> out = new ArrayList<>();

        while (remln > 0) {
            ws.sbox(d0x6a0af7f8, d0x6a0dab50, new int[]{1, 2, 3});
            ws.sbox(d0x6a0af7f8, d0x6a0dab50, new int[]{1, 2, 3});
            ws.sbox(d0x6a0b47f8, d0x6a0dab50, new int[]{1, 2, 3});
            ws.sbox(d0x6a0af7f8, d0x6a0dab50, new int[]{1, 2, 3});
            ws.shuffle(repl);
            ws.sbox(d0x6a0b47f8, d0x6a0dab50, new int[]{1, 2, 3});
            ws.shuffle(repl);
            ws.shuffle(repl);
            ws.sbox(d0x6a0b47f8, d0x6a0dab50, new int[]{1, 2, 3});
            ws.exlookup(d0x6a0b37f8);

            List<Integer> dat = ws.mask(Arrays.copyOfRange(st, sto, sto + 16));
            out.addAll(dat);

            sto += 16;
            remln -= 16;
        }

        return toByteArray(out, st.length);
    }

    public static byte[] processV9888(byte[] st) {
        Workspace ws = new Workspace(new int[]{0x3f, 0x17, 0x79, 0x69, 0x24, 0x6b, 0x37, 0x50, 0x63, 0x09, 0x45, 0x6f, 0x0c, 0x07, 0x07, 0x09});
        int[] repl = {0, 5, 10, 15, 4, 9, 14, 3, 8, 13, 2, 7, 12, 1, 6, 11};
        int remln = st.length;
        int sto = 0;
        List<Integer> out = new ArrayList<>();

        while (remln > 0) {
            ws.sbox(d0x6a0ba4d0, d0x6a0dab50, new int[]{1, 2});
            ws.sbox(d0x6a0bf4d0, d0x6a0dab50, new int[]{1, 2});
            ws.sbox(d0x6a0bf4d0, d0x6a0dab50, new int[]{1, 2});
            ws.sbox(d0x6a0ba4d0, d0x6a0dab50, new int[]{1, 2});
            ws.shuffle(repl);
            ws.shuffle(repl);
            ws.shuffle(repl);
            ws.sbox(d0x6a0bf4d0, d0x6a0dab50, new int[]{1, 2});
            ws.sbox(d0x6a0ba4d0, d0x6a0dab50, new int[]{1, 2});
            ws.exlookup(d0x6a0be4d0);

            List<Integer> dat = ws.mask(Arrays.copyOfRange(st, sto, sto + 16));
            out.addAll(dat);

            sto += 16;
            remln -= 16;
        }

        return toByteArray(out, st.length);
    }

    public static byte[] processV4648(byte[] st) {
        Workspace ws = new Workspace(new int[]{0x16, 0x2b, 0x64, 0x62, 0x13, 0x04, 0x18, 0x0d, 0x63, 0x25, 0x14, 0x17, 0x0f, 0x13, 0x46, 0x0c});
        int[] repl = {0, 5, 10, 15, 4, 9, 14, 3, 8, 13, 2, 7, 12, 1, 6, 11};
        int remln = st.length;
        int sto = 0;
        List<Integer> out = new ArrayList<>();

        while (remln > 0) {
            ws.sbox(d0x6a0ca1a8, d0x6a0dab50, new int[]{1, 3});
            ws.shuffle(repl);
            ws.sbox(d0x6a0ca1a8, d0x6a0dab50, new int[]{1, 3});
            ws.sbox(d0x6a0c51a8, d0x6a0dab50, new int[]{1, 3});
            ws.sbox(d0x6a0ca1a8, d0x6a0dab50, new int[]{1, 3});
            ws.sbox(d0x6a0c51a8, d0x6a0dab50, new int[]{1, 3});
            ws.sbox(d0x6a0c51a8, d0x6a0dab50, new int[]{1, 3});
            ws.shuffle(repl);
            ws.shuffle(repl);
            ws.exlookup(d0x6a0c91a8);

            List<Integer> dat = ws.mask(Arrays.copyOfRange(st, sto, sto + 16));
            out.addAll(dat);

            sto += 16;
            remln -= 16;
        }

        return toByteArray(out, st.length);
    }

    public static byte[] processV5683(byte[] st) {
        Workspace ws = new Workspace(new int[]{0x7c, 0x36, 0x5c, 0x1a, 0x0d, 0x10, 0x0a, 0x50, 0x07, 0x0f, 0x75, 0x1f, 0x09, 0x3b, 0x0d, 0x72});
        int[] repl = {0, 5, 10, 15, 4, 9, 14, 3, 8, 13, 2, 7, 12, 1, 6, 11};
        int remln = st.length;
        int sto = 0;
        List<Integer> out = new ArrayList<>();

        while (remln > 0) {
            ws.sbox(d0x6a0d4e80, d0x6a0dab50, new int[]{});
            ws.shuffle(repl);
            ws.sbox(d0x6a0cfe80, d0x6a0dab50, new int[]{});
            ws.sbox(d0x6a0d4e80, d0x6a0dab50, new int[]{});
            ws.sbox(d0x6a0cfe80, d0x6a0dab50, new int[]{});
            ws.sbox(d0x6a0d4e80, d0x6a0dab50, new int[]{});
            ws.shuffle(repl);
            ws.sbox(d0x6a0cfe80, d0x6a0dab50, new int[]{});
            ws.shuffle(repl);
            ws.exlookup(d0x6a0d3e80);

            List<Integer> dat = ws.mask(Arrays.copyOfRange(st, sto, sto + 16));
            out.addAll(dat);

            sto += 16;
            remln -= 16;
        }

        return toByteArray(out, st.length);
    }
}
