/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kfxdedrm;

import java.io.ByteArrayInputStream;

final class BytesIOInputStream extends ByteArrayInputStream {
    public BytesIOInputStream(byte[] buf) {
        super(buf);
    }

    public synchronized int tell() {
        return pos;
    }

    public synchronized void seek(int position) {
        reset();
        //noinspection ResultOfMethodCallIgnored
        skip(position);
    }
}
