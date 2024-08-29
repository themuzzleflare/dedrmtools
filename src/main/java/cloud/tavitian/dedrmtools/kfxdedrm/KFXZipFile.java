/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.kfxdedrm;

import java.io.IOException;
import java.util.zip.ZipFile;

final class KFXZipFile extends ZipFile {
    public KFXZipFile(String name) throws IOException {
        super(name);
    }
}
