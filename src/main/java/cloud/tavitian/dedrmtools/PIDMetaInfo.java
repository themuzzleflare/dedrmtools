/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import org.jetbrains.annotations.NotNull;

import static cloud.tavitian.dedrmtools.Util.formatByteArray;

public record PIDMetaInfo(byte[] rec209, byte[] token) {
    @Override
    public @NotNull String toString() {
        return String.format("{rec209=%s, token=%s}", formatByteArray(rec209), formatByteArray(token));
    }
}
