/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kfxdedrm;

import cloud.tavitian.dedrmtools.BytesIOInputStream;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cloud.tavitian.dedrmtools.Util.*;
import static cloud.tavitian.dedrmtools.kfxdedrm.IonConstants.*;

public final class BinaryIonParser {
    private final BytesIOInputStream stream;
    private final int initPos;
    private final List<Integer> annotations = new ArrayList<>();
    private final List<IonCatalogItem> catalog = new ArrayList<>();
    private final SymbolTable symbols = new SymbolTable();
    private boolean eof = false;
    private ParserState state;
    private int localRemaining = 0;
    private boolean needHasNext = false;
    private boolean isInStruct = false;
    private int valueTid = -1;
    private int valueFieldId = -1;
    private int parentTid = 0;
    private int valueLen = 0;
    private boolean valueIsNull = false;
    private boolean valueIsTrue = false;
    private Object value;
    private boolean didImports = false;
    private List<ContainerRec> containerStack;

    public BinaryIonParser(BytesIOInputStream stream) {
        this.stream = stream;
        initPos = stream.tell();

        reset();
    }

    public void reset() {
        state = ParserState.BEFORE_TID;
        needHasNext = true;
        localRemaining = -1;
        eof = false;
        isInStruct = false;
        containerStack = new ArrayList<>();
        stream.seek(initPos);
    }

    public boolean hasNext() throws Exception {
        while (needHasNext && !eof) {
            hasNextRaw();

            if (containerStack.isEmpty() && !valueIsNull) {
                if (valueTid == TID_SYMBOL) {
                    if (value instanceof Integer intValue && intValue == SID_ION_1_0) needHasNext = true;
                } else if (valueTid == TID_STRUCT) {
                    for (int a : annotations) {
                        if (a == SID_ION_SYMBOL_TABLE) {
                            parseSymbolTable();
                            needHasNext = true;
                            break;
                        }
                    }
                }
            }
        }

        return !eof;
    }

    private void hasNextRaw() throws Exception {
        clearValue();

        while (valueTid == -1 && !eof) {
            needHasNext = false;

            if (state == ParserState.BEFORE_FIELD) {
                if (valueFieldId != SID_UNKNOWN) throw new IllegalStateException("Unexpected field ID");

                valueFieldId = readFieldId();

                if (valueFieldId != SID_UNKNOWN) state = ParserState.BEFORE_TID;
                else eof = true;
            } else if (state == ParserState.BEFORE_TID) {
                state = ParserState.BEFORE_VALUE;
                valueTid = readTypeId();

                if (valueTid == -1) {
                    state = ParserState.EOF;
                    eof = true;
                    break;
                }

                if (valueTid == TID_TYPEDECL) {
                    if (valueLen == 0) checkVersionMarker();
                    else loadAnnotations();
                }
            } else if (state == ParserState.BEFORE_VALUE) {
                skip(valueLen);
                state = ParserState.AFTER_VALUE;
            } else if (state == ParserState.AFTER_VALUE) {
                if (isInStruct) state = ParserState.BEFORE_FIELD;
                else state = ParserState.BEFORE_TID;
            } else {
                if (state != ParserState.EOF) throw new IllegalStateException("Unexpected state" + state);
            }
        }
    }

    public int next() throws Exception {
        if (hasNext()) {
            needHasNext = true;
            return valueTid;
        } else return -1;
    }

    public void stepIn() throws Exception {
        if ((valueTid != TID_STRUCT && valueTid != TID_LIST && valueTid != TID_SEXP) || eof)
            throw new Exception(String.format("valuetid=%s eof=%s", valueTid, eof));

        if ((valueIsNull && state != ParserState.AFTER_VALUE) || (!valueIsNull && state != ParserState.BEFORE_VALUE)) {
            throw new Exception(String.format("valuenull=%s state=%s", valueIsNull, state));
        }

        int nextRem = localRemaining;

        if (nextRem != -1) {
            nextRem -= valueLen;

            if (nextRem < 0) nextRem = 0;
        }

        push(parentTid, stream.tell() + valueLen, nextRem);

        isInStruct = (valueTid == TID_STRUCT);

        if (isInStruct) state = ParserState.BEFORE_FIELD;
        else state = ParserState.BEFORE_TID;

        localRemaining = valueLen;
        parentTid = valueTid;
        clearValue();
        needHasNext = true;
    }

