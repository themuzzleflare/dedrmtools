/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.mobidedrm;

import cloud.tavitian.dedrmtools.Book;
import cloud.tavitian.dedrmtools.Debug;
import cloud.tavitian.dedrmtools.PIDMetaInfo;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static cloud.tavitian.dedrmtools.CharMaps.*;
import static cloud.tavitian.dedrmtools.Util.*;
import static cloud.tavitian.dedrmtools.kindlekeys.KindleKeyUtils.checksumPid;

public final class MobiBook extends Book {
    private static final String version = "3.0";

    /**
     * The data file
     */
    private final byte[] dataFile;
    /**
     * The header of the file, first 78 bytes
     */
    private final byte[] header;
    /**
     * The magic bytes of the file
     */
    private final byte[] magic;
    /**
     * The number of sections in the file
     */
    private final int numSections;
    /**
     * The sections of the file
     */
    private final BookSections sections = new BookSections();
    /**
     * The metadata dictionary
     */
    private final MetaDictionary metaArray = new MetaDictionary();
    /**
     * the first section of the file
     */
    private final byte[] sect;
    /**
     * The number of records
     */
    private final int records;
    /**
     * The compression type
     */
    private final int compression;
    /**
     * The decrypted mobi data
     */
    byte[] mobiData;
    /**
     * The crypto type
     */
    private int cryptoType = -1;
    /**
     * The flag to print replica
     */
    private boolean printReplica = false;
    /**
     * The extra data flags
     */
    private int extraDataFlags = 0;
    /**
     * The mobi length
     */
    private int mobiLength = 0;
    /**
     * The mobi codepage
     */
    private int mobiCodepage = 1252;
    /**
     * The mobi version
     */
    private int mobiVersion = -1;

