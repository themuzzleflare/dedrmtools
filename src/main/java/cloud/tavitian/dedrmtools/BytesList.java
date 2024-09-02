/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import java.util.ArrayList;
import java.util.Iterator;

import static cloud.tavitian.dedrmtools.Util.formatByteArray;

public class BytesList extends ArrayList<byte[]> {
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