    public void stepOut() throws IOException {
        ContainerRec rec = containerStack.removeLast();

        eof = false;
        parentTid = rec.tid();

        if (parentTid == TID_STRUCT) {
            isInStruct = true;
            state = ParserState.BEFORE_FIELD;
        } else {
            isInStruct = false;
            state = ParserState.BEFORE_TID;
        }

        needHasNext = true;

        clearValue();

        int curPos = stream.tell();

        if (rec.nextPos() > curPos) skip(rec.nextPos() - curPos);
        else {
            if (rec.nextPos() != curPos) throw new IllegalStateException("Mismatch in stream position");
        }

        localRemaining = rec.remaining();
    }

    private void push(int typeId, int nextPosition, int nextRemaining) {
        containerStack.add(new ContainerRec(nextPosition, typeId, nextRemaining));
    }

    private void clearValue() {
        valueTid = -1;
        value = null;
        valueIsNull = false;
        valueFieldId = SID_UNKNOWN;
        annotations.clear();
    }

    private byte[] read() throws IOException {
        return read(1);
    }

    private byte[] read(int count) throws IOException {
        if (localRemaining != -1) {
            localRemaining -= count;

            if (localRemaining < 0) throw new IllegalStateException("EOF encountered");
        }

        byte[] result = stream.readNBytes(count);

        if (result.length == 0) throw new EOFException("Unexpected end of stream.");

        return result;
    }

    private int readVarUInt() throws IOException {
        int b = ord(read());
        int result = b & 0x7F;

        int i = 0;

        while ((b & 0x80) == 0 && i < 4) {
            b = ord(read());
            result = (result << 7) | (b & 0x7F);
            i++;
        }

        if (i >= 4 && (b & 0x80) == 0) throw new IllegalStateException("int overflow");

        return result;
    }

    private int readVarInt() throws IOException {
        int b = ord(read());

        boolean negative = (b & 0x40) != 0;
        int result = b & 0x3F;

        int i = 0;

        while ((b & 0x80) == 0 && i < 4) {
            b = ord(read());

            result = (result << 7) | (b & 0x7F);
            i++;
        }

        if (i >= 4 && (b & 0x80) == 0) throw new IllegalStateException("int overflow");

        if (negative) return -result;

        return result;
    }

    private double readDecimal() throws IOException {
        System.out.println("Reading decimal");

        if (valueLen == 0) return 0;

        int rem = localRemaining - valueLen;
        localRemaining = valueLen;

        int exponent = readVarInt();

        if (localRemaining <= 0) throw new IllegalStateException("Only exponent in ReadDecimal");

        if (localRemaining > 8) throw new IllegalStateException("Decimal overflow");

        boolean signed = false;

        List<Integer> b = ordList(read(localRemaining));

        if ((b.getFirst() & 0x80) != 0) {
            b.set(0, (b.getFirst() & 0x7F));
            signed = true;
        }

        // Convert variably sized network order integer into 64-bit little-endian
        int j = 0;
        int[] vb = new int[8];

        for (int i = b.size(); i >= 0; i--) {
            vb[i] = b.get(j);
            j++;
        }

        long v = ByteBuffer.wrap(toByteArray(vb)).getLong();
        double result = v * Math.pow(10, exponent);

        if (signed) result = -result;

        localRemaining = rem;

        return result;
    }

    private int readFieldId() {
        if (localRemaining != -1 && localRemaining < 1) return -1;

        try {
            return readVarUInt();
        } catch (Exception e) {
            return -1;
        }
    }

    private int readTypeId() throws IOException {
        if (localRemaining != -1) {
            if (localRemaining < 1) return -1;
            localRemaining -= 1;
        }

        byte[] b = stream.readNBytes(1);

        if (b.length < 1) return -1;

        int bInt = ord(b);

        int result = bInt >> 4;
        int ln = bInt & 0xF;

        if (ln == LEN_IS_VAR_LEN) ln = readVarUInt();
        else if (ln == LEN_IS_NULL) {
            ln = 0;
            state = ParserState.AFTER_VALUE;
        } else if (result == TID_NULL) {
            // Must have LEN_IS_NULL
            throw new IllegalStateException("Unexpected NULL type ID.");
        } else if (result == TID_BOOLEAN) {
            if (ln > 1) throw new IllegalStateException("Invalid boolean length.");

            valueIsTrue = (ln == 1);
            ln = 0;
            state = ParserState.AFTER_VALUE;
        } else if (result == TID_STRUCT) {
            if (ln == 1) ln = readVarUInt();
        }

        valueLen = ln;

        return result;
    }

