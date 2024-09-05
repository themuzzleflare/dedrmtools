/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import java.util.Map;
import java.util.Set;

interface KindleKeyManager {
    byte[] getUsername();

    Set<String> getKindleInfoFiles();

    Map<String, byte[]> getDbFromFile(String kInfoFile);
}
