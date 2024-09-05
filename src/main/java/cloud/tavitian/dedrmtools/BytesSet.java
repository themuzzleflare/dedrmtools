/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import java.util.Iterator;
import java.util.LinkedHashSet;

import static cloud.tavitian.dedrmtools.Util.formatByteArray;

public class BytesSet extends LinkedHashSet<byte[]> {
    @Override
    public String toString() {
        Iterator<byte[]> it = iterator();

        if (!it.hasNext()) return "[]";

        StringBuilder sb = new StringBuilder("[");

        for (; ; ) {
            byte[] e = it.next();
            sb.append(formatByteArray(e));

            if (!it.hasNext()) return sb.append(']').toString();

            sb.append(',').append(' ');
        }
    }
}
