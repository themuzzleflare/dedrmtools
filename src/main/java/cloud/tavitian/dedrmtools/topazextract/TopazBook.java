/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.topazextract;

import cloud.tavitian.dedrmtools.Book;

public final class TopazBook extends Book {
    private static final String VERSION = "1.0";

    public TopazBook(@SuppressWarnings("unused") String infile) {
        super();
        System.out.printf("TopazExtract v%s.%n", VERSION);
        System.out.println("Removes DRM protection from Topaz eBooks and extracts the contents.");
    }

    @Override
    public String getBookType() {
        return "Topaz";
    }

    @Override
    public String getBookExtension() {
        return ".htmlz";
    }
}
