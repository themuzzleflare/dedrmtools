/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import java.util.List;

interface KindleKeyManager {
    List<byte[]> getIdStrings();

    byte[] getUsername();

    List<String> getKindleInfoFiles();

    KindleDatabase<byte[]> getDbFromFile(String kInfoFile);
}
