/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import java.io.IOException;
import java.util.Set;

interface BookManager {
    String getBookTitle();

    String getBookType();

    String getBookExtension();

    void getFile(String outpath) throws IOException;

    void processBook(Set<String> pidSet) throws Exception;

    PIDMetaInfo getPidMetaInfo();
}
