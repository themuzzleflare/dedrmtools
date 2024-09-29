/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.mobidedrm;

import org.jetbrains.annotations.NotNull;

import static cloud.tavitian.dedrmtools.Util.formatByteArray;

record DRMInfo(byte[] key, String pid) {
    @Override
    public @NotNull String toString() {
        return String.format("{key=%s, pid=%s}", formatByteArray(key), pid);
    }
}
