/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.mobidedrm;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

record BookSection(int offset, int flags, int val) {
    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return String.format("(%d, %d, %d)", offset, flags, val);
    }
}
