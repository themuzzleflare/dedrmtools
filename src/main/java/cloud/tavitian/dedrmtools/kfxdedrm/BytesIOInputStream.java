/*
 * Copyright © 2024-2025 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kfxdedrm;

import org.jetbrains.annotations.Contract;

import java.io.ByteArrayInputStream;

final class BytesIOInputStream extends ByteArrayInputStream {
    public BytesIOInputStream(byte[] buf) {
        super(buf);
    }

    @Contract(pure = true)
    public synchronized int tell() {
        return pos;
    }

    public synchronized void seek(int position) {
        reset();
        //noinspection ResultOfMethodCallIgnored
        skip(position);
    }
}