    public MobiBook(String infile) throws Exception {
        super();
        System.out.printf("MobiDeDrm v%s.%n", version);
        System.out.println("Removes protection from Kindle/Mobipocket, Kindle/KF8 and Kindle/Print Replica eBooks.");

        // initial sanity check on file
        FileInputStream fis = new FileInputStream(infile);
        dataFile = fis.readAllBytes();
        fis.close();

        header = Arrays.copyOfRange(dataFile, 0, 78);
        magic = Arrays.copyOfRange(header, 0x3C, 0x3C + 8);

        if (!Arrays.equals(magic, bookmobiBytes) && !Arrays.equals(magic, textreadBytes))
            throw new Exception("Invalid file format.");

        // build up section offset and flag info
        // python: self.num_sections, = struct.unpack('>H', self.header[76:78])
        numSections = ByteBuffer.wrap(header, 76, 2).getShort();

        for (int i = 0; i < numSections; i++) {
            // python:
            // offset, a1, a2, a3, a4 = struct.unpack('>LBBBB', self.data_file[78 + i * 8:78 + i * 8 + 8])
            // flags, val = a1, a2 << 16 | a3 << 8 | a4
            // self.sections.append((offset, flags, val))
            ByteBuffer buffer = ByteBuffer.wrap(dataFile, 78 + i * 8, 8);

            int offset = buffer.getInt();
            int a1 = buffer.get() & 0xFF;
            int a2 = buffer.get() & 0xFF;
            int a3 = buffer.get() & 0xFF;
            int a4 = buffer.get() & 0xFF;

            int flags = a1;
            int val = (a2 << 16) | (a3 << 8) | a4;

            BookSection bookSection = new BookSection(offset, flags, val);

            sections.add(bookSection);
        }

        // parse information from section 0
        sect = loadSection(0);
        // python: self.records, = struct.unpack('>H', self.sect[0x8:0x8 + 2])
        records = ByteBuffer.wrap(sect, 0x8, 2).getShort();
        // python: self.compression, = struct.unpack('>H', self.sect[0x0:0x0 + 2])
        compression = ByteBuffer.wrap(sect, 0x0, 2).getShort();

        if (Arrays.equals(magic, textreadBytes)) {
            System.out.println("PalmDoc format book detected.");
            return;
        }

        // python:
        //  self.mobi_length, = struct.unpack('>L', self.sect[0x14:0x18])
        //  self.mobi_codepage, = struct.unpack('>L', self.sect[0x1c:0x20])
        //  self.mobi_version, = struct.unpack('>L', self.sect[0x68:0x6C])
        mobiLength = ByteBuffer.wrap(sect, 0x14, 4).getInt();
        mobiCodepage = ByteBuffer.wrap(sect, 0x1c, 4).getInt();
        mobiVersion = ByteBuffer.wrap(sect, 0x68, 4).getInt();

        if (mobiLength >= 0xE4 && mobiVersion >= 5) extraDataFlags = ByteBuffer.wrap(sect, 0xF2, 2).getShort();

        // multibyte utf8 data is included in the encryption for PalmDoc compression so clear that byte so that we leave it to be decrypted.
        if (compression != 17480) extraDataFlags &= 0xFFFE;

        // if exth region exists parse it for metadata array
        try {
            int exthFlag = ByteBuffer.wrap(sect, 0x80, 4).getInt();
            byte[] exth = new byte[0];

            if ((exthFlag & 0x40) != 0) exth = Arrays.copyOfRange(sect, 16 + mobiLength, sect.length);

            byte[] rangeToCheck = Arrays.copyOfRange(exth, 0, 4);

            if (exth.length >= 12 && Arrays.equals(rangeToCheck, exthBytes)) {
                int nItems = ByteBuffer.wrap(exth, 8, 4).getInt();
                int pos = 12;

                for (int i = 0; i < nItems; i++) {
                    int type = ByteBuffer.wrap(exth, pos, 4).getInt();
                    int size = ByteBuffer.wrap(exth, pos + 4, 4).getInt();
                    byte[] content = Arrays.copyOfRange(exth, pos + 8, pos + size);

                    metaArray.put(type, content);

                    // reset the text to speech flag and clipping limit, if present
                    if (type == 401 && size == 9) {
                        // set clipping limit to 100%
                        // python: self.patch_section(0, b'\144', 16 + self.mobi_length + pos + 8)
                        byte[] newContent = {(byte) 100};
                        patchSection(0, newContent, 16 + mobiLength + pos + 8);
                    } else if (type == 404 && size == 9) {
                        // make sure text to speech is enabled
                        // python: self.patch_section(0, b'\0', 16 + self.mobi_length + pos + 8)
                        byte[] newContent = new byte[0];
                        patchSection(0, newContent, 16 + mobiLength + pos + 8);
                    } else if (type == 405 && size == 9) {
                        // remove rented book flag
                        // python: self.patch_section(0, b'\0', 16 + self.mobi_length + pos + 8)
                        byte[] newContent = new byte[0];
                        patchSection(0, newContent, 16 + mobiLength + pos + 8);
                    } else if (type == 406 && size == 16) {
                        // remove rental due date
                        // python: self.patch_section(0, b'\0' * 8, 16 + self.mobi_length + pos + 8)
                        byte[] newContent = new byte[8];
                        patchSection(0, newContent, 16 + mobiLength + pos + 8);
                    } else if (type == 208) {
                        // remove watermark (atv:kin: stuff)
                        // python:  self.patch_section(0, b'\0' * (size - 8), 16 + self.mobi_length + pos + 8)
                        byte[] newContent = new byte[size - 8];
                        patchSection(0, newContent, 16 + mobiLength + pos + 8);
                    }

                    pos += size;
                }
            }
        } catch (Exception e) {
            System.err.printf("Cannot set metaArray: Error: %s%n", e.getMessage());
        }
    }

    private static byte[] pc1(byte[] key, byte[] src, boolean decryption) throws Exception {
        return PukallCipher.pc1(key, src, decryption);
    }

    private static byte[] pc1(byte[] key, byte[] src) throws Exception {
        return pc1(key, src, true);
    }

