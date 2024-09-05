/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import java.util.Set;

public final class IORegStorageDriveClasses {
    public static final String AppleAHCIDiskDriver = "AppleAHCIDiskDriver";
    public static final String AppleANS3NVMeController = "AppleANS3NVMeController";

    public static final Set<String> all = Set.of(
            AppleAHCIDiskDriver,
            AppleANS3NVMeController
    );

    private IORegStorageDriveClasses() {
    }
}
