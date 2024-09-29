/*
 * Copyright © 2024 Paul Tavitian.
 */

package cloud.tavitian.dedrmtools;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;

public final class BookFile extends File {
    public BookFile(String pathname) {
        super(pathname);
    }

    @SuppressWarnings("unused")
    public BookFile(String parent, String child) {
        super(parent, child);
    }

    @SuppressWarnings("unused")
    public BookFile(File parent, String child) {
        super(parent, child);
    }

    @SuppressWarnings("unused")
    public BookFile(URI uri) {
        super(uri);
    }

    public @NotNull String getRoot() {
        String fileName = getName();

        // Find the last dot in the file name
        int lastDotIndex = fileName.lastIndexOf('.');

        // No extension found or dot is at the beginning (e.g., ".hiddenfile")
        if (lastDotIndex == -1 || lastDotIndex == 0) return fileName;

        // Return the root (file name without extension)
        return fileName.substring(0, lastDotIndex);
    }
}