    private static int getSizeOfTrailingDataEntries(byte[] ptr, int size, int flags) {
        int num = 0, testflags = flags >> 1;

        while (testflags != 0) {
            if ((testflags & 1) != 0) num += getSizeOfTrailingDataEntry(ptr, size - num);
            testflags >>= 1;
        }

        // Check the low bit to see if there's multibyte data present.
        // If multibyte data is included in the encryped data, we'll have already cleared this flag.
        if ((flags & 1) != 0) num += (ptr[size - num - 1] & 0x3) + 1;

        return num;
    }

    private static int getSizeOfTrailingDataEntry(byte[] ptr, int size) {
        int bitpos = 0, result = 0;

        if (size <= 0) return result;

        while (true) {
            byte v = ptr[size - 1];

            result |= (v & 0x7F) << bitpos;

            bitpos += 7;
            size -= 1;

            if ((v & 0x80) != 0 || (bitpos >= 28) || (size == 0)) return result;
        }
    }

    private static DRMInfo parseDrm(byte[] data, int count, Set<String> pidSet) throws Exception {
        byte[] foundKey = null;
        String foundPid = null;

        // b'\x72\x38\x33\xB0\xB4\xF2\xE3\xCA\xDF\x09\x01\xD6\xE2\xE0\x3F\x96'
        byte[] keyvec1 = new byte[]{
                (byte) 0x72, (byte) 0x38, (byte) 0x33, (byte) 0xB0,
                (byte) 0xB4, (byte) 0xF2, (byte) 0xE3, (byte) 0xCA,
                (byte) 0xDF, (byte) 0x09, (byte) 0x01, (byte) 0xD6,
                (byte) 0xE2, (byte) 0xE0, (byte) 0x3F, (byte) 0x96
        };

        for (String pid : pidSet) {
            byte[] bigPidBytes = pid.getBytes(StandardCharsets.UTF_8); // Convert the string PID to bytes
            byte[] bigPid = ljustBytes(bigPidBytes, 16, (byte) 0); // Pad the PID to 16 bytes
            byte[] tempKey = pc1(keyvec1, bigPid, false); // Encrypt the padded PID with the keyvec1 to get a temp key
            int tempKeySum = sumBytes(tempKey); // Calculate the checksum of the temp key

            for (int i = 0; i < count; i++) {
                // python: verification, size, type, cksum, cookie = struct.unpack('>LLLBxxx32s', data[i * 0x30:i * 0x30 + 0x30])
                ByteBuffer buffer = ByteBuffer.wrap(data, i * 0x30, 0x30);

                // get some values from data, specifically verification, cksum, and cookie
                long verification = buffer.getInt();
                long size = buffer.getInt() & 0xFFFFFFFFL;
                long type = buffer.getInt() & 0xFFFFFFFFL;
                int cksum = buffer.get() & 0xFF;
                byte[] cookie = new byte[32];

                buffer.position(buffer.position() + 3); // Skip padding bytes (xxx)
                buffer.get(cookie);

                // Check if the checksum of the temp key matches the cksum extracted from the data
                if (cksum == tempKeySum) {
                    cookie = pc1(tempKey, cookie); // Decrypt the cookie we extracted from data with the temp key

                    // python: ver, flags, finalkey, expiry, expiry2 = struct.unpack('>LL16sLL', cookie)
                    ByteBuffer cookieBuffer = ByteBuffer.wrap(cookie);

                    // Extract some values from the decrypted cookie, specifically ver, flags, and the final key
                    long ver = cookieBuffer.getInt() & 0xFFFFFFFFL;
                    int flags = cookieBuffer.getInt();
                    byte[] finalKey = new byte[16];

                    cookieBuffer.get(finalKey);

                    long expiry = cookieBuffer.getInt() & 0xFFFFFFFFL;
                    long expiry2 = cookieBuffer.getInt() & 0xFFFFFFFFL;

                    // Check if:
                    // - the verification value extracted from data matches the ver value extracted from the decrypted cookie
                    // - the flags extracted from the decrypted cookie have the first 5 bits set to 1
                    if (verification == ver && (flags & 0x1F) == 1) {
                        foundKey = finalKey; // Set the found key to the final key extracted from the decrypted cookie
                        foundPid = pid; // Set the found PID to the current PID
                        break;
                    }
                }
            }

            if (foundKey != null) break;
        }

        if (foundKey == null) {
            // Then try the default encoding that doesn't require a PID
            foundPid = "00000000"; // Set the found PID to the default PID
            int tempKeySum = sumBytes(keyvec1); // Now using keyvec1 as the temp key. Calculate the checksum of keyvec1

            for (int i = 0; i < count; i++) {
                ByteBuffer buffer = ByteBuffer.wrap(data, i * 0x30, 0x30);

                // get some values from data, specifically verification, cksum, and cookie
                long verification = buffer.getInt() & 0xFFFFFFFFL;
                long size = buffer.getInt() & 0xFFFFFFFFL;
                long type = buffer.getInt() & 0xFFFFFFFFL;
                int cksum = buffer.get() & 0xFF;
                byte[] cookie = new byte[32];

                buffer.position(buffer.position() + 3);  // Skip padding bytes
                buffer.get(cookie);

                // Check if the checksum of the temp key matches the cksum extracted from the data
                if (cksum == tempKeySum) {
                    cookie = pc1(keyvec1, cookie); // Decrypt the cookie we extracted from data with the temp key

                    ByteBuffer cookieBuffer = ByteBuffer.wrap(cookie);

                    // Extract some values from the decrypted cookie, specifically ver, flags, and the final key
                    long ver = cookieBuffer.getInt() & 0xFFFFFFFFL;
                    int flags = cookieBuffer.getInt();
                    byte[] finalKey = new byte[16];

                    cookieBuffer.get(finalKey);

                    long expiry = cookieBuffer.getInt() & 0xFFFFFFFFL;
                    long expiry2 = cookieBuffer.getInt() & 0xFFFFFFFFL;

                    // check if the verification value extracted from data matches the ver value extracted from the decrypted cookie
                    if (verification == ver) {
                        foundKey = finalKey; // Set the found key to the final key extracted from the decrypted cookie
                        break;
                    }
                }
            }
        }

        return new DRMInfo(foundKey, foundPid);
    }

