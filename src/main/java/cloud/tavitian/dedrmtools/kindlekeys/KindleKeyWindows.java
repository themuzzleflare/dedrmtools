/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kindlekeys;

import java.util.List;

final class KindleKeyWindows extends KindleKey {
    @Override
    public List<byte[]> getIdStrings() {
        return List.of();
    }

    @Override
    public byte[] getUsername() {
        return new byte[0];
    }

    @Override
    public List<String> getKindleInfoFiles() {
        return List.of();
    }

    @Override
    public KindleDatabase<byte[]> getDbFromFile(String kInfoFile) {
        return null;
    }
}
