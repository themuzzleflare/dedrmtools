/*
 * Copyright © 2024-2025 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kfxdedrm;

record ContainerRec(int nextPos, int tid, int remaining) {
    @Override
    public String toString() {
        return String.format("ContainerRec{nextPos=%d, tid=%d, remaining=%d}", nextPos, tid, remaining);
    }
}