    private void skip(int count) throws IOException {
        if (localRemaining != -1) {
            localRemaining -= count;

            if (localRemaining < 0) throw new EOFException("EOF encountered");
        }

        stream.skip(count);
    }

    public void addToCatalog(String name, int version, List<String> symbols) {
        catalog.add(new IonCatalogItem(name, version, symbols));
    }

    private void parseSymbolTable() throws Exception {
        // Advance to the next value (shouldn't do anything meaningful)
        next();

        if (valueTid != TID_STRUCT)
            throw new IllegalStateException(String.format("Expected a TID_STRUCT but found: %s", valueTid));

        if (didImports) return;

        stepIn();

        int fieldType = next();

        while (fieldType != -1) {
            if (!valueIsNull) {
                if (valueFieldId != SID_IMPORTS) throw new IllegalStateException("Unsupported symbol table field id");

                if (fieldType == TID_LIST) gatherImports();
            }

            fieldType = next();
        }

        stepOut();
        didImports = true;
    }

    private void gatherImports() throws Exception {
        stepIn();

        int t = next();

        while (t != -1) {
            if (!valueIsNull && t == TID_STRUCT) readImport();
            t = next();
        }

        stepOut();
    }

    private void checkVersionMarker() throws IOException {
        for (byte[] marker : VERSION_MARKER) {
            if (!Arrays.equals(read(), marker)) throw new IllegalStateException("Unknown version marker");
        }

        valueLen = 0;
        valueTid = TID_SYMBOL;
        value = SID_ION_1_0;
        valueIsNull = false;
        valueFieldId = SID_UNKNOWN;
        state = ParserState.AFTER_VALUE;
    }

    private void loadAnnotations() throws IOException {
        int ln = readVarUInt();
        long maxPos = stream.tell() + ln;

        while (stream.tell() < maxPos) annotations.add(readVarUInt());

        valueTid = readTypeId();
    }

    private void readImport() throws Exception {
        int version = -1;
        int maxId = -1;
        String name = "";

        stepIn();

        int t = next();

        while (t != -1) {
            if (!valueIsNull && valueFieldId != SID_UNKNOWN) {
                if (valueFieldId == SID_NAME) name = stringValue();
                else if (valueFieldId == SID_VERSION) version = intValue();
                else if (valueFieldId == SID_MAX_ID) maxId = intValue();
            }

            t = next();
        }

        stepOut();

        if (name.isEmpty() || name.equals(SystemSymbols.ION)) return;

        if (version < 1) version = 1;

        IonCatalogItem table = findCatalogItem(name);

        if (maxId < 0) {
            if (table == null || version != table.version())
                throw new IllegalStateException(String.format("Import %s lacks maxId", name));

            maxId = table.symnames().size();
        }

        if (table != null) {
            symbols.importSymbols(table, Math.min(maxId, table.symnames().size()));

            if (table.symnames().size() < maxId)
                symbols.importUnknown(String.format("%s-unknown", name), maxId - table.symnames().size());
        } else symbols.importUnknown(name, maxId);
    }

    private IonCatalogItem findCatalogItem(String name) {
        for (IonCatalogItem item : catalog) if (item.name().equals(name)) return item;
        return null; // Return null if no matching item is found
    }

    private void prepareValue() throws IOException {
        if (value == null) loadScalarValue();
    }

    private int intValue() throws IOException {
        if (valueTid != TID_POSINT && valueTid != TID_NEGINT) throw new IllegalStateException("Not an int");

        prepareValue();

        // Assuming value is stored as an Integer after being processed by loadScalarValue
        if (value instanceof Integer intValue) return intValue;
        else
            throw new IllegalStateException(String.format("Expected an integer value but found: %s", value.getClass().getSimpleName()));
    }

    public String stringValue() throws IOException {
        if (valueTid != TID_STRING) throw new IllegalStateException("Not a string");

        if (valueIsNull) return "";

        prepareValue();

        if (value instanceof String string) return string;
        else
            throw new IllegalStateException(String.format("Expected a string value but found: %s", value.getClass().getSimpleName()));
    }

    private String symbolValue() throws IOException {
        if (valueTid != TID_SYMBOL) throw new IllegalStateException("Not a symbol");

        prepareValue();

        if (value instanceof Integer symbolId) {
            String result = symbols.findById(symbolId);

            if (result.isEmpty()) result = String.format("SYMBOL#%d", symbolId);

            return result;
        } else
            throw new IllegalStateException(String.format("Expected an integer value for symbol ID but found: %s", value.getClass().getSimpleName()));
    }

