/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools.topazextract;

import cloud.tavitian.dedrmtools.Book;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class TopazBook extends Book {
    private static final String VERSION = "1.0";

    public TopazBook(@SuppressWarnings("unused") String infile) {
        super();
        System.out.printf("TopazExtract v%s.%n", VERSION);
        System.out.println("Removes DRM protection from Topaz eBooks and extracts the contents.");
    }

    @Contract(pure = true)
    @Override
    public @NotNull String getBookType() {
        return "Topaz";
    }

    @Contract(pure = true)
    @Override
    public @NotNull String getBookExtension() {
        return ".htmlz";
    }
}
