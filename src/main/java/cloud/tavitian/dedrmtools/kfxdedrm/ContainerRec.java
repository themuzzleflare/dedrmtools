/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kfxdedrm;

public record ContainerRec(int nextPos, int tid, int remaining) {
    public String toString() {
        return String.format("ContainerRec{nextPos=%d, tid=%d, remaining=%d}", nextPos, tid, remaining);
    }
}