    public byte[] lobValue() throws IOException {
        if (valueTid != TID_CLOB && valueTid != TID_BLOB)
            throw new IllegalStateException(String.format("Not a LOB type: %s", getFieldName()));

        if (valueIsNull) return null;

        byte[] result = read(valueLen);

        state = ParserState.AFTER_VALUE;

        return result;
    }

    private double decimalValue() throws IOException {
        if (valueTid != TID_DECIMAL) throw new IllegalStateException("Not a decimal");

        prepareValue();

        if (value instanceof Double doubleVal) return doubleVal;
        else
            throw new IllegalStateException(String.format("Expected a decimal value but found: %s", value.getClass().getSimpleName()));
    }

    public String getFieldName() {
        if (valueFieldId == SID_UNKNOWN) return "";

        return symbols.findById(valueFieldId);
    }

    private SymbolToken getFieldNameSymbol() {
        return new SymbolToken(getFieldName(), valueFieldId);
    }

    public String getTypeName() {
        if (annotations.isEmpty()) return "";
        return symbols.findById(annotations.getFirst());
    }

    private void forceImport(List<String> symbols) {
        IonCatalogItem item = new IonCatalogItem("Forced", 1, symbols);
        this.symbols.importSymbols(item, symbols.size());
    }

    private void loadScalarValue() throws IOException {
        if (valueTid != TID_NULL && valueTid != TID_BOOLEAN && valueTid != TID_POSINT &&
                valueTid != TID_NEGINT && valueTid != TID_FLOAT && valueTid != TID_DECIMAL &&
                valueTid != TID_TIMESTAMP && valueTid != TID_SYMBOL && valueTid != TID_STRING) return;

        if (valueIsNull) {
            value = null;
            return;
        }

        if (valueTid == TID_STRING) {
            byte[] stringBytes = read(valueLen);
            value = new String(stringBytes, StandardCharsets.UTF_8);
        } else if (valueTid == TID_POSINT || valueTid == TID_NEGINT || valueTid == TID_SYMBOL) {
            if (valueLen == 0) value = 0;
            else {
                if (valueLen > 4) throw new IllegalStateException(String.format("int too long: %d", valueLen));

                int v = 0;

                for (int i = valueLen - 1; i >= 0; i--) v |= ord(read()) << (i * 8);

                if (valueTid == TID_NEGINT) value = -v;
                else value = v;
            }
        } else if (valueTid == TID_DECIMAL) value = readDecimal();

        state = ParserState.AFTER_VALUE;
    }

    private void ionWalk(int supert, String indent, List<String> lst) throws Exception {
        while (hasNext()) {
            String l;
            if (supert == TID_STRUCT) l = String.format("%s:", getFieldName());
            else l = "";

            int t = next();

            if (t == TID_STRUCT || t == TID_LIST) {
                if (!l.isEmpty()) lst.add(indent + l);

                l = getTypeName();

                if (!l.isEmpty()) lst.add(String.format("%s%s::", indent, l));

                if (t == TID_STRUCT) lst.add(String.format("%s{", indent));
                else lst.add(String.format("%s[", indent));

                stepIn();
                ionWalk(t, indent + " ", lst);
                stepOut();

                if (t == TID_STRUCT) lst.add(indent + "}");
                else lst.add(indent + "]");
            } else {
                switch (t) {
                    case TID_STRING:
                        l += String.format("\"%s\"", stringValue());
                        break;
                    case TID_CLOB, TID_BLOB:
                        l += String.format("{%s}", printLob(lobValue()));
                        break;
                    case TID_POSINT:
                        l += String.valueOf(intValue());
                        break;
                    case TID_SYMBOL:
                        String tn = getTypeName();
                        if (!tn.isEmpty()) {
                            tn += "::";
                        }
                        l += tn + symbolValue();
                        break;
                    case TID_DECIMAL:
                        l += String.valueOf(decimalValue());
                        break;
                    default:
                        l += String.format("TID %d", t);
                }

                lst.add(indent + l);
            }
        }
    }

    private String printLob(byte[] b) {
        if (b == null) return "null";

        StringBuilder result = new StringBuilder();

        for (byte value : b) result.append(String.format("%02x ", value));

        // Remove the trailing space if result is not empty
        if (!result.isEmpty()) result.setLength(result.length() - 1);

        return result.toString();
    }

    private void print(List<String> lst) throws Exception {
        reset();
        ionWalk(-1, "", lst);
    }
}
