/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import java.util.Arrays;

public record PIDMetaInfo(byte[] rec209, byte[] token) {
    @Override
    public String toString() {
        return String.format("PIDMetaInfo{rec209=%s, token=%s}", Arrays.toString(rec209), Arrays.toString(token));
    }
}
