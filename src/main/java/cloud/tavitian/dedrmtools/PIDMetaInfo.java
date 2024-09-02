/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import static cloud.tavitian.dedrmtools.Util.formatByteArray;

public record PIDMetaInfo(byte[] rec209, byte[] token) {
    @Override
    public String toString() {
        return String.format("{rec209=%s, token=%s}", formatByteArray(rec209), formatByteArray(token));
    }
}
