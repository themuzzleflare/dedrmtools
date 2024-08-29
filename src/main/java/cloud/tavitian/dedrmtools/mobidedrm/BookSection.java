/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.mobidedrm;

record BookSection(int offset, int flags, int val) {
    @Override
    public String toString() {
        return String.format("(%d, %d, %d)", offset, flags, val);
    }
}
