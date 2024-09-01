/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

@FieldOrder({"cbData", "pbData"})
public class DATA_BLOB extends Structure {
    public int cbData;
    public Pointer pbData;
}
