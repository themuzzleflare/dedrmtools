/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import java.io.IOException;
import java.util.Set;

import static cloud.tavitian.dedrmtools.Util.commaSeparatedStringToSanitisedSet;
import static cloud.tavitian.dedrmtools.Util.toSet;

public abstract class Book implements BookManager, BookCleanup {
    @Override
    public PIDMetaInfo getPidMetaInfo() {
        return new PIDMetaInfo(null, null);
    }

    /**
     * @param pids pid strings
     * @throws Exception if an error occurs
     */
    @SuppressWarnings("unused")
    public void processBook(String... pids) throws Exception {
        processBook(toSet(pids));
    }

    /**
     * @param pids comma separated string of pids
     * @throws Exception if an error occurs
     */
    public void processBook(String pids) throws Exception {
        processBook(commaSeparatedStringToSanitisedSet(pids));
    }

    @Override
    public void cleanup() {
    }

    @Override
    public String getBookTitle() {
        return "";
    }

    @Override
    public void processBook(Set<String> pidSet) throws Exception {
    }

    @Override
    public void getFile(String outpath) throws IOException {
    }
}
