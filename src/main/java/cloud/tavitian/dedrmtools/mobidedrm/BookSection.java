/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.mobidedrm;

public record BookSection(int offset, int flags, int val) {
    public String toString() {
        return String.format("(%d, %d, %d)", offset, flags, val);
    }
}
