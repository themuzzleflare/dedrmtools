/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import java.io.ByteArrayInputStream;

public final class BytesIOInputStream extends ByteArrayInputStream {
    public BytesIOInputStream(byte[] buf) {
        super(buf);
    }

    public int tell() {
        return pos;
    }

    public void seek(int position) {
        reset();
        skip(position);
    }
}
