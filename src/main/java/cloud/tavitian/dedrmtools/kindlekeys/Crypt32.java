/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

interface Crypt32 extends StdCallLibrary {
    Crypt32 INSTANCE = Native.load("crypt32", Crypt32.class, W32APIOptions.DEFAULT_OPTIONS);

    boolean CryptUnprotectData(DATA_BLOB pDataIn, PointerByReference ppszDataDescr, DATA_BLOB pOptionalEntropy,
                               Pointer pvReserved, Pointer pPromptStruct, int dwFlags, DATA_BLOB pDataOut);
}
