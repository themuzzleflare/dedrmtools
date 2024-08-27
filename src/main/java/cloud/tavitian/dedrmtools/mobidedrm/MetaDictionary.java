/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.mobidedrm;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static cloud.tavitian.dedrmtools.Util.formatByteArray;

public final class MetaDictionary extends LinkedHashMap<Integer, byte[]> {
    public String toString() {
        Iterator<Map.Entry<Integer, byte[]>> i = entrySet().iterator();

        if (!i.hasNext())
            return "{}";

        StringBuilder sb = new StringBuilder();

        sb.append('{');

        for (; ; ) {
            Map.Entry<Integer, byte[]> e = i.next();
            Integer key = e.getKey();
            byte[] value = e.getValue();

            sb.append(key);
            sb.append('=');
            sb.append(formatByteArray(value));

            if (!i.hasNext())
                return sb.append('}').toString();

            sb.append(',').append(' ');
        }
    }
}