    /**
     * @param section The section index to load
     * @return The byte array of the section
     */
    private byte[] loadSection(int section) {
        int endoff = section + 1 == numSections ? dataFile.length : sections.get(section + 1).offset();

        int off = sections.get(section).offset();

        return Arrays.copyOfRange(dataFile, off, endoff);
    }

    @Override
    public String getBookTitle() {
        Map<Integer, String> codecMap = new LinkedHashMap<>();

        codecMap.put(1252, "windows-1252");
        codecMap.put(65001, "utf-8");

        byte[] title = new byte[0];
        String codec = "windows-1252";

        if (Arrays.equals(magic, bookmobiBytes)) {
            if (metaArray.containsKey(503)) title = metaArray.get(503);
            else {
                // python:
                // toff, tlen = struct.unpack('>II', self.sect[0x54:0x5c])
                // tend = toff + tlen
                // title = self.sect[toff:tend]
                int toff = ByteBuffer.wrap(sect, 0x54, 4).getInt();
                int tlen = ByteBuffer.wrap(sect, 0x58, 4).getInt();
                int tend = toff + tlen;

                title = Arrays.copyOfRange(sect, toff, tend);
            }

            if (codecMap.containsKey(mobiCodepage)) codec = codecMap.get(mobiCodepage);
        }

        if (title.length == 0) {
            title = Arrays.copyOfRange(header, 0, 32);
            title = Arrays.copyOf(title, indexOfNullByte(title)); // Remove null bytes
        }

        return new String(title, Charset.forName(codec));
    }

