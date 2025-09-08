/*
 * Copyright © 2024-2025 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import org.jetbrains.annotations.Contract;

import java.util.Set;

public final class SystemProfilerStorageDriveDataTypes {
    public static final String SP_SERIAL_ATA_DATA_TYPE = "SPSerialATADataType";
    public static final String SP_NVME_DATA_TYPE = "SPNVMeDataType";

    public static final Set<String> all = Set.of(SP_SERIAL_ATA_DATA_TYPE, SP_NVME_DATA_TYPE);

    @Contract(pure = true)
    private SystemProfilerStorageDriveDataTypes() {
    }
}
