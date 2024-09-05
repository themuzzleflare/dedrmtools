/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import java.util.Set;

public final class SystemProfilerStorageDriveDataTypes {
    public static final String SPSerialATADataType = "SPSerialATADataType";
    public static final String SPNVMeDataType = "SPNVMeDataType";

    public static final Set<String> all = Set.of(
            SPSerialATADataType,
            SPNVMeDataType
    );

    private SystemProfilerStorageDriveDataTypes() {
    }
}