    @Override
    public PIDMetaInfo getPidMetaInfo() {
        byte[] rec209 = new byte[0];

        ByteArrayOutputStream tokenStream = new ByteArrayOutputStream();

        if (metaArray.containsKey(209)) {
            rec209 = metaArray.get(209);

            // The 209 data comes in five-byte groups. Interpret the last four bytes
            // of each group as a big-endian unsigned integer to get a key value.
            // If that key exists in the metaArray, append its contents to the token.
            for (int i = 0; i < rec209.length; i += 5) {
                // python:
                // val, = struct.unpack('>I', data[i + 1:i + 5])
                // sval = self.meta_array.get(val, b'')
                // token += sval
                int val = ByteBuffer.wrap(rec209, i + 1, 4).getInt();
                byte[] sval = metaArray.getOrDefault(val, new byte[0]);

                for (byte b : sval) tokenStream.write(b);
            }
        }

        return new PIDMetaInfo(rec209, tokenStream.toByteArray());
    }

    private void patch(int off, byte[] newContent) {
        // python: self.data_file = self.data_file[:off] + new + self.data_file[off + len(new):]
        System.arraycopy(newContent, 0, dataFile, off, newContent.length);
    }

    private void patchSection(int section, byte[] newContent, int inOff) {
        int endoff = section + 1 == numSections ? dataFile.length : sections.get(section + 1).offset();
        int off = sections.get(section).offset();

        assert off + inOff + newContent.length <= endoff;

        patch(off + inOff, newContent);
    }

    private void patchSection(int section, byte[] newContent) {
        patchSection(section, newContent, 0);
    }

    @Override
    public void getFile(String outpath) throws IOException {
        FileOutputStream fos = new FileOutputStream(outpath);
        fos.write(mobiData);
        fos.close();
    }

    @Override
    public String getBookType() {
        if (printReplica) return "Print Replica";

        if (mobiVersion >= 8) return "Kindle Format 8";

        if (mobiVersion >= 0) return String.format("Mobipocket %d", mobiVersion);

        return "PalmDoc";
    }

    @Override
    public String getBookExtension() {
        if (printReplica) return ".azw4";

        if (mobiVersion >= 8) return ".azw3";

        return ".mobi";
    }

