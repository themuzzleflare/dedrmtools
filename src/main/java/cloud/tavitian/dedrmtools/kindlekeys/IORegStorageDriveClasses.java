/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import java.util.Set;

public final class IORegStorageDriveClasses {
    public static final String APPLE_AHCI_DISK_DRIVER = "AppleAHCIDiskDriver";
    public static final String APPLE_ANS3_NVME_CONTROLLER = "AppleANS3NVMeController";

    public static final Set<String> all = Set.of(
            APPLE_AHCI_DISK_DRIVER,
            APPLE_ANS3_NVME_CONTROLLER
    );

    private IORegStorageDriveClasses() {
    }
}
