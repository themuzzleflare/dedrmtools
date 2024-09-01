/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

interface Kernel32 extends StdCallLibrary {
    Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class, W32APIOptions.DEFAULT_OPTIONS);

    int GetSystemDirectoryW(char[] lpBuffer, int uSize);

    int GetVolumeInformationW(String lpRootPathName, char[] lpVolumeNameBuffer, int nVolumeNameSize,
                              IntByReference lpVolumeSerialNumber, IntByReference lpMaximumComponentLength,
                              IntByReference lpFileSystemFlags, char[] lpFileSystemNameBuffer, int nFileSystemNameSize);

    int GetEnvironmentVariableW(String lpName, char[] lpBuffer, int nSize);

    int GetLastError();
}
