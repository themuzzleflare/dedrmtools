/*
 * Copyright © 2024-2025 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kfxdedrm;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

record ContainerRec(int nextPos, int tid, int remaining) {
    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return String.format("ContainerRec{nextPos=%d, tid=%d, remaining=%d}", nextPos, tid, remaining);
    }
}
