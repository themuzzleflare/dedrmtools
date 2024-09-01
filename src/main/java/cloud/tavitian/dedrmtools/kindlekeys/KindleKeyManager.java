/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import java.util.List;

interface KindleKeyManager {
    byte[] getUsername();

    List<String> getKindleInfoFiles();

    KindleDatabase<byte[]> getDbFromFile(String kInfoFile);
}