    @Override
    public void processBook(Set<String> pidSet) throws Exception {
        cryptoType = ByteBuffer.wrap(sect, 0xC, 2).getShort();

        System.out.printf("Crypto Type is: %d%n", cryptoType);

        if (cryptoType == 0) {
            System.out.println("This book is not encrypted.");

            // Still check for Print Replica
            byte[] data = loadSection(1);
            byte[] rangeToCheck = Arrays.copyOfRange(data, 0, 4);

            printReplica = Arrays.equals(rangeToCheck, mopBytes);

            mobiData = dataFile; // No need to decrypt

            return;
        }

        if (cryptoType != 2 && cryptoType != 1)
            throw new Exception(String.format("Cannot decode unknown Mobipocket encryption type %d", cryptoType));

        if (metaArray.containsKey(406)) {
            // python:
            // data406 = self.meta_array[406]
            // val406, = struct.unpack('>Q', data406)
            byte[] data406 = metaArray.get(406);
            long val406 = ByteBuffer.wrap(data406).getLong();

            if (val406 != 0) {
                System.out.printf("Warning: This is a library or rented eBook (%d). Continuing...%n", val406);
                // Uncomment the line below to enforce stopping on rented books:
                // throw new Exception("Cannot decode library or rented ebooks.");
            }
        }

        // Normalise PID list
        Set<String> goodPids = normalisePids(pidSet);

        Debug.printf("PIDs: %s%n", pidSet);
        Debug.printf("Good PIDs: %s%n", goodPids);

        byte[] foundKey;
        String pid;

        if (cryptoType == 1) {
            // Type 1 encryption
            byte[] t1Keyvec = "QDCVEPMU675RUBSZ".getBytes(StandardCharsets.US_ASCII);
            byte[] bookKeyData;

            if (Arrays.equals(magic, textreadBytes)) bookKeyData = Arrays.copyOfRange(sect, 0x0E, 0x0E + 16);
            else if (mobiVersion < 0) bookKeyData = Arrays.copyOfRange(sect, 0x90, 0x90 + 16);
            else bookKeyData = Arrays.copyOfRange(sect, mobiLength + 16, mobiLength + 32);

            pid = "00000000";
            foundKey = pc1(t1Keyvec, bookKeyData);
        } else {
            // Type 2 encryption

            // extract the DRM data
            int drmPtr = ByteBuffer.wrap(sect, 0xA8, 4).getInt();
            int drmCount = ByteBuffer.wrap(sect, 0xAC, 4).getInt();
            int drmSize = ByteBuffer.wrap(sect, 0xB0, 4).getInt();

            if (drmCount == 0)
                throw new Exception("Encryption not initialised. Must be opened with Mobipocket Reader first.");

            byte[] drmData = Arrays.copyOfRange(sect, drmPtr, drmPtr + drmSize);

            DRMInfo drmResult = parseDrm(drmData, drmCount, goodPids);

            foundKey = drmResult.key();
            pid = drmResult.pid();

            if (foundKey == null)
                throw new Exception(String.format("No key found in %d PIDs tried.", goodPids.size()));

            // Clear the DRM keys
            // self.patch_section(0, b'\0' * drm_size, drm_ptr)
            patchSection(0, new byte[drmSize], drmPtr);
            // Kill the DRM pointers
            // self.patch_section(0, b'\xff' * 4 + b'\0' * 12, 0xA8)
            patchSection(0, new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, 0xA8);
        }

        if (pid.equals("00000000")) System.out.println("File has default encryption, no specific key needed.");
        else System.out.printf("File is encoded with PID %s.%n", checksumPid(pid, letters));

        // Clear the crypto type
        // self.patch_section(0, b'\0' * 2, 0xC)
        patchSection(0, new byte[2], 0xC);

        // Decrypt sections
        System.out.print("Decrypting. Please wait . . .");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] rangeToWrite = Arrays.copyOfRange(dataFile, 0, sections.get(1).offset());
        outputStream.write(rangeToWrite); // Append data before the first section

        for (int i = 1; i <= records; i++) {
            byte[] data = loadSection(i);
            int extraSize = getSizeOfTrailingDataEntries(data, data.length, extraDataFlags);

            if (i % 100 == 0) System.out.print(" .");

            byte[] rangeToDecode = Arrays.copyOfRange(data, 0, data.length - extraSize);
            byte[] decodedData = pc1(foundKey, rangeToDecode);

            if (i == 1) {
                byte[] rangeToCheck = Arrays.copyOfRange(decodedData, 0, 4);
                printReplica = Arrays.equals(mopBytes, rangeToCheck);
            }

            outputStream.write(decodedData);

            if (extraSize > 0) {
                rangeToWrite = Arrays.copyOfRange(data, data.length - extraSize, data.length);
                outputStream.write(rangeToWrite);
            }
        }

        // mobidata_list.append(self.data_file[self.sections[self.records + 1][0]:])
        if (numSections > records + 1) {
            rangeToWrite = Arrays.copyOfRange(dataFile, sections.get(records + 1).offset(), dataFile.length);
            outputStream.write(rangeToWrite);
        }

        mobiData = outputStream.toByteArray();

        System.out.println(" done");
    }

    private Set<String> normalisePids(Set<String> pidSet) throws IOException {
        Set<String> goodPids = new LinkedHashSet<>();

        for (String pid : pidSet) {
            if (pid.length() == 10) {
                String substring = pid.substring(0, 8);

                if (!checksumPid(substring, letters).equals(pid))
                    System.out.printf("Warning: PID %s has an incorrect checksum, should have been %s%n", pid, checksumPid(substring, letters));

                goodPids.add(substring);
            } else if (pid.length() == 8) goodPids.add(pid);
            else System.out.printf("Warning: PID %s has the wrong number of digits%n", pid);
        }

        return goodPids;
    }
}
